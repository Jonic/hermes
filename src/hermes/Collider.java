package src.hermes;

import src.hermes.shape.Shape;

/**
 * Following  to interact two beings
 * detect method determines if two bodies should interact
 * handle "interacts" the two beings by updating them
 */
public abstract class Collider<A extends Being, B extends Being> implements Interactor<A, B>{

	// returns true if A and B collide
	public boolean detect(A being1, B being2) {
		Shape.collide(being1.getShape(), being2.getShape());
		return true;
	}

	// handles an interaction between A and B
	public abstract void handleCollision(A being1, B being2);

}

