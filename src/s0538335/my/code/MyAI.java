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

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

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
	
	ArrayList<Tile> openList = new ArrayList<Tile>();
	ArrayList<Tile> closeList = new ArrayList<Tile>();

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
		final boolean [][] visited = new boolean [raster.length][raster.length];
		
		Tile startTile = new Tile();
		startTile.setxCoord(Math.round(info.getX()/SIZE));
		startTile.setyCoord(Math.round(info.getY()/SIZE));
		startTile.setAccessible(raster[startTile.getxCoord()][startTile.getyCoord()]);
		startTile.setWeight(0);
		
		Tile targetTile = new Tile();
		targetTile.setxCoord(Math.round(info.getCurrentCheckpoint().x/SIZE));
		targetTile.setxCoord(Math.round(info.getCurrentCheckpoint().y/SIZE));
		targetTile.setAccessible(raster[targetTile.getxCoord()][targetTile.getyCoord()]);
		
		//first add startTile to openList
		openList.add(startTile);
		
		
		while(!closeList.contains(targetTile) || !openList.isEmpty()){
		
			Tile actualTile = findLowestWeight();
			openList.remove(actualTile);
			closeList.add(actualTile);
			
			ArrayList<Tile> neighbours = findNeighbour(actualTile);
			 
			for (Tile tile : neighbours) {
				if (tile.accessible && !closeList.contains(tile)){
					if(!openList.contains(tile)){
						openList.add(tile);
						tile.setPrev(actualTile);
					}else{
						// to do: wie kann ich das Gewicht vergleichen? Contains möglicher Weise nicht machbar
						// wenn gewicht besser dann prev. ändern
					}
					
				}
			}
		}
	}
	
	public ArrayList<Tile> storePath(Tile startTile, Tile targetTile){
		ArrayList<Tile> path = new ArrayList<Tile>();
		Tile actualTile = targetTile;
		
		while (!actualTile.equals(startTile)){
			path.add(actualTile);
			actualTile = actualTile.getPrev();
		}
		
		return path;
		
	}
	
	private ArrayList<Tile> findNeighbour(Tile nextTile){
		ArrayList<Tile> tiles = new ArrayList<>();
		
		for (int row = -1; row < 2; row++) {
			for (int col = -1; col < 2; col++) {
				if (row == 0 && col == 0){
					continue;
				}else if (!raster[nextTile.getxCoord() + row][nextTile.getyCoord() + col]){
					continue;
				}else{
					Tile tile = new Tile();
					tile.setxCoord(nextTile.getxCoord() + row);
					tile.setyCoord(nextTile.getyCoord() + col);
					tile.setAccessible(raster[nextTile.getxCoord() + row][nextTile.getyCoord() + col]);
					
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
	
	public Tile findLowestWeight(){
		int lowestWeight = Integer.MAX_VALUE;
		Tile tileWithLowestWeight = new Tile();
		for (Tile tile : openList) {
			if (tile.getWeight() < lowestWeight){
				lowestWeight = tile.getWeight();
				tileWithLowestWeight = tile;
			}
		}
		return tileWithLowestWeight;
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
