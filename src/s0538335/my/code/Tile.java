package s0538335.my.code;

public class Tile {
		int xCoord;
		int yCoord;
		Tile prev;
		int weight;
		int heuristicWeight;
		int totalWeight;
		boolean accessible;
		
		
		
		public int getxCoord() {
			return xCoord;
		}
		public void setxCoord(int xCoord) {
			this.xCoord = xCoord;
		}
		
		public int getyCoord() {
			return yCoord;
		}
		public void setyCoord(int yCoord) {
			this.yCoord = yCoord;
		}
		
		public Tile getPrev() {
			return prev;
		}
		public void setPrev(Tile prev) {
			this.prev = prev;
		}
		
		
		public boolean getAccessible(){
			return accessible;
		}
		public void setAccessible(boolean accessible){
			this.accessible = accessible;
		}
		
		public int getWeight() {
			return weight;
		}
		public void setWeight(int weight) {
			this.weight = weight;
		}
		
		public int getHeuristicWeight(){
			return heuristicWeight;
		}
		public void setHeuristicWeight(int heuristicWeight) {
			this.heuristicWeight = heuristicWeight;
		}
		
		public int getTotalWeight(){
			return totalWeight;
		}
		public void setTotalWeight(){
			this.totalWeight = this.weight + this.heuristicWeight;
		}
		
		public void getObject(Tile tile){
			this.xCoord = tile.getxCoord();
			this.yCoord = tile.getyCoord();
			this.accessible = tile.getAccessible();
			this.heuristicWeight = tile.getHeuristicWeight();
			this.weight = tile.getWeight();
			this.totalWeight = tile.getTotalWeight();
			this.prev = tile.getPrev();
		}
		
		public boolean isTheSame(Tile tile){
			if (this.xCoord == tile.getxCoord() && this.yCoord == tile.yCoord){
				return true;
			}
			return false;
		}
}
