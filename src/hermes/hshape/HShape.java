package src.hermes.hshape;

import processing.core.*;

/**
 * an abstraction for shape, used for collision detection and response
 * when the shape is linked to a being, position should point to the being's position
 * in order for collision to work, all shapes must know how to collide with all other shapes
 * 
 * @author Sam
 *
 */
public abstract class HShape {
	
	protected PVector _position; // the shape's position
		// all points in the shape are defined relative to the position
	
	/**
	 * creates a new shape
	 * @param position  the shape's position
	 * when position is attached to a being, the shape's position should reference the being's position
	 */
	protected HShape(PVector position) {
		assert position != null : "InHShape constructor: position must be a valid PVector";
		
		_position = position;
	}
	
	/**
	 * detects a collision with another shape
	 * @param other		the shape to collide with
	 * @return			whether the shapes have collided
	 */
	public boolean collide(HShape other) {
		assert other != null : "HShape.collide: other must be a validHShape";
		return projectionVector(other) != null;
	}

	/**
	 * finds the projection vector for a collision with another shape
	 * the projection vector is the vector by which one body could be displaced such that
	 *  it no longer intersects the other body, by the shortest possible distance
	 * @param other		the shape to collide with
	 * @return			the projection vector from this body to other, or null if they aren't colliding
	 */
	public abstract PVector projectionVector(HShape other);
	public abstract PVector projectionVector(Rectangle other);
	public abstract PVector projectionVector(Circle other);
	public abstract PVector projectionVector(Polygon other);
	
	/**
	 * whether the Shape contains the given point coordinates (boundary is inclusive)
	 * @param point     the point vector
	 * @return          true if (x,y) lies within the Shape
	 */
	public abstract boolean contains(PVector point);
	
	/**
	 * @param x         the x coordinate
	 * @param y         the y coordinate
	 * @return          true if (x,y) lies within the Shape
	 */
	public abstract boolean contains(float x, float y);
	
	/**
	 * finds the smallest Rectangle that encloses the shape
	 * @return	the bounding box
	 */
	public abstract Rectangle getBoundingBox();
	
	/**
	 * Returns the shape's position. 
	 * Note, if you move this vector it will move the shape.
	 * @return the shape's position
	 */
	public PVector getPosition() {
		return _position;
	}
	
	/**
	 * Shortcut method for drawing the shape on screen
	 */
	public abstract void draw();
}

