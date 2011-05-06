import src.template.library.*;
import src.hermes.*;
import src.hermesTest.physicsTest.*;
import src.hermesTest.postOfficeTests.*;
import src.hermes.shape.*;
import src.hermes.animation.*;
import src.hermesTest.shapeTests.*;
import src.hermesTest.core.*;
import src.hermes.physics.*;
import src.hermes.postoffice.*;
import static src.hermes.HermesMath.*;

///////////////////////////////////////////////////
// GLOBAL VARS AND CONSTANSTS
///////////////////////////////////////////////////

World world;
Camera cam;
PostOffice po;

int mode = 0; // 0 is setup; 1 is run

//Frame size
int frameWidth = 700;
int frameHeight = 540;

//Container sizes and locations
//Y location and size is same for both
int containerTopLeftY = 30;
int containerHeight = 480;
//Canvas X and width
int canvasTopLeftX = 30;
int canvasWidth = 360;
//Toolbox X and width
int toolBoxTopLeftX = 420;
int toolBoxWidth = 250;
//Cell constants
float flowMax = 10;
int cellSideLength = 40; //gives us 9 across, 12 down
int numCellsX = canvasWidth / cellSideLength;
int numCellsY = canvasHeight / cellSideLength;

//Tool stored by dragging, used for placing tools on the board
int toolMode = 0;
//Constants defining the tool being dragged
static int PUNCHER = 1;
static int BATON = 2;
static int FUSE = 3;
static int SQUARE = 4;
static int CIRCLE = 5;

Tool dragTool = null;



////////////////////////////////////////
// BEINGS
////////////////////////////////////////

/**
 * Central canvas for building levels/tracks
 * Holds information for vector field
 * Has inside bounding box
 * Responsible for placing tools into grid
 */
class Canvas extends Being {
  
	Cell[][] _grid;
	Group<Tool> _toolGroup;

  Canvas() {
    super(new Rectangle(new PVector(canvasTopLeftX, containerTopLeftY), new PVector(canvasWidth, containerHeight), PApplet.CORNER));
	 _grid = new Cell[numCellsX][numCellsY];
    initialize();
  }
  
  void initialize() {
    for(int i=0; i<numCellsX; i++) {
     for(int j=0; j<numCellsY; j++) {
	   _grid[i][j] = new Cell();
     } 
    }
  }

	//TODO Add cell randomizer
  
  void draw() {
	 if(mode == BUILD) {
	   for(int i=0; i<numCellsX; i++) {
        for(int j=0; j<numCellsY; j++) {
	       Cell curr = _grid[i][j];
	       curr.draw()
        } 
      }
    }
  }

  /**
   * Checks if mouse is currently in canvas
   * If the user has selected a tool, place the tool in the location of the mouse
   */
  void handleMouseMessage(MouseMessage m) {
	int x = m.getX();
	int y = m.getY();
	x -= canvasTopLeftX;
	y -= canvasTopLeftY;
	
	// if mouse released
	// check that you are dragging a tool
	if(m.getAction() == PostOffice.MOUSE_RELEASED && dragTool != null) {
		int x = m.getX();
		int y = m.getY();
		x -= canvasTopLeftX;
		y -= canvasTopLeftY;
		// check that the mouse location is within the canvas
		if(x >= 0 && y >= 0 && x <= canvasWidth && y <= canvasHeight) {
			x /= cellSideLength;
			y /= cellSideLength;
			// add the tool to the appropriate cell
			Cell in = _grid[x][y];
		   // remove the tool at that cell if necessary
			if(in.hasTool) {
				Tool remove = in.getTool();
				_toolGroup.remove(remove);
			}
			Tool newTool = dragTool;
			in.setTool(dragTool);
			_toolGroup.add(dragTool);
		}
	}
  }
}

/**
 * Helper class for Canvas, defines a cell in the vector field
 * Location is defined by which cell of the 2D array they are stored in inside Canvas
 */
class Cell {
 
  PVector _flowDirection; //Any normalized vector
  float _flowStrength; //Cannot be negative or greater than flowMax
  Tool _tool;
  
  Cell() {
    _flowDirection = new PVector(0,1);
    _flowStrength = 1;
	 _tool = true;
  }
  
  void setFlowDir(PVector direction) {
    _flowDirection = direction; 
  }

  void setFlowStrength(float strength) {
	 _flowStrength = strength;
	}
	
	boolean hasTool() {
		return _tool != null;
	}
	
	void setTool(Tool tool) {
		_Tool = tool;
	}
	
	void draw() {
		if(_tool == null) {
		}
	}
 }

abstract class Tool extends Being {

}

class MouseHandler implements MouseSubscriber {
	
	Canvas _c;
	boolean _mousePressed;
	
	MouseHandler(Canvas c) {
		
	}
	
	void handleMouseMessage() {
		
	}
}

///////////////////////////////////////////////////
// GROUPS
///////////////////////////////////////////////////

class CellGroup extends Group {
  
  
}

///////////////////////////////////////////////////
// PAPPLET
///////////////////////////////////////////////////

void setup() {
  size(frameWidth, frameHeight); 
  Hermes.setPApplet(this);
 
  cam = new Camera();
  try {
   po = new PostOffice(8080, 8000);
  } catch(Exception e) {
   po = new PostOffice(); 
  }
  world = new World(po, cam);
  world.lockUpdateRate(50);
  
  Canvas canvas = new Canvas();
  world.registerBeing(canvas, false);
  
  smooth();

  world.start(); // gets the World thread running
}


void draw() {
    background(110);
    
    cam.draw(); // Camera object handles drawing all the appropriate Beings

}
