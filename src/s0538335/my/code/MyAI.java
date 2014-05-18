package s0538335.my.code;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.Info;
import s0538335.my.code.Tile;

public class MyAI extends AI {
	
	private static final boolean DEBUG = true;
	
	private static final float BRAKE_THRESHOLD = 0.4f;
	private static final int BRAKE_ACCELERATION = 3;
	
	private static final int TILE_SIZE = 20;
	private static final float CAR_SIZE = TILE_SIZE * 1.5f;
	
	private static final float BRAKE_ANGLE = 0.4f;
	private static final float PREFFERED_TIME = 0.7f;

	private boolean[][] raster = new boolean[info.getWorld().getWidth() / TILE_SIZE][info.getWorld().getHeight() / TILE_SIZE];

	private ArrayList<Tile> openList = new ArrayList<Tile>();
	private ArrayList<Tile> closeList = new ArrayList<Tile>();
	private ArrayList<Tile> path = new ArrayList<Tile>();

	private int currentTileIndex;
	private Tile currentTile;

	private Tile targetTile;
	private Tile startTile;

	private Rectangle2D carRectangle;
	private float difference;

	public MyAI(Info info) {
		super(info);
		createRaster();
		findAndStorePath();
	}

	private void findAndStorePath() {
		findPath();
		storePath();
		setStartTile();
	}
	
	@Override
	public float getAcceleration() {
		if (Math.abs(difference) > BRAKE_THRESHOLD) {
			System.out.println("LANGSSAAAM");
			return BRAKE_ACCELERATION;
		} else {
			return info.getMaxAcceleration();			
		}
	}

	@Override
	public float getAngularAcceleration() {
		nextTileOrNewPath();

		if (currentTileIndex == 0) {
			return align(info.getCurrentCheckpoint());
		} else {
			return align(currentTile.getCenterPoint());
		}
	}

	@Override
	public String getName() {
		return "Autooo";
	}

	// Draw line to target

	@Override
	public void drawDebugStuff() {
		if (!DEBUG) {
			return;
		}
		
		drawTile(startTile, 0, 0, 1);

		for (int i = 0; i < path.size(); i++) {
			Tile tile = path.get(i);
			if (i > currentTileIndex) {
				drawTile(tile, 1, 0, 0);
			} else {
				drawTile(tile, 0, 1, 0);
			}
		}

		drawTile(targetTile, 1.0f, 0, 1.0f);

		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex3d(info.getCurrentCheckpoint().x, info.getCurrentCheckpoint().y, 0.1f);
		GL11.glVertex3d(info.getCurrentCheckpoint().x + TILE_SIZE, info.getCurrentCheckpoint().y, 0.1f);
		GL11.glVertex3d(info.getCurrentCheckpoint().x + TILE_SIZE, info.getCurrentCheckpoint().y + TILE_SIZE, 0.1f);
		GL11.glVertex3d(info.getCurrentCheckpoint().x, info.getCurrentCheckpoint().y + TILE_SIZE, 0.1f);
		GL11.glEnd();

		GL11.glColor3f(0, 1.0f, 1.0f);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3f(info.getX(), info.getY(), 0.2f);
		GL11.glVertex3f(currentTile.getCenterXCoord(), currentTile.getCenterYCoord(), 0.2f);
		GL11.glEnd();

		if (carRectangle != null) {
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex3d(carRectangle.getX(), carRectangle.getY(), 0.1f);
			GL11.glVertex3d(carRectangle.getX() + carRectangle.getWidth(), carRectangle.getY(), 0.1f);
			GL11.glVertex3d(carRectangle.getX() + carRectangle.getWidth(), carRectangle.getY() + carRectangle.getWidth(), 0.1f);
			GL11.glVertex3d(carRectangle.getX(), carRectangle.getY() + carRectangle.getWidth(), 0.1f);
			GL11.glEnd();
		}
	}

