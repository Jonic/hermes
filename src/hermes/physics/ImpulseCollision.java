package src.hermes.physics;

import processing.core.*;

/**
 * this class is used to store the data for an impulse-based collision between two beings
 * 	intended for internal use, you do not need to understand or use this class!
 * it will accumulate an impulse for a collision between two beings, and apply it equally to each
 *  when the collision is resolved. 
 * @author Sam
 *
 */
public class ImpulseCollision {

	private MassedBeing _being1, _being2; // the two beings colliding
	private PVector _projection; // the projection vector from being1 to being2
	private PVector _impulse; // the impulse on _being2 from _being1
	
	/**
	 * sets up a collision between
	 * @param _being1
	 * @param _being2
	 */
	public ImpulseCollision(MassedBeing being1, MassedBeing being2) {
		assert being1 != null : "ImpulseCollision contructor: being1 must be a valid being";
		assert being2 != null : "ImpulseCollision contructor: being2 must be a valid being";
		
		_being1 = being1;
		_being2 = being2;
	}
	
	/**
	 * add an impulse to the collision
	 * @param impulse	the impulse to add
	 * @param origin	the origin of the impulse (should be being1 or being2)
	 */
	public void addImpulse(PVector impulse, MassedBeing origin) {
		assert impulse != null : "ImpulseCollision.addImpulse: impulse must be a valid PVector";
		
		if(origin == _being1) 		// if the impulse is from being1
			_impulse.add(impulse); 	// add it
		else if(origin == _being2) 	// if its from being2
			_impulse.sub(impulse); 	// subtract it
		else
			assert false : "ImpulseCollision.addImpulse: origin of collision force must be being1 or being2";
	}
	
}
