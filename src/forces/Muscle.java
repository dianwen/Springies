package forces;

import jgame.JGColor;

import org.jbox2d.common.Vec2;

import nodes.SuperMass;

public class Muscle extends Force {

	private float restLength;
	private float constant;
	private SuperMass massA;
	private SuperMass massB; 
	private float amplitude;
	private float time;

	public Muscle(SuperMass a, SuperMass b, float rl, float c, float amp) {
		massA = a;
		massB = b;
		restLength = rl;
		constant = c;	 
		amplitude = amp;
	}
	
	@Override
	public void calculateForce() {
		time+=0.15;
		float newRestLength = (float) (restLength + amplitude*Math.sin(time));
		
		Vec2 locA = massA.getPos();
		Vec2 locB = massB.getPos();
		double distance = findDistance(locA,locB);
		
		massA.setForce(constant*(distance-newRestLength)*(locB.x-locA.x)/distance, constant*(distance-newRestLength)*(locB.y-locA.y)/distance);
		massB.setForce(-constant*(distance-newRestLength)*(locB.x-locA.x)/distance, -constant*(distance-newRestLength)*(locB.y-locA.y)/distance);
	}
	
	public double findDistance(Vec2 a, Vec2 b){
		return Math.sqrt((a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y));
	}

	@Override
	public void paint(){
		eng.drawLine(massA.getPos().x, massA.getPos().y, massB.getPos().x, massB.getPos().y, 1, JGColor.gray);
	}
}
