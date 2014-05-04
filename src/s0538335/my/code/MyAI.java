package s0538335.my.code;

import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D;

import org.lwjgl.opengl.GL11;
import org.omg.PortableInterceptor.DISCARDING;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.Info;

public class MyAI extends AI {
	
	private float acceleration;
	
	private int rotationTime = 2;
	private int wishedTime = 4;
	
	private static final int HEIGHT_LITTLE_RECTANGLE = 20;
	private static final int HEIGHT_MEDIUM_RECTANGLE = 40;
	private static final int HEIGHT_BIG_RECRANGLE = 80;
	
	private static final int WIDTH_LITTLE_RECTANGLE = 5;
	private static final int WIDTH_MEDIUM_RECTANGLE = 10;
	private static final int WIDTH_BIG_RECTANGLE = 5;
	
	private static final int BACK_UP_START = 130;
	private int backUpAmount = 0;

	private Float tooClosePointForBackUp;
	
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
		return flee();
	}

	@Override
	public String getName() {
		return "Autooo";
	}
	
	//Draw line to target
	
	@Override
	public void drawDebugStuff() {
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2f(info.getX(), info.getY());
		GL11.glVertex2f(info.getCurrentCheckpoint().x, info.getCurrentCheckpoint().y);
		GL11.glEnd();
	}
	
	// seek to target
	
	public float seek(){
		
		acceleration = info.getMaxAcceleration();
		
		float newOrientation = getNewOrientation(info.getCurrentCheckpoint().x, info.getCurrentCheckpoint().y);
		
		float difference = newOrientation - info.getOrientation();
//		if (difference >= 1.8 *Math.PI){
//			System.out.println("vorher positiv " + difference);
//			difference = (float) (difference - 1.8 *Math.PI);
//			System.out.println("nachher positiv " + difference);
//		}else if (difference <= -1.8 *Math.PI){
//			System.out.println("vorher negativ " + difference);
//			difference = (float) (difference + 1.8 *Math.PI);
//			System.out.println("nachher negativ " + difference);
//		}

		float wunschdrehGeschwindigkeite = difference * info.getMaxAngularVelocity() / rotationTime ;
		float drehBeschleunigung = (wunschdrehGeschwindigkeite - info.getAngularVelocity()) / wishedTime;
		
		return drehBeschleunigung;
	}
	
	// Flee from a point
	
	public float fleeBack(Point2D.Float obstacles){
		
		wishedTime = 2;
		
		float distanceX = info.getX() - obstacles.x;
		float distanceY = info.getY() - obstacles.y;
		float newOrientation = (float) Math.atan2(distanceY, distanceX);
		
		float difference = newOrientation - info.getOrientation();
		
//		if (difference > 1.8 *Math.PI){
//			System.out.println("vorher positiv " + difference);
//			difference = (float) (difference - 1.8 *Math.PI);
//			System.out.println("nachher positiv " + difference);
//		}else if (difference < -1.8 *Math.PI){
//			System.out.println("vorher negativ " + difference);
//			difference = (float) (difference + 1.8 *Math.PI);
//			System.out.println("nachher negativ " + difference);
//		}
		
		float wunschdrehGeschwindigkeite = difference * info.getMaxAngularVelocity() / rotationTime;
		float drehBeschleunigung = (wunschdrehGeschwindigkeite - info.getAngularVelocity()) / wishedTime;
				
		return drehBeschleunigung;
		
	}
	
	//Check if obstacle is in front of the car
	
	public float flee(){
		
		if (backUpAmount > 0) {
//			System.out.println("Backing up " + backUpAmount);
			backUpAmount--;
			return fleeBack(tooClosePointForBackUp);
		}
		
		Point2D.Float tooClosePoint = createPointForRec(HEIGHT_LITTLE_RECTANGLE);
		Rectangle2D.Float tooClose = createRectangle(HEIGHT_LITTLE_RECTANGLE, tooClosePoint, WIDTH_LITTLE_RECTANGLE);

		Point2D.Float nearPoint = createPointForRec(HEIGHT_MEDIUM_RECTANGLE);
		Rectangle2D.Float recNear = createRectangle(HEIGHT_MEDIUM_RECTANGLE, nearPoint, WIDTH_MEDIUM_RECTANGLE);

		Point2D.Float farPoint = createPointForRec(HEIGHT_BIG_RECRANGLE);
		Rectangle2D.Float recFar = createRectangle(HEIGHT_BIG_RECRANGLE, farPoint, WIDTH_BIG_RECTANGLE);
		
		Polygon[] obstacles = info.getWorld().getObstacles();
		for (Polygon polygon : obstacles) {
			
			//obstacle is too close
			if(polygon.intersects(tooClose)) {
				rotationTime = 4;
				acceleration = -5;
				backUpAmount = BACK_UP_START;
				tooClosePointForBackUp = tooClosePoint;
//				System.out.println("Hindernis zu nah");	
				return fleeBack(tooClosePoint);
			}
			
			//obstacle is near
			else if(polygon.intersects(recNear)) {
//				System.out.println("Hindernis nah");
				acceleration = -2;
				return 0.8f * fleeBack(nearPoint) + 0.2f * seek();
			}
			
			//obstacle is far away
			else if(polygon.intersects(recFar)) {
//				System.out.println("Hindernis weit");
				acceleration = info.getMaxAcceleration()/2;
				return 0.3f * seek() + 0.5f * fleeBack(farPoint);
			}
		}
		
		return seek();
		
	}
	
	//Point in front of the car
	
	public Point2D.Float createPointForRec(int height){
		Point2D.Float point = new Point2D.Float();
		point.x = (float) (info.getX() + height * (Math.cos(info.getOrientation())));
		point.y = (float) (info.getY() + height * Math.sin(info.getOrientation()));
		
		return point;
	}
	
	// Create Rectangle
	
	public Rectangle2D.Float createRectangle(int height, Point2D.Float point, int width){
		Point2D.Float centerPoint = createPointForRec(height/2);
		point.setLocation(point.x - width, point.y - width);
		Rectangle2D.Float rec = new Rectangle2D.Float();
		rec.setFrameFromCenter(centerPoint, point);
		
		return rec;
	}
	
	// Orientation to the target
	
	public float getNewOrientation(float x, float y){
		float distanceX = x - info.getX();
		float distanceY = y - info.getY();
		float newOrientation = (float) Math.atan2(distanceY, distanceX);
		return newOrientation;
	}

}
