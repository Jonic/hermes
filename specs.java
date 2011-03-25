/**
 * Basic game object class
 * Anything that is getting drawn or interacting with other game objects is a Being
 */
abstract class Being {

	abstract double getX();
	abstract double getY();
	abstract double getZ();

	/**
	 * draws the object to the screen
	 * takes a screen x and y coor
	 * the reason for this is that the being's internal x and y will be screen coordinates, not world coordinates
	 * there may be a better way to do this, with framebuffers, need to look in to this
	 */
	abstract void draw(x, y);

	//Need to add Shape enum?
	//Also does every Being need a
	abstract Shape getShape;

	List<Group> groups;
	Shape shape; // do beings necessarily need this? -- sam

	//TODO: need to add methods for recieving messages?
	//Has to be defined in Being

}

/**
 * TODO: Timer class?
 */

/**
 * Contains necessary logic to interact two beings
 * detect method determines if two bodies should interact
 * handle "interacts" the two beings by updating them
 */
interface Interactor<A,B> {

	boolean detect(A, B);
	void handle(A, B);

}

/**
 * Determines which beings are compared and potentially interacted by Interactor
 * Group B is given as arugment to constructor and organized according to optimizer type
 * Basic optimizer checks all the members of A against all the members of B (order n^2)
 * Sample advanced optimizers include quadtree
 * detect checks body A against the structure formed from Group B
 */
interface Optimizer<A,B> {

	public void detect(A body, Interactor i);

}

/**
 * TODO: Need Group def?
 */

/**
 * Defines a 'game state'
 * Examples include: Levels, menu screens
 * Subclassed by user to create individual worlds
 * User creates the necessary Beings, Interactors, and Optimizers in setup
 * The other functions are implemented by us; we handle the running and drawing of the world
 */
abstract class World {

	List<Being> masterList;

	//Setup loop
	//Written by user
	abstract void setup();

	//Is this in the world?
	void registerInteraction(Group A, Group B, Interactor inter, Optimizer optimizer) {}

	//Creates a new thread and runs the update loop in it
	void run() {}

	//Basic method to call to know if world is running or not
	boolean getState {}

	//Called by God's draw method to
	void draw() {}

}

/**
 * the fundamental game obeject that runs the world and does drawing
 */
class God {
	
	void setWorld(World world);
	
	World getWorld();
	
	// draws the world state to the screen, must be called within user's Processing draw loop
	void draw();
	
	// adds a post graphics effect at the end of the world draw, for things like global fades, glows, etc
	void addGraphicsEffect(GraphicsEffect fx);
}

/**
 * Handles communication between game and outside world
 * Detects key events, mouse events, and osc messages
 * Beings filter passed messages internally
 */
class PostOffice {

	//Fields containing subscribing Beings
	ArrayList<Being> keySubscribed;
	ArrayList<Being> mouseSubscribed;
	ArrayList<Being> oscSubscribed;

	//Constructor
	PostOffice() {}

	//Runs Post Office
	//Waits for events, and adds them to queue for handling
	//Does not handle events while a world update loop is running
	void run() {}

	//Sends key presses to subscribing Beings
	void handleKeyPress(char keyPressed) {}

	//Sends mouse clicks to subscribing Beings
	void handleMouseClick(Point clickLocation) {}

	//Sends osc messages to subscribing Beings
	//Q: how will we internally represent OSC messages?
	void handleOSC(Message recievedMessage) {}

	//Registers Group of Being for keyboard subscription
	void registerKeySubscribe(Group g) {}

	//Registers Group of Being for mouse subscription
	void registerMouseSubscribe(Group g) {}

	//Registers Group of Being for osc subscription
	void registerOSCSubscribe(Group g) {}
}

/**
 * an animated sprite based on an image sequence
 */
class Sprite {
	// loads a Sprite by importing a single image and splitting it into a tiles with given width and height
	Sprite(String filename, int width, int height);
	
	// loads a Sprite from an image sequence in format name.#.ext
	Sprite(String name, String extension, int end, int start = 0);
	
	// creates a playable animation between frames start_frame and end_frame 
	void addAnimation(int key, int start_frame, int end_frame);
	
	// plays the animation denoted by the given keys, will loop indefinitely if loop is true
	void play(int key, boolean loop = false);
	
	// draws the sprite next animation frame at the gien screen position
	//  advances frame count by given value
	void draw(double x, double y, int advanceFrame = 1);
}

/**
 * this interface is used to implement a callback method for applying an effect globally to a block of pixels
 */
interface graphicsEffect {

	// applies an effect to some Processing image (should this be PGraphics instead?)
	void apply(PImage buffer);

}

// a pre-written fade effect
class Fade implements graphicsEffect {}
