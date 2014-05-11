package s0538335.my.code;

import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.Info;
import s0538335.my.code.Tile;

public class MyAI extends AI {

	private static final int TILE_SIZE = 10;

	private float acceleration;
	
	private static final float BRAKE_ANGLE = (float) 0.8;
	private static final float PREFFERED_TIME = (float) 0.7;

	private boolean[][] raster = new boolean[info.getWorld().getWidth() / TILE_SIZE][info.getWorld().getHeight() / TILE_SIZE];

	private ArrayList<Tile> openList = new ArrayList<Tile>();
	private ArrayList<Tile> closeList = new ArrayList<Tile>();
	private ArrayList<Tile> path = new ArrayList<Tile>();

	private int currentTileIndex;
	private Tile currentTile;

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
		return align(seek());
	}

	@Override
	public String getName() {
		return "Autooo";
	}

	// Draw line to target

	@Override
	public void drawDebugStuff() {
		for (Tile tile : path) {
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex3f(tile.getXCoord(), tile.getYCoord(), 0.1f);
			GL11.glVertex3f(tile.getXCoord() + TILE_SIZE, tile.getYCoord(), 0.1f);
			GL11.glVertex3f(tile.getXCoord() + TILE_SIZE, tile.getYCoord() + TILE_SIZE, 0.1f);
			GL11.glVertex3f(tile.getXCoord(), tile.getYCoord() + TILE_SIZE, 0.1f);
			GL11.glEnd();
		}
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(info.getX(), info.getY());
		GL11.glVertex2f(currentTile.getCenterXCoord(), currentTile.getCenterYCoord());
		GL11.glEnd();
	}

	public void createRaster() {

		Polygon[] obstacle = info.getWorld().getObstacles();

		for (int x = 0; x < info.getWorld().getWidth() / TILE_SIZE; x++) {
			for (int y = 0; y < info.getWorld().getHeight() / TILE_SIZE; y++) {
				raster[x][y] = true;
				Rectangle2D.Float rec = new Rectangle2D.Float(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
				for (Polygon obstaclePoint : obstacle) {
					if (obstaclePoint.intersects(rec)) {
						raster[x][y] = false;
					}

				}
				// GL11.glBegin(GL11.GL_QUADS);
				// GL11.glVertex3f(x*SIZE, y*SIZE,0.1f);
				// GL11.glVertex3f(x*SIZE + SIZE, y*SIZE,0.1f);
				// GL11.glVertex3f(x*SIZE + SIZE, y*SIZE + SIZE,0.1f);
				// GL11.glVertex3f(x*SIZE, y*SIZE + SIZE,0.1f);
				// GL11.glEnd();
			}
		}
	}

	public void findPath() {
		System.out.println("x: "+ info.getX() + " y: " + info.getY());
		System.out.println("x in raster: " + info.getX() /TILE_SIZE + " y in raster: " + info.getY() / TILE_SIZE);
		Tile startTile = new Tile(TILE_SIZE, matchRaster(info.getX() / TILE_SIZE), matchRaster(info.getY() / TILE_SIZE));
		startTile.setAccessible(raster[startTile.getXPositionInRaster()][startTile.getYPositionInRaster()]);

		Tile targetTile = new Tile(TILE_SIZE, matchRaster(info.getCurrentCheckpoint().x / TILE_SIZE), matchRaster(info.getCurrentCheckpoint().y / TILE_SIZE));
		targetTile.setAccessible(raster[targetTile.getXPositionInRaster()][targetTile.getYPositionInRaster()]);

		// first add startTile to openList
		closeList.clear();
		openList.clear();
		openList.add(startTile);

		while (!openList.isEmpty()) {

			Tile currentTile = findLowestWeight();
			openList.remove(currentTile);
			closeList.add(currentTile);

			if (currentTile.hasSamePositionInRaster(targetTile)) {
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
		storePath(startTile, targetTile);
	}

	public void storePath(Tile startTile, Tile targetTile) {
		Tile actualTile = targetTile.clone();

		while (!actualTile.hasSamePositionInRaster(startTile)) {
			path.add(actualTile);
			actualTile = actualTile.getPrev();
		}
		currentTileIndex = path.size() - 1;
		currentTile = path.get(currentTileIndex);
	}

	private ArrayList<Tile> findNeighbour(Tile actualTile, Tile targetTile) {
		ArrayList<Tile> tiles = new ArrayList<>();

		for (int row = -1; row < 2; row++) {
			for (int col = -1; col < 2; col++) {
				if ((row == 0 && col == 0)
						|| actualTile.getXPositionInRaster() + row * TILE_SIZE < 0
						|| actualTile.getYPositionInRaster() + col * TILE_SIZE < 0
						|| actualTile.getXPositionInRaster() + row * TILE_SIZE >= info.getWorld().getWidth() / TILE_SIZE 
						|| actualTile.getYPositionInRaster() + col * TILE_SIZE >= info.getWorld().getHeight() / TILE_SIZE) {
					continue;
				} else if (!raster[actualTile.getXPositionInRaster() + row * TILE_SIZE][actualTile.getYPositionInRaster() + col * TILE_SIZE]) {
					continue;
				} else {
					Tile tile = new Tile(TILE_SIZE, actualTile.getXPositionInRaster() + row, actualTile.getYPositionInRaster() + col);
					tile.setAccessible(raster[actualTile.getXPositionInRaster() + row][actualTile.getYPositionInRaster() + col]);

					int dist = (int) Math.sqrt(Math.pow(targetTile.getXPositionInRaster() - tile.getXPositionInRaster(), 2) + Math.pow(targetTile.getYPositionInRaster() - tile.getYPositionInRaster(), 2));
					tile.setHeuristicWeight(dist);

					if (Math.abs(row) == 1 && Math.abs(col) == 1) {
						tile.setWeight(14);
					} else {
						tile.setWeight(10);
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

	private int matchRaster(float value) {
		return Math.round(value / TILE_SIZE) * TILE_SIZE;
	}

	// seek to target

	private float align(Vector2f target) {
		double targetOrientation = Math.atan2(target.y, target.x);
		float difference = (float) targetOrientation - info.getOrientation();
		float prefferedRotaionSpeed = difference * info.getMaxAngularAcceleration() / BRAKE_ANGLE;
		float acceleration = prefferedRotaionSpeed - info.getAngularVelocity() / PREFFERED_TIME;
		return acceleration;
	}

	private Vector2f seek() {
		nextTileOrNewPath();
		return new Vector2f(currentTile.getCenterXCoord() - info.getX(), currentTile.getCenterYCoord() - info.getY());
	}

	private void nextTileOrNewPath() {
		if (currentTile.contains(info.getX(), info.getY())) {
			if (currentTileIndex == 0) {
				System.out.println("Pfad neu berechnen");
				findPath();
			} else {
				System.out.println("Neues Teilstück wird angefahren");
				currentTileIndex--;
				currentTile = path.get(currentTileIndex);
			}
		}
	}

	// Orientation to the target

	public float getNewOrientation(float x, float y) {
		float distanceX = x - info.getX();
		float distanceY = y - info.getY();
		float newOrientation = (float) Math.atan2(distanceY, distanceX);
		return newOrientation;
	}

}
