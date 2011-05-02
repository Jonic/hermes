package src.hermes;

import processing.core.PApplet;

/**
 * Library class for broad scale static helpers, storage of PApplet
 */
public class Hermes {
	
	private static PApplet _parentApplet; //Storage of sketch's PApplet. 
	
	/**
	 * Getter for the PApplet
	 */
	public static PApplet getPApplet() {
		return _parentApplet;
	}
	
	/**
	 * Set the PApplet that all utilities use
	 */
	public static void setPApplet(PApplet parentApplet) {
		_parentApplet = parentApplet;		
	}
	
	/**
	 * causes the calling thread to sleep, catches InterruptedException without any interactions
	 * @param time	the time (in milliseconds) to sleep
	 */
	public static void unsafeSleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {}
	}
	
}
