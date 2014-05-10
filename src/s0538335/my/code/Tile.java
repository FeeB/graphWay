package s0538335.my.code;

import java.awt.geom.Rectangle2D;

public class Tile {

	private int size;
	private int x;
	private int y;
	private Tile prev;
	private int weight;
	private int heuristicWeight;
	private int totalWeight;
	private boolean accessible;
	private Rectangle2D.Float asRectangle;

	public Tile(int size, int x, int y) {
		this.size = size;
		this.x = x;
		this.y = y;
		this.asRectangle = new Rectangle2D.Float(x * size, y * size, size, size);
	}
	
	public int getxCoord() {
		return x;
	}

	public int getyCoord() {
		return y;
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
		Tile tile = new Tile(size, x, y);
		tile.setPrev(prev);
		tile.setAccessible(accessible);
		tile.setHeuristicWeight(heuristicWeight);
		tile.setWeight(weight);
		return tile;
	}

	public boolean isTheSame(Tile tile) {
		if (this.x == tile.getxCoord() && this.y == tile.y) {
			return true;
		}
		return false;
	}
	
	public boolean contains(double x, double y) {
		return asRectangle.contains(x, y);
	}
	
}