	private void drawTile(Tile tile, float red, float green, float blue) {
		GL11.glColor3f(red, green, blue);				
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex3f(tile.getXCoord(), tile.getYCoord(), 0.1f);
		GL11.glVertex3f(tile.getXCoord() + TILE_SIZE, tile.getYCoord(), 0.1f);
		GL11.glVertex3f(tile.getXCoord() + TILE_SIZE, tile.getYCoord() + TILE_SIZE, 0.1f);
		GL11.glVertex3f(tile.getXCoord(), tile.getYCoord() + TILE_SIZE, 0.1f);
		GL11.glEnd();
	}
	
	public void createRaster() {

		Polygon[] obstacles = info.getWorld().getObstacles();
		System.out.println(info.getWorld().getHeight());
		System.out.println(info.getWorld().getWidth());

		for (int x = 0; x < info.getWorld().getWidth() / TILE_SIZE; x++) {
			for (int y = 0; y < info.getWorld().getHeight() / TILE_SIZE; y++) {
				raster[x][y] = true;
				Rectangle2D.Float rec = new Rectangle2D.Float(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
				for (Polygon obstacle : obstacles) {
					if (obstacle.intersects(rec)) {
						raster[x][y] = false;
					}

				}
			}
		}
	}

	public void findPath() {
		System.out.println("x: "+ info.getX() + " y: " + info.getY());
		System.out.println("x in raster: " + info.getX() /TILE_SIZE + " y in raster: " + info.getY() / TILE_SIZE);
		System.out.println(raster.length);
		startTile = new Tile(TILE_SIZE, (int) (info.getX() / TILE_SIZE), (int) (info.getY() / TILE_SIZE));
		startTile.setAccessible(raster[startTile.getXPositionInRaster()][startTile.getYPositionInRaster()]);

		targetTile = new Tile(TILE_SIZE, info.getCurrentCheckpoint().x / TILE_SIZE, info.getCurrentCheckpoint().y / TILE_SIZE);
		System.out.println("x in coord: " + info.getCurrentCheckpoint().x + " y in coord: " + info.getCurrentCheckpoint().y);
		System.out.println("target x: " + targetTile.getXPositionInRaster());
		System.out.println("target y: " + targetTile.getYPositionInRaster());
		System.out.println("without match x: " + info.getCurrentCheckpoint().x / TILE_SIZE + " without match y: " + info.getCurrentCheckpoint().y / TILE_SIZE);
		System.out.println("raster x länge: " + raster.length);
		System.out.println("raster y länge: " + raster[1].length);
		targetTile.setAccessible(raster[targetTile.getXPositionInRaster()][targetTile.getYPositionInRaster()]);

		// first add startTile to openList
		path.clear();
		closeList.clear();
		openList.clear();
		if (closeList.isEmpty()){
			System.out.println("closelist is clear");
		}
		if (openList.isEmpty()){
			System.out.println("openlist is clear");
		}
		openList.add(startTile);

		while (!openList.isEmpty()) {

			Tile currentTile = findLowestWeight();
			openList.remove(currentTile);
			closeList.add(currentTile);

			if (currentTile.contains(info.getCurrentCheckpoint().getX(), info.getCurrentCheckpoint().getY())) {
				targetTile.setPrev(currentTile.getPrev());
				break;
			}

			ArrayList<Tile> neighbours = findNeighbour(currentTile, targetTile);

			for (Tile tile : neighbours) {
				if (tile.getAccessible() && !listContainsTile(closeList, tile)) {
					if (!listContainsTile(openList, tile)) {
						tile.setPrev(currentTile);
						openList.add(tile);
					} else if (getEqualTile(openList, tile).getWeight() > tile.getWeight()) {
						getEqualTile(openList, tile).setWeight(tile.getWeight());
						getEqualTile(openList, tile).setPrev(currentTile);
						getEqualTile(openList, tile).setTotalWeight();
					}
				}
			}
		}
		System.out.println(startTile.getXCoord());
		System.out.println(startTile.getYCoord());
	}

	public void storePath() {
		if (targetTile.hasSamePositionInRaster(startTile)){
			path.add(targetTile);
		}

		Tile tile = targetTile.clone();
		while (!tile.hasSamePositionInRaster(startTile)) {
			path.add(tile);
			tile = tile.getPrev();
		}
	}

	private void setStartTile() {
		currentTileIndex = path.size() - 1;
		currentTile = path.get(currentTileIndex);
	}

	private ArrayList<Tile> findNeighbour(Tile actualTile, Tile targetTile) {
		ArrayList<Tile> tiles = new ArrayList<>();

		for (int row = -1; row < 2; row++) {
			for (int col = -1; col < 2; col++) {
				if ((row == 0 && col == 0)
						|| actualTile.getXPositionInRaster() + row < 0
						|| actualTile.getYPositionInRaster() + col < 0
						|| actualTile.getXPositionInRaster() + row >= info.getWorld().getWidth() / TILE_SIZE 
						|| actualTile.getYPositionInRaster() + col >= info.getWorld().getHeight() / TILE_SIZE) {
					continue;
				} else if (!raster[actualTile.getXPositionInRaster() + row][actualTile.getYPositionInRaster() + col]) {
					continue;
				} else {
					Tile tile = new Tile(TILE_SIZE, actualTile.getXPositionInRaster() + row, actualTile.getYPositionInRaster() + col);
					tile.setAccessible(raster[actualTile.getXPositionInRaster() + row][actualTile.getYPositionInRaster() + col]);

					int dist = (int) Math.sqrt(Math.pow(targetTile.getXCoord() - tile.getXCoord(), 2) + Math.pow(targetTile.getYCoord() - tile.getYCoord(), 2));
					tile.setHeuristicWeight(dist);

					if (Math.abs(row) == 1 && Math.abs(col) == 1) {
						tile.setWeight(1);
					} else {
						tile.setWeight(1);
					}

					tile.setTotalWeight();

					tiles.add(tile);
				}
			}
		}
		return tiles;

	}

	public Tile findLowestWeight() {
		int lowestWeight = Integer.MAX_VALUE;
		Tile tileWithLowestWeight = null;
		for (Tile tile : openList) {
			if (tile.getTotalWeight() < lowestWeight) {
				lowestWeight = tile.getTotalWeight();
				tileWithLowestWeight = tile;
			}
		}
		return tileWithLowestWeight;
	}

	public boolean listContainsTile(ArrayList<Tile> list, Tile tile) {
		for (Tile existingTile : list) {
			if (tile.getXPositionInRaster() == existingTile.getXPositionInRaster() && tile.getYPositionInRaster() == existingTile.getYPositionInRaster()) {
				return true;
			}
		}
		return false;
	}

	public Tile getEqualTile(ArrayList<Tile> list, Tile tile) {
		for (Tile existingTile : list) {
			if (tile.getXPositionInRaster() == existingTile.getXPositionInRaster() && tile.getYPositionInRaster() == existingTile.getYPositionInRaster()) {
				return existingTile;
			}
		}
		return null;
	}

	// seek to target

	private float align(Point target) {
		float targetOrientation = (float) Math.atan2(target.y - info.getY(), target.x - info.getX());
		
		difference = targetOrientation - info.getOrientation();
		if (Math.abs(difference) > Math.PI){
			difference = difference * -1;
		}
		float preferredRotaionSpeed = difference * info.getMaxAngularAcceleration() / BRAKE_ANGLE;
		float acceleration = preferredRotaionSpeed - info.getAngularVelocity() / PREFFERED_TIME;
		return acceleration;
	}

	private void nextTileOrNewPath() {
		
		carRectangle = new Rectangle2D.Float(info.getX() - CAR_SIZE / 2, info.getY() - CAR_SIZE / 2f, CAR_SIZE, CAR_SIZE);

		if (currentTile.intersects(carRectangle)) {
			if (currentTileIndex == 0) {
				System.out.println("Pfad neu berechnen");
				findAndStorePath();
			} else {
				System.out.println("Neues Teilstück wird angefahren");
				currentTileIndex--;
				currentTile = path.get(currentTileIndex);
			}
		}
	}
}
