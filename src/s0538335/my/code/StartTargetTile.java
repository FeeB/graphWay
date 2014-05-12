package s0538335.my.code;

public class StartTargetTile extends Tile{
	
	private int xCoord;
	private int yCoord;

	public StartTargetTile(int size, int xPositionInRaster, int yPositionInRaster) {
		super(size, xPositionInRaster, yPositionInRaster);
		// TODO Auto-generated constructor stub
	}

	public float getXCoord() {
		return xCoord;
	}

	public void setXCoord(int xCoord) {
		this.xCoord = xCoord;
	}

	public float getYCoord() {
		return yCoord;
	}

	public void setYCoord(int yCoord) {
		this.yCoord = yCoord;
	}
	
	public StartTargetTile clone() {
		StartTargetTile tile = new StartTargetTile(getSize(), getXPositionInRaster(), getYPositionInRaster());
		tile.setPrev(getPrev());
		tile.setAccessible(getAccessible());
		tile.setHeuristicWeight(getHeuristicWeight());
		tile.setWeight(getWeight());
		tile.setXCoord(xCoord);
		tile.setYCoord(yCoord);
		return tile;
	}
	
	

}
