package s0538335.my.code;

import java.awt.List;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.omg.PortableInterceptor.DISCARDING;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.Info;

import s0538335.my.code.Tile;

public class MyAI extends AI {

	private float acceleration;

	private int rotationTime = 2;
	private int wishedTime = 4;
	
	private static final int SIZE = 10;
	private boolean[][] raster = new boolean[info.getWorld().getWidth() / SIZE][info.getWorld().getHeight() / SIZE];

	public MyAI(Info arg0) {
		super(arg0);
		acceleration = info.getMaxAcceleration();
	}

	@Override
	public float getAcceleration() {
		return acceleration;
	}

	@Override
	public float getAngularAcceleration() {
		return seek();
	}

	@Override
	public String getName() {

		return "Autooo";
	}

	// Draw line to target

	@Override
	public void drawDebugStuff() {
		createRaster();
		findPath();
	}

	public void createRaster() {
		
		Polygon[] obstacle = info.getWorld().getObstacles();

		for (int x = 0; x < info.getWorld().getWidth() / SIZE; x++) {
			for (int y = 0; y < info.getWorld().getHeight() / SIZE; y++) {
				raster[x][y] = true;
				Rectangle2D.Float rec = new Rectangle2D.Float(x * SIZE, y * SIZE, SIZE, SIZE);
				for (Polygon obstaclePoint : obstacle) {
					if (obstaclePoint.intersects(rec)) {
						raster[x][y] = false;
					}

				}
				GL11.glBegin(GL11.GL_QUADS);
			    GL11.glVertex3f(x*SIZE, y*SIZE,0.1f);
			    GL11.glVertex3f(x*SIZE + SIZE, y*SIZE,0.1f);
			    GL11.glVertex3f(x*SIZE + SIZE, y*SIZE + SIZE,0.1f);
			    GL11.glVertex3f(x*SIZE, y*SIZE + SIZE,0.1f);
			    GL11.glEnd();
			}

		}

	}
	
	public void findPath(){
		final int [][] distance = new int [raster.length][raster.length];  // shortest known distance from "s"
		final int [][] previous = new int [raster.length][raster.length];  // preceeding node in path
		final boolean [][] visited = new boolean [raster.length][raster.length];
		
		for (int x=0; x<distance.length; x++) {
			for (int y=0; y<distance.length; y++) {
				distance[x][y] = Integer.MAX_VALUE;
			}
		}
		
		distance[Math.round(info.getX()/SIZE)][Math.round(info.getY()/SIZE)] = 0;
		
		for (int i=0; i<distance.length; i++) {
			final int[] nextCoord = minVertex(distance, visited);
			visited[nextCoord[0]][nextCoord[1]] = true;
			
			// The shortest path to next is dist[next] and via pred[next].
		
			 ArrayList<Tile> neighbours = findNeighbour(nextCoord);
			 
			 
//			 for (int j=0; j<neighbours.length; j++) {
//				 neighbours[i][j]
//				 ToDo find neighbours!
				 
//				 final int v = neighbours[j];
//			     final int d = distance[next] + G.getWeight(next,v);
//			     if (distance[v] > d) {
//			    	 distance[v] = d;
//			         pred[v] = next;
//			     }
			 }
//		}
//		return pred
	}
	
	private static int[] minVertex (int[][] distance, boolean[][] visited) {
		int dist = Integer.MAX_VALUE;
		int[] nextCoord = new int[2];
		int nextX = -1;
		int nextY = -1; // graph not connected, or no unvisited vertices
		for (int i = 0; i < distance.length; i++) {
			for (int j = 0; j < distance.length; j++){
				if (!visited[i][j] && distance[i][j] < dist) {
					nextX = i;
					nextY = j;
					dist=distance[i][j];
				}
			}
		 }
		nextCoord[0] = nextX;
		nextCoord[1] = nextY;
		 return nextCoord;
	}
	
	private ArrayList<Tile> findNeighbour(int[] position){
		ArrayList<Tile> tiles = new ArrayList<>();
		int x = position[0];
		int y = position[1];
		
		for (int row = -1; row < 2; row++) {
			for (int col = -1; col < 2; col++) {
				if (row == 0 && col == 0){
					continue;
				}else{
					Tile tile = new Tile();
					tile.setxCoord(x + row);
					tile.setyCoord(y + col);
					tile.setAccessible(raster[x + row][y + col]);
					
					if(Math.abs(row)==1 && Math.abs(col)==1){
						tile.setWeight(14);
					}else{
						tile.setWeight(10);
					}
					
					tiles.add(tile);
				}
			}
		}
		return tiles;
		
	}
	
	
	// seek to target

	public float seek() {

		acceleration = info.getMaxAcceleration();

		float newOrientation = getNewOrientation(info.getCurrentCheckpoint().x, info.getCurrentCheckpoint().y);

		float difference = newOrientation - info.getOrientation();

		float wunschdrehGeschwindigkeite = difference * info.getMaxAngularVelocity() / rotationTime;
		float drehBeschleunigung = (wunschdrehGeschwindigkeite - info.getAngularVelocity()) / wishedTime;

		return drehBeschleunigung;
	}

	// Orientation to the target

	public float getNewOrientation(float x, float y) {
		float distanceX = x - info.getX();
		float distanceY = y - info.getY();
		float newOrientation = (float) Math.atan2(distanceY, distanceX);
		return newOrientation;
	}

}
