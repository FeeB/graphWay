package s0538335.my.code;

import java.awt.Point;
import java.awt.geom.Rectangle2D;

public class Tile {

	private int size;
	private int xPositionInRaster;
	private int yPositionInRaster;
	private Tile prev;
	private int weight;
	private int heuristicWeight;
	private int totalWeight;
	private boolean accessible;
	private Rectangle2D.Float asRectangle;

	public Tile(int size, int xPositionInRaster, int yPositionInRaster) {
		this.size = size;
		this.xPositionInRaster = xPositionInRaster;
		this.yPositionInRaster = yPositionInRaster;
		this.asRectangle = new Rectangle2D.Float(xPositionInRaster * size, yPositionInRaster * size, size, size);
	}
	
	public int getXPositionInRaster() {
		return xPositionInRaster;
	}

	public int getYPositionInRaster() {
		return yPositionInRaster;
	}
	
	public float getXCoord() {
		return (xPositionInRaster * size);
	}
	
	public float getYCoord() {
		return (yPositionInRaster * size);
	}
	
	public float getCenterXCoord() {
		return getXCoord() + (size / 2);
	}
	
	public float getCenterYCoord() {
		return getYCoord() + (size / 2);
	}

	public Point getCenterPoint() {
		return new Point(Math.round(getCenterXCoord()), Math.round(getCenterYCoord()));
	}

	public Tile getPrev() {
		return prev;
	}

	public void setPrev(Tile prev) {
		this.prev = prev;
	}

	public boolean getAccessible() {
		return accessible;
	}

	public void setAccessible(boolean accessible) {
		this.accessible = accessible;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getHeuristicWeight() {
		return this.heuristicWeight;
	}

	public void setHeuristicWeight(int heuristicWeight) {
		this.heuristicWeight = heuristicWeight;
	}

	public int getTotalWeight() {
		return totalWeight;
	}

	public void setTotalWeight() {
		this.totalWeight = this.weight + this.heuristicWeight;
	}

	public Tile clone() {
		Tile tile = new Tile(size, xPositionInRaster, yPositionInRaster);
		tile.setPrev(prev);
		tile.setAccessible(accessible);
		tile.setHeuristicWeight(heuristicWeight);
		tile.setWeight(weight);
		return tile;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public boolean hasSamePositionInRaster(Tile tile) {
		if (this.xPositionInRaster == tile.getXPositionInRaster() && this.yPositionInRaster == tile.getYPositionInRaster()) {
			return true;
		}
		return false;
	}
	
	public boolean contains(double x, double y) {
		return asRectangle.contains(x, y);
	}

	public boolean intersects(Rectangle2D carRectangle) {
		return asRectangle.intersects(carRectangle);
	}
	
}
