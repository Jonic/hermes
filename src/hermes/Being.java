package src.hermes;

import java.util.Iterator;
import java.util.LinkedList;

import src.hermes.postoffice.*;
import src.hermes.shape.Rectangle;
import src.hermes.shape.Shape;
import processing.core.*;

/**
 * Basic game object class
 * Anything that is getting drawn or interacting with other game objects is a Being
 */
public abstract class Being implements KeySubscriber, MouseSubscriber, MouseWheelSubscriber, OscSubscriber {

	protected PVector _position; // the being's position
	protected PVector _velocity; // the being's velocity
	
	protected Shape _shape; 		 // the being's shape
	
	private boolean _done = true;
	protected long _time;
	
	private LinkedList<GenericGroup<?,?>> _groups;
	
	public Being(Shape collisionShape, PVector velocity) {

		initialize(collisionShape, velocity);

	}


	/**
	 * Alternate constructor that defaults to 0 velocity. 
	 * <br><br>When a Being's velocity in 0, the World that it is in won't'update the Being's position automatically. Thus, all motion must be handled through setX, setY, or setPosition
	 * <br><br>NOTE: If this Being's velocity is later set to something other than 0, it will then have its position updated automatically
	 * @param collisionShape		Shape used to determine Being's position and collision detection
	 */
	public Being(Shape collisionShape) { 
		//Defaults to 0 velocity, so update won't move this Being
		initialize(collisionShape, new PVector(0,0));
	}
	
	
	private void initialize(Shape collisionShape, PVector velocity) {
		assert collisionShape != null : "Being constructor: shape must be a valid Shape";
		assert velocity != null : "Being constructor: velocity must be a valid PVector";
		
		_groups = new LinkedList<GenericGroup<?,?>>();
		_shape = collisionShape;
		_position = collisionShape.getPosition();
		_velocity = velocity;
		_time = System.nanoTime();
		
	}
	
	
	
	protected long updateTime() { 
		long time = System.nanoTime();
		long elapsed = time - _time;
		if(elapsed > 0) 
			_time = time;
		return elapsed;
	}
	
	/**
	 * draws the object to the screen
	 */
	public abstract void draw();
	
	/**
	 * adds the being to the group
	 * @param group		the group to add to
	 */
	@SuppressWarnings("unchecked")
	protected void addToGroup(GenericGroup group) {
		// need to lock on the group
		synchronized(group) {
			group.getBeings().add(this);
			_groups.add(group);
		}
	}
	
	/**
	 * removes the being from this group
	 * @param group		the group to remove from
	 */
	protected void removeFromGroup(GenericGroup group) {
		// need to lock on the group
		synchronized(group) {
			group.getBeings().remove(this);
			_groups.remove(group);
		}
	}
	
	/**
	 * removes the being from all containing groups
	 */
	protected void delete() {
		// go through all the groups, deleting this being
		for(Iterator<GenericGroup<?,?>> iter = _groups.iterator(); iter.hasNext(); ) {
			GenericGroup<?,?> group = iter.next();
			// need to lock on the group
			synchronized(group) {
				group.getBeings().remove(this);
				iter.remove();
			}
		}
	}
	
	/**
	 * returns a being's shape
	 */
	public Shape getShape() {
		return _shape;
	}
	
	public PVector getPosition() {
		return _position;
	}
	
	/**
	 * Sets the Being's new position
	 * Note: this is a deep copy so the Being's
	 * position and its Shape's position will always
	 * be in sync
	 * @param position
	 */
	public void setPosition(PVector position) {
		_position.x = position.x;
		_position.y = position.y;
		_position.z = position.z;
	}
	
	public void setPosition(float x, float y, float z) {
		_position.x = x;
		_position.y = y;
		_position.z = z;
	}
	
	
	public float getX() {
		return _position.x;
	}
	
	public float getY() {
		return _position.y;
	}
	
	public float getZ() {
		return _position.z;
	}
	
	public void setPosition(float x, float y) {
		_position.x = x;
		_position.y = y;
	}
	
	public void setX(float x) {
		_position.x = x;
	}
	
	public void setY(float y) {
		_position.y = y;
	}
	
	public void setZ(float z) {
		_position.z = z;
	}
	
	
	
	
	
	
	public PVector getVelocity() {
		return _velocity;
	}
	
	public void setVelocity(PVector velocity) {
		_velocity = velocity;
	}

	
	public Rectangle getBoundingBox() {
		return _shape.getBoundingBox();
	}
	
	protected void setDone(boolean done) {
		_done = done;
	}
	
	public boolean processUpdate() {
		if(_done)
			update();
		setDone(true);
		step();
		return _done;
	}
	
	public void update() {}
	
	protected void step() {
		double elapsed = (double)updateTime();
		EulerIntegratePosition(elapsed/1e9);
	}
	
	protected void EulerIntegratePosition(double dt) {
		_position.add(PVector.mult(_velocity, (float)dt));
	}
	
	public String toString() {
		return "position: " + _position + " velocity: " + _velocity;
	}
	
	//Methods for receiving methods from PostOffice, defined in subscriber interfaces
	//Left blank here, must be overrided by user to add functionality
	public void handleKeyMessage(KeyMessage m) {
		//VOID
	}
	public void handleMouseMessage(MouseMessage m) {
		//VOID
	}
	public void handleMouseWheelMessage(MouseWheelMessage m) {
		//VOID
	}
	public void handleOscMessage(OscMessage m) {
		//VOID
	}
}
