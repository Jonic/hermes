package src.hermes.postoffice;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.collect.HashMultimap;
import src.hermes.Hermes;

/**
 * Listens for and sends OSC, mouse, and keyboard messages
 * Tells subscribers of a specific type of message when one is received
 * and passes on information stored in message to subscriber
 */
public class PostOffice implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	
	//Default ports for OSC communication
	public static final int DEFAULT_PORT_IN = 7000;
	public static final int DEFAULT_PORT_OUT = 7070;
	
	//Constants representing buttons
	public static int NOBUTTON = MouseEvent.NOBUTTON;
	public static int BUTTON1 = MouseEvent.BUTTON1;
	public static int BUTTON2 = MouseEvent.BUTTON2;
	public static int BUTTON3 = MouseEvent.BUTTON3;
	
	//Constants representing type of mouse action
	public static int MOUSE_PRESSED = MouseEvent.MOUSE_PRESSED;
	public static int MOUSE_RELEASED = MouseEvent.MOUSE_RELEASED;
	public static int MOUSE_DRAGGED = MouseEvent.MOUSE_DRAGGED;
	public static int MOUSE_MOVED = MouseEvent.MOUSE_MOVED;
	
	//OSCPorts for listening and receiving
	private com.illposed.osc.OSCPortIn _receive;
	private com.illposed.osc.OSCPortOut _send;
	@SuppressWarnings("unused")
	private com.illposed.osc.OSCListener _listener;
	
	//Maps that associate subscribers with messages they want to receive
	private HashMultimap<String, KeySubscriber> _keySubs;
	private HashMultimap<Integer, MouseSubscriber> _mouseSubs;
	private ArrayList<MouseWheelSubscriber> _mouseWheelSubs;
	private HashMultimap<String, OscSubscriber> _oscSubs;
	
	//Stores messages as they are received, which are then picked off by checkMail()
	private ConcurrentLinkedQueue<KeyMessage> _keyQueue;
	private ConcurrentLinkedQueue<MouseMessage> _mouseQueue;
	private ConcurrentLinkedQueue<MouseWheelMessage> _mouseWheelQueue;
	private ConcurrentLinkedQueue<OscMessage> _oscQueue;
	
	//Boolean stating whether osc is on or off
	private boolean _onOSC;
	
	/**
	 * Constructor with no OSC
	 */
	public PostOffice() {
		_onOSC = false;
		POInit();
	}
	
	/**
	 * Constructor that defines location to send to on localhost
	 * @param applet - Top Processing PApplet running the PostOffice
	 * @param portIn - port to receive messages on
	 * @param portOut - port to send messages on
	 */
	public PostOffice(int portIn, int portOut) {
		assert portIn > 1000 : "PostOffice constructor: portIn must be a valid port number, greater than 1000";
		assert portOut > 1000 : "PostOffice constructor: portOut must be a valid port number, greater than 1000";
		
		_onOSC = true;
		POInit();
		//Start OSC and set listener
		try {
			_receive = new com.illposed.osc.OSCPortIn(portIn);
			_receive.startListening();
			_listener = new PostOfficeOSCListener(this);
		} catch (SocketException e) {
			System.err.println("OSC Port In on " + portIn + " could not start");
		}
		try {
			_send = new com.illposed.osc.OSCPortOut(InetAddress.getLocalHost(),portOut);
		} catch (UnknownHostException e) {
			System.err.println("OSC Port Out on " + portOut + " could not start");
		} catch (SocketException e) {
			System.err.println("OSC Port Out on " + portOut + " could not start");
		}
	}
	
	/**
	 * Constructor for PostOffice that sends messages to non-local address
	 * @param applet - Top Processing PApplet running the PostOffice
	 * @param portIn - port to receive messages on
	 * @param portOut - port to send messages on
	 * @param netAddress - url of location to send messages to
	 */
	public PostOffice(int portIn, int portOut, String netAddress) {
		assert portIn > 1000 : "PostOffice constructor: portIn must be a valid port number, greater than 1000";
		assert portOut > 1000 : "PostOffice constructor: portOut must be a valid port number, greater than 1000";
		assert netAddress != null : "PostOffice constructor: netAddress must be a valid String";
		
		_onOSC = true;
		POInit();
		//Start OSC and set listener
		try {
			_receive = new com.illposed.osc.OSCPortIn(portIn);
			_receive.startListening();
		} catch (SocketException e) {
			System.err.println("OSC Port In on " + portIn + " could not start");
		}
		try {
			_send = new com.illposed.osc.OSCPortOut(InetAddress.getByName(netAddress), portOut);
		} catch (UnknownHostException e) {
			System.err.println("OSC Port Out on " + portOut + ", net address " + netAddress + " could not start");
		} catch (SocketException e) {
			System.err.println("OSC Port Out on " + portOut + ", net address " + netAddress + " could not start");
		}
		_listener = new PostOfficeOSCListener(this);
	}
	
	/**
	 * Helper for constructors
	 * Sets PO as listener on PApplet, and initializes internal lists
	 */
	private void POInit() {
		//Set PostOffice to listen for events
		Hermes.getPApplet().addKeyListener(this);
		Hermes.getPApplet().addMouseListener(this);
		Hermes.getPApplet().addMouseMotionListener(this);
		Hermes.getPApplet().addMouseWheelListener(this);
		//Initialize subscription list and message queue
		_keySubs = HashMultimap.create();
		_mouseSubs = HashMultimap.create();
		_mouseWheelSubs = new ArrayList<MouseWheelSubscriber>();
		_keyQueue = new ConcurrentLinkedQueue<KeyMessage>();
		_mouseQueue = new ConcurrentLinkedQueue<MouseMessage>();
		_mouseWheelQueue = new ConcurrentLinkedQueue<MouseWheelMessage>();
		if(_onOSC) {
			_oscSubs = HashMultimap.create();
			_oscQueue = new ConcurrentLinkedQueue<OscMessage>();
		}
	}
	
	///////////////////////////////////////
	//Methods for registering subscriptions
	
	/**
	 * Registers a subscription to messages sent by a specific keyboard key
	 * @param sub - the KeySubscriber signing up
	 * @param key - the name of the keyboard key whose messages the subscriber wants
	 */
	public void registerKeySubscription(KeySubscriber sub, String key) {
		assert sub != null : "PostOffice.registerKeySubscription: sub must be a valid KeySubscriber";
		assert key != null : "PostOffice.registerKeySubscription: key must be a valid String";
		_keySubs.put(key, sub);
	}
	
	/**
	 * Registers a subscription to messages sent by a specific mouse button
	 * Buttons are defined by constants in the Post Office class
	 * Subscribe with "NO_BUTTON" to receive information about mouse movements when no button is pressed
	 * @param sub - the MouseSubscriber signing up
	 * @param button - an integer corresponding to a mouse button whose messages the subscriber wants
	 */
	public void registerMouseSubscription(MouseSubscriber sub, int button) {
		assert sub != null : "PostOffice.registerMouseSubscription: sub must be a valid MouseSubscriber";
		assert button == NOBUTTON ||
				button == BUTTON1 ||
				button == BUTTON2 ||
				button == BUTTON3 :
					"PostOffice.registerMouseSubscription: button must be one of the buttons defined in PostOffice";
		_mouseSubs.put((Integer)button, sub);
	}
	
	/**
	 * Registers a subscription to the mouse wheel (one subscription gets you everything)
	 * @param sub - the MouseWheelSubscriber signing up
	 */
	public void registerMouseWheelSubscription(MouseWheelSubscriber sub) {
		assert sub != null : "PostOffice.registerMouseWheelSubscription: sub must be a valid MouseWheelSubscriber";
		_mouseWheelSubs.add(sub);
	}

	/**
	 * Registers a subscription to messages received on a specific OSC address
	 * @param sub - the OscSubscriber signing up
	 * @param address - the address whose messages the subscriber wants
	 */
	public void registerOscSubscription(OscSubscriber sub, String address) {
		assert _onOSC : "PostOffice.registerOscSubscription: cannot register an OSC subscription unless OSC is on";
		assert sub != null : "PostOffice.registerOscSubscription: sub must be a valid OscSubscriber";
		assert address != null : "PostOffice.registerOscSubscription: address must be a valid String";
		_oscSubs.put(address, sub);
	}
	
	/////////////////////////////////
	//Methods for sending OscMessages
	
	/**
	 * Sends an OscMessage on the given address containing only the given int
	 * @param address - address message is to be sent on
	 * @param send - integer to be sent
	 */
	public void sendInt(String address, int send) {
		assert _onOSC : "PostOffice.sendInt: cannot send an OSC message while OSC is off";
		assert address != null : "PostOffice.sendInt: address must be a valid String";
		Object[] array = new Object[1];
		array[0] = (Integer) send;
		com.illposed.osc.OSCMessage mail = new com.illposed.osc.OSCMessage(address, array);
		try {
			_send.send(mail);
		}
		catch(Exception e) {
			System.err.println("Error sending message " + send + " on " + address);
		}
	}
	
	/**
	 * Sends an OscMessage on the given address containing only the given float
	 * @param address
	 * @param send
	 */
	public void sendFloat(String address, float send) {
		assert _onOSC : "PostOffice.sendFloat: cannot send an OSC message unless OSC is off";
		assert address != null : "PostOffice.sendFloat: address must be a valid String";
		Object[] array = new Object[1];
		array[0] = (Float) send;
		com.illposed.osc.OSCMessage mail = new com.illposed.osc.OSCMessage(address, array);
		try {
			_send.send(mail);
		}
		catch(Exception e) {
			System.err.println("Error sending message " + send + " on " + address);
		}
	}
	
	/**
	 * Sends an OscMessage on the given address containing only the given boolean
	 * @param address
	 * @param send
	 */
	public void sendBoolean(String address, boolean send) {
		assert _onOSC : "PostOffice.sendBoolean: cannot send an OSC message unless OSC is off";
		assert address != null : "PostOffice.sendBoolean: address must be a valid String";
		Object[] array = new Object[1];
		array[0] = (Boolean) send;
		com.illposed.osc.OSCMessage mail = new com.illposed.osc.OSCMessage(address, array);
		try {
			_send.send(mail);
		}
		catch(Exception e) {
			System.err.println("Error sending message " + send + " on " + address);
		}
	}
	
	/**
	 * Sends an OscMessage on the given address containing the contents of the given list
	 * @param address
	 * @param send
	 */
	public void sendList(String address, ArrayList<Object> send) {
		assert _onOSC : "PostOffice.sendList: cannot send an OSC message unless OSC is off";
		assert address != null : "PostOffice.sendList: address must be a valid String";
		assert send != null : "PostOffice.sendList: send must be a valid ArrayList";
		int size = send.size();
		Object[] array = new Object[size];
		for(int i = 0; i < size; i++) {
			array[i] = send.get(i);
		}
		com.illposed.osc.OSCMessage mail = new com.illposed.osc.OSCMessage(address, array);
		try {
			_send.send(mail);
		}
		catch(Exception e) {
			System.err.print("Error sending message ");
			for(Object o : array) {
				System.err.print(o + " ");
			}
			System.err.print("on address " + address + "\n");
		}
	}
	
	//////////////////////////////////////////////////////////////////////////
	/**
	 * Command that sends all messages queued by the PostOffice to subscribers
	 */
	public void checkMail() {
		//Send all the messages in each queue to the corresponding subscribers
		synchronized(_keyQueue) {
			while(!_keyQueue.isEmpty()) {
				KeyMessage m = _keyQueue.poll();
				String key = m.getKey();
				Set<KeySubscriber> subs = _keySubs.get(key);
				for(KeySubscriber sub : subs) {
					sub.handleKeyMessage(m);
				}
			}
		}
		synchronized(_mouseQueue) {
			while(!_mouseQueue.isEmpty()) {
				MouseMessage m = _mouseQueue.poll();
				int button = m.getButton();
				Set<MouseSubscriber> subs = _mouseSubs.get(button);
				for(MouseSubscriber sub : subs) {
					sub.handleMouseMessage(m);
				}
			}
		}
		synchronized(_mouseWheelQueue) {
			while(!_mouseWheelQueue.isEmpty()) {
				MouseWheelMessage m = _mouseWheelQueue.poll();
				for(MouseWheelSubscriber sub : _mouseWheelSubs) {
					sub.handleMouseWheelMessage(m);
				}
			}
		}
		
		if(_onOSC) {
			
			synchronized(_oscQueue) {
				while(!_oscQueue.isEmpty()) {
					OscMessage m = _oscQueue.poll();
					
					String address = m.getAddress();
					Set<OscSubscriber> subs = _oscSubs.get(address);

					for(OscSubscriber sub : subs) {
						sub.handleOscMessage(m);
					}
				}
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////
	//Methods defined by implemented interfaces for handling mouse+keyboard input
	/////////////////////////////////////////////////////////////////////////////
	//Get keyboard events
	
	/**
	 * Ignore keyTyped events
	 */
	public void keyTyped(KeyEvent e) {
		//VOID
	}
	/**
	 * On a key press, make a new KeyMessage and add it to the queue
	 * Note: keyPressed events will repeat at rate set by OS if key is held down
	 * Users should control this with keyReleased events
	 */
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		String keyString = KeyEvent.getKeyText(key);
		KeyMessage m = new KeyMessage(keyString, true);
		synchronized(_keyQueue) {
			_keyQueue.add(m);
		}
	}
	/**
	 * On a key release, make a new KeyMessage and add it to the queue
	 */
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		String keyString = KeyEvent.getKeyText(key);
		KeyMessage m = new KeyMessage(keyString, false);
		synchronized(_keyQueue) {
			_keyQueue.add(m);
		}
	}
	
	///////////////////////////////
	//Get mouse clicks and releases
	
	/**
	 * Ignore mouseClicked events
	 */
	public void mouseClicked(MouseEvent e) {
		//VOID
	}
	/**
	 * On a mouse press, make a new MouseMessage and add it to the queue
	 */
	public void mousePressed(MouseEvent e) {
		MouseMessage m  = new MouseMessage(e.getButton(), MOUSE_PRESSED, e.getX(), e.getY());
		synchronized(_mouseQueue) {
			_mouseQueue.add(m);
		}
	}
	/**
	 * On a mouse button release, make a new MouseMessage and add it to the queue
	 */
	public void mouseReleased(MouseEvent e) {
		MouseMessage m  = new MouseMessage(e.getButton(), MOUSE_RELEASED, e.getX(), e.getY());
		synchronized(_mouseQueue) {
			_mouseQueue.add(m);
		}
	}
	/**
	 * Ignore mouseEntered events
	 */
	public void mouseEntered(MouseEvent e) {
		//VOID
	}
	/**
	 * Ignore mouseExited events
	 */
	public void mouseExited(MouseEvent e) {
		//VOID
	}

	///////////////////////////////
	//Get changes in mouse location
	/**
	 * When the mouse is dragged, create a MouseMessage and add it to the group
	 */
	public void mouseDragged(MouseEvent e) {
		MouseMessage m  = new MouseMessage(e.getButton(), MOUSE_DRAGGED, e.getX(), e.getY());
		synchronized(_mouseQueue) {
			_mouseQueue.add(m);
		}
		
	}
	/**
	 * When the mouse is moved, create a MouseMessage and add it to the queue
	 */
	public void mouseMoved(MouseEvent e) {
		MouseMessage m  = new MouseMessage(e.getButton(), MOUSE_MOVED, e.getX(), e.getY());
		synchronized(_mouseQueue) {
			_mouseQueue.add(m);
		}
		
	}

	//////////////////////////
	//Get mouse wheel movement
	/**
	 * When the mouse wheel is moved, create a MouseWheelMessage and add it to the queue
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		MouseWheelMessage m = new MouseWheelMessage(e.getWheelRotation());
		synchronized(_mouseWheelQueue) {
			_mouseWheelQueue.add(m);
		}
	}
	
	//////////////////
	//Get OSC Messages
	/**
	 * Implementation of OCSListener that turns illposed messages into our messages
	 */
	class PostOfficeOSCListener implements com.illposed.osc.OSCListener {
		
		//Reference to containing post office
		PostOffice _p;
		
		/**
		 * Constructor passes in reference to containing post office
		 */
		protected PostOfficeOSCListener(PostOffice p) {
			_p = p;
		}
		
		/**
		 * Handles a received message
		 * Converts it from illposed to our message
		 * Adds it to queue
		 */
		public void acceptMessage(Date time, com.illposed.osc.OSCMessage message) {
			System.out.println("GOT AN OSC MESSAGE!.. testing.. delete this println in PostOfficeOSCListener");
			OscMessage m = new OscMessage(message);
			synchronized(_p._oscQueue) {
				_p._oscQueue.add(m);
			}
		}
	}
}
