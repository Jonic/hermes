package src.hermes;

/**
 * interactor that detects collisions between Beings based on their shape
 */
public abstract class Collider<A extends Being, B extends Being> implements Interactor<A, B>{

	// returns true if A and B collide
	public boolean detect(A being1, B being2) {
		return being1.getShape().collide(being2.getShape());
	}

}

