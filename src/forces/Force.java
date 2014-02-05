package forces;

import jgame.JGObject;
import nodes.SuperMass;

import org.jbox2d.common.Vec2;

public abstract class Force extends JGObject{
	protected boolean isOn = true; 
	
	public Force() {
		super("force", true, 0, 0, 0, null);
		// TODO Auto-generated constructor stub
	}

	public abstract void calculateForce();
	
	public abstract void toggleForces(int toggle);
}
