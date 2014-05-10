package s0538335.my.code;

import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.Info;
import s0538335.my.code.Tile;

public class MyAI extends AI {

	private static final int TILE_SIZE = 20;

	private float acceleration;

	private int rotationTime = 2;
	private int wishedTime = 4;

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
		return seek();
	}

	@Override
	public String getName() {
		return "Autooo";
	}

	// Draw line to target

	@Override
	public void drawDebugStuff() {

		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex3f(currentTile.getxCoord() * TILE_SIZE, currentTile.getyCoord() * TILE_SIZE, 0.1f);
		GL11.glVertex3f(currentTile.getxCoord() * TILE_SIZE + TILE_SIZE, currentTile.getyCoord() * TILE_SIZE, 0.1f);
		GL11.glVertex3f(currentTile.getxCoord() * TILE_SIZE + TILE_SIZE, currentTile.getyCoord() * TILE_SIZE + TILE_SIZE, 0.1f);
		GL11.glVertex3f(currentTile.getxCoord() * TILE_SIZE, currentTile.getyCoord() * TILE_SIZE + TILE_SIZE, 0.1f);
		GL11.glEnd();

		GL11.glBegin(GL11.GL_LINE);
		GL11.glVertex2f(info.getX(), info.getY());
		GL11.glVertex2f(currentTile.getxCoord() * TILE_SIZE, currentTile.getxCoord() * TILE_SIZE);
		// System.out.println(counter);
		// System.out.println(path.get(counter-1).getxCoord() * SIZE);
		// System.out.println(path.get(counter-1).getxCoord() * SIZE);
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
		Tile startTile = new Tile(TILE_SIZE, matchRaster(Math.round(info.getX() / TILE_SIZE)), matchRaster(Math.round(info.getY() / TILE_SIZE)));
		startTile.setAccessible(raster[startTile.getxCoord()][startTile.getyCoord()]);

		Tile targetTile = new Tile(TILE_SIZE, matchRaster(info.getCurrentCheckpoint().x / TILE_SIZE), matchRaster(info.getCurrentCheckpoint().y / TILE_SIZE));
		targetTile.setAccessible(raster[targetTile.getxCoord()][targetTile.getyCoord()]);

		// first add startTile to openList
		closeList.clear();
		openList.clear();
		openList.add(startTile);

		while (!openList.isEmpty()) {

			Tile actualTile = findLowestWeight();
			openList.remove(actualTile);
			closeList.add(actualTile);

			if (actualTile.isTheSame(targetTile)) {
				targetTile.setPrev(actualTile.getPrev());
				break;
			}

			ArrayList<Tile> neighbours = findNeighbour(actualTile, targetTile);

			for (Tile tile : neighbours) {
				if (tile.getAccessible() && !listContainsTile(closeList, tile)) {
					if (!listContainsTile(openList, tile)) {
						tile.setPrev(actualTile);
						openList.add(tile);
					} else if (getEqualTile(openList, tile).getWeight() > tile.getWeight()) {
						getEqualTile(openList, tile).setWeight(tile.getWeight());
						getEqualTile(openList, tile).setPrev(actualTile);
						getEqualTile(openList, tile).setTotalWeight();
					}
				}
			}
		}
		storePath(startTile, targetTile);
	}

	public void storePath(Tile startTile, Tile targetTile) {
		Tile actualTile = targetTile.clone();

		while (!actualTile.isTheSame(startTile)) {
			path.add(actualTile);
			actualTile = actualTile.getPrev();
		}
		currentTileIndex = 0;
		currentTile = path.get(currentTileIndex);
	}

	private ArrayList<Tile> findNeighbour(Tile actualTile, Tile targetTile) {
		ArrayList<Tile> tiles = new ArrayList<>();

		for (int row = -1; row < 2; row++) {
			for (int col = -1; col < 2; col++) {
				if ((row == 0 && col == 0)
						|| actualTile.getxCoord() + row * TILE_SIZE < 0
						|| actualTile.getyCoord() + col * TILE_SIZE < 0
						|| actualTile.getxCoord() + row * TILE_SIZE >= info.getWorld().getWidth() / TILE_SIZE 
						|| actualTile.getyCoord() + col * TILE_SIZE >= info.getWorld().getHeight() / TILE_SIZE) {
					continue;
				} else if (!raster[actualTile.getxCoord() + row * TILE_SIZE][actualTile.getyCoord() + col * TILE_SIZE]) {
					continue;
				} else {
					Tile tile = new Tile(TILE_SIZE, actualTile.getxCoord() + row * TILE_SIZE, actualTile.getyCoord() + col * TILE_SIZE);
					tile.setAccessible(raster[actualTile.getxCoord() + row * TILE_SIZE][actualTile.getyCoord() + col * TILE_SIZE]);

					int dist = (int) Math.sqrt(Math.pow(targetTile.getxCoord() - tile.getxCoord(), 2) + Math.pow(targetTile.getyCoord() - tile.getyCoord(), 2));
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
			if (tile.getxCoord() == existingTile.getxCoord() && tile.getyCoord() == existingTile.getyCoord()) {
				return true;
			}
		}
		return false;
	}

	public Tile getEqualTile(ArrayList<Tile> list, Tile tile) {
		for (Tile existingTile : list) {
			if (tile.getxCoord() == existingTile.getxCoord() && tile.getyCoord() == existingTile.getyCoord()) {
				return existingTile;
			}
		}
		return null;
	}

	private int matchRaster(int value) {
		return (value / TILE_SIZE) * TILE_SIZE;
	}

	// seek to target

	public float seek() {

		float newOrientation = getNewOrientation(currentTile.getxCoord() * TILE_SIZE, currentTile.getxCoord() * TILE_SIZE);

		float difference = newOrientation - info.getOrientation();

		float preferedRotarySpeed = difference * info.getMaxAngularVelocity() / rotationTime;
		float rotaryAcceleration = (preferedRotarySpeed - info.getAngularVelocity()) / wishedTime;

		if (currentTile.contains(info.getX(), info.getY()) && currentTile.contains(info.getCurrentCheckpoint().getX(), info.getCurrentCheckpoint().getY())) {
			findPath();
		} else if (currentTile.contains(info.getX(), info.getY())) {
			currentTileIndex++;
			currentTile = path.get(currentTileIndex);
		}
		
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex3f(currentTile.getxCoord() * TILE_SIZE, currentTile.getyCoord() * TILE_SIZE, 0.1f);
		GL11.glVertex3f(currentTile.getxCoord() * TILE_SIZE + TILE_SIZE, currentTile.getyCoord() * TILE_SIZE, 0.1f);
		GL11.glVertex3f(currentTile.getxCoord() * TILE_SIZE + TILE_SIZE, currentTile.getyCoord() * TILE_SIZE + TILE_SIZE, 0.1f);
		GL11.glVertex3f(currentTile.getxCoord() * TILE_SIZE, currentTile.getyCoord() * TILE_SIZE + TILE_SIZE, 0.1f);
		GL11.glEnd();
		
		return rotaryAcceleration;
		

	}

	// Orientation to the target

	public float getNewOrientation(float x, float y) {
		float distanceX = x - info.getX();
		float distanceY = y - info.getY();
		float newOrientation = (float) Math.atan2(distanceY, distanceX);
		return newOrientation;
	}

}
