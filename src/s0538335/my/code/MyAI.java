package s0538335.my.code;

import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import org.lwjgl.opengl.GL11;
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
	ArrayList<Tile> path = new ArrayList<Tile>();
	
	private int counter;

	public MyAI(Info arg0) {
		super(arg0);
		acceleration = info.getMaxAcceleration();
		createRaster();
		findPath();
	}

	@Override
	public float getAcceleration() {
		return acceleration;
	}

	@Override
	public float getAngularAcceleration() {
		createRaster();
		findPath();
		return seek();
	}

	@Override
	public String getName() {

		return "Autooo";
	}

	// Draw line to target

	@Override
	public void drawDebugStuff() {
		
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
//				GL11.glBegin(GL11.GL_QUADS);
//			    GL11.glVertex3f(x*SIZE, y*SIZE,0.1f);
//			    GL11.glVertex3f(x*SIZE + SIZE, y*SIZE,0.1f);
//			    GL11.glVertex3f(x*SIZE + SIZE, y*SIZE + SIZE,0.1f);
//			    GL11.glVertex3f(x*SIZE, y*SIZE + SIZE,0.1f);
//			    GL11.glEnd();
			}

		}

	}
	
	public void findPath(){		
		Tile startTile = new Tile();
		startTile.setxCoord(Math.round(info.getX()/SIZE));
		startTile.setyCoord(Math.round(info.getY()/SIZE));
		startTile.setAccessible(raster[startTile.getxCoord()][startTile.getyCoord()]);
		startTile.setWeight(0);
		setRightXYCoord(startTile);
		
		Tile targetTile = new Tile();
		targetTile.setxCoord(info.getCurrentCheckpoint().x/SIZE);
		targetTile.setyCoord(info.getCurrentCheckpoint().y/SIZE);
		System.out.println(targetTile.getyCoord());
		targetTile.setAccessible(raster[targetTile.getxCoord()][targetTile.getyCoord()]);
		setRightXYCoord(targetTile);
		System.out.println(targetTile.getyCoord());
		
		//first add startTile to openList
		openList.add(startTile);
		
		
		while(!openList.isEmpty()){
		
			Tile actualTile = findLowestWeight();
			openList.remove(actualTile);
			closeList.add(actualTile);
			
			if(actualTile.isTheSame(targetTile)){
				targetTile.setPrev(actualTile.getPrev());
				break;
			}
			
			ArrayList<Tile> neighbours = findNeighbour(actualTile, targetTile);
			 
			for (Tile tile : neighbours) {
				if (tile.accessible && !listContainsTile(closeList, tile)){
					if(!listContainsTile(openList, tile)){
						openList.add(tile);
						tile.setPrev(actualTile);
					}else{
						if(getEqualTile(openList, tile).getWeight() > tile.getWeight()){
							getEqualTile(openList, tile).setWeight(tile.getWeight());
							getEqualTile(openList, tile).setPrev(actualTile);
							getEqualTile(openList, tile).setTotalWeight();
						}
					}
					
				}
			}
		}
		storePath(startTile, targetTile);
	}
	
	public void storePath(Tile startTile, Tile targetTile){
		Tile actualTile = new Tile();
		actualTile.getObject(targetTile);
				
		while (!actualTile.isTheSame(startTile)){
			path.add(actualTile);
			actualTile = actualTile.getPrev();
		}
		counter = path.size();
	}
	
	private ArrayList<Tile> findNeighbour(Tile actualTile, Tile targetTile){
		ArrayList<Tile> tiles = new ArrayList<>();
		
		for (int row = -1; row < 2; row++) {
			for (int col = -1; col < 2; col++) {
				if (row == 0 && col == 0 || actualTile.getxCoord() + row * SIZE < 0 || actualTile.getyCoord() + col * SIZE < 0
						|| actualTile.getxCoord() + row * SIZE >= info.getWorld().getWidth() / SIZE || actualTile.getyCoord() + col * SIZE >= info.getWorld().getHeight() / SIZE){
					continue;
				}else if (!raster[actualTile.getxCoord() + row * SIZE][actualTile.getyCoord() + col * SIZE]){
					continue;
				}else{
					Tile tile = new Tile();
					tile.setxCoord(actualTile.getxCoord() + row * SIZE);
					tile.setyCoord(actualTile.getyCoord() + col * SIZE);
					tile.setAccessible(raster[actualTile.getxCoord() + row * SIZE][actualTile.getyCoord() + col * SIZE]);
					
					int dist = (int) Math.sqrt(Math.pow(targetTile.getxCoord() - tile.getxCoord(), 2) + Math.pow(targetTile.getyCoord() - tile.getyCoord(), 2));
					tile.setHeuristicWeight(dist);
					
					if(Math.abs(row)==1 && Math.abs(col)==1){
						tile.setWeight(14);
					}else{
						tile.setWeight(10);
					}
					
					tile.setTotalWeight();
					
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
			if (tile.getTotalWeight() < lowestWeight){
				lowestWeight = tile.getTotalWeight();
				tileWithLowestWeight = tile;
			}
		}
		return tileWithLowestWeight;
	}
	
	public boolean listContainsTile(ArrayList<Tile> list, Tile tile){
		for (Tile existingTile : list) {
			if(tile.getxCoord() == existingTile.getxCoord() && tile.getyCoord() == existingTile.getyCoord()) {
				return true;
			}
		}
		return false;	
	}
	
	public Tile getEqualTile(ArrayList<Tile> list, Tile tile){
		for (Tile existingTile : list) {
			if(tile.getxCoord() == existingTile.getxCoord() && tile.getyCoord() == existingTile.getyCoord()) {
				return existingTile;
			}
		}
		return null;
	}
	
	public void setRightXYCoord(Tile tile){
		int x = (tile.getxCoord()/10) * 10;
		int y = (tile.getyCoord()/10) * 10;
		
		tile.setxCoord(x);
		tile.setyCoord(y);
	}
	
	
	// seek to target

	public float seek() {

//		acceleration = info.getMaxAcceleration();
//		int actualX = (int) (((info.getX() / SIZE) / 100) * 10);
//		int actualY = (int) (((info.getY() / SIZE) / 100) * 10);
//		int targetX = (int) (((info.getCurrentCheckpoint().x / SIZE) / 10) * 10);
//		int targetY = (int) (((info.getCurrentCheckpoint().y / SIZE) / 10) * 10);
//		Rectangle2D.Float recActualPos = new Rectangle2D.Float(actualX, actualY, SIZE, SIZE);
//		Rectangle2D.Float recTarget = new Rectangle2D.Float(targetX, targetY, SIZE, SIZE);
//		
//		
//		
//		while(!recActualPos.intersects(recTarget)){
		
		
		float newOrientation = getNewOrientation(path.get(counter).getxCoord(), path.get(counter).getxCoord());

		float difference = newOrientation - info.getOrientation();

		float wunschdrehGeschwindigkeite = difference * info.getMaxAngularVelocity() / rotationTime;
		float drehBeschleunigung = (wunschdrehGeschwindigkeite - info.getAngularVelocity()) / wishedTime;

		counter--;
		if(counter < 0){
			findPath();
		}
		
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
