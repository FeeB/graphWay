package s0538335.my.code;

public class Tile {
		int xCoord;
		int yCoord;
		Tile prev;
		int weight;
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
		
		public int getWeight() {
			return weight;
		}
		public void setWeight(int weight) {
			this.weight = weight;
		}
		
		public boolean getAccessible(){
			return accessible;
		}
		public void setAccessible(boolean accessible){
			this.accessible = accessible;
		}
		
		
}
