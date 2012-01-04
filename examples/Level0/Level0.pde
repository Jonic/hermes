import hermes.physics.*;
import hermes.animation.*;
import hermes.hshape.*;
import hermes.postoffice.*;
import hermes.*;
import static hermes.postoffice.POConstants.*;
import java.util.LinkedList;

/**
Notes:
- may want to make toolbox into more columns to fit all the tools -- maybe even add an area for "currently selected tool" 
  or just add some length to the deal -- need odd number of tiles high
- may not need to register the canvas, toolbox with world (do not need to get drawn, updated) 
- could we add a sort of map and filter functionality to group? since it's parametrized,  it might
  be doable (maybe using a class .... i dont know)
- ask sam about multi-sampling
- at some point ... levels, menus, more tools, handles for rotating by mouse arbitrary angles


//TODO:


Bugs:




**/

///////////////////////////////////////////////////
// GLOBAL VARS AND CONSTANSTS
///////////////////////////////////////////////////

World world;
HCamera cam;
PostOffice po;


final int BUILD = 0;
final int RUN = 1;
final int COMPLETED = 2;
int mode = BUILD; // 0 is setup; 1 is run

//Container sizes and locations
//Y location and size is same for both
int containerHeight = 440;
int containerTopY = 120;
int containerBottomY = containerTopY + containerHeight;
//Canvas X and width
Canvas canvas = null;
int canvasWidth = 360;
int canvasLeftX = 30;
int canvasRightX = canvasLeftX + canvasWidth;
//Toolbox X and width
ToolBox toolBox;
int toolBoxWidth = 120;
int toolBoxLeftX = 430;
int toolBoxRightX = toolBoxLeftX + toolBoxWidth;

int cellSideLength = 40; //gives us 9 across, 12 down in canvas ; 3 across, 12 down in toolbox

//Frame size
int frameWidth = 700;
int frameHeight = 590;
int bgColor = color(122, 131, 139);

//groups
Group<Ball> ballGroup = null;
Group<Goal> goalGroup = null;
Group<Canvas> canvasGroup = null;
Group<Tool> toolGroup = null;
Group<Bubble> bubbleGroup = null;

///////////////////////////////////////////////////
// OTHER CLASSES
///////////////////////////////////////////////////

/**
 * The 0 at the top-left corner of the screen
 */
class Zero {
  
 int vertBarWidth = 30;
 int vertBarHeight = 60;
 
 int horizBarWidth = 60;
 int horizBarHeight = 10;
 
 int vertBarLeftX = 0;
 int vertBarRightX = 70;
 int vertBarY = horizBarHeight;

 int horizBarX = 20;
 int horizBarBottomY = horizBarHeight + vertBarHeight;
 int horizBarTopY = 0;

 void draw() {
   fill(255);
   noStroke();
   rect(horizBarX, horizBarTopY, horizBarWidth, horizBarHeight);
   rect(horizBarX, horizBarBottomY, horizBarWidth, horizBarHeight);
   rect(vertBarLeftX, vertBarY, vertBarWidth, vertBarHeight);
   rect(vertBarRightX, vertBarY, vertBarWidth, vertBarHeight);
 } 
  
}

///////////////////////////////////////////////////
// HELPER METHODS
///////////////////////////////////////////////////

/**
 * Changing mode of game between BUILD, RUN, and COMPLETED
 */
void setMode(int newMode) {
  if(newMode == BUILD) {
    if(bubbleGroup!=null) {//delete the bubbles
      bubbleGroup.destroy();
    }
    if(ball!=null) {     //delete the ball
      world.deleteFromGroups(ball);
      ball = null;
    }
    if(goal!=null) { // delete the goal
      world.deleteFromGroups(goal);
      goal = null;
    }
  } else if(newMode == RUN) {
    //clean up global vars
    templateTool = null;
    dragTool = null;
    selectedTool = null;
    
    makeBubbles();//make the bubbles (note: I put this before make the ball so that ball will be drawn overtop)
    ball = new Ball();    //make the ball
    world.addToGroup(ball, ballGroup);
    goal = new Goal();    //make the goal
    world.addToGroup(goal, goalGroup);
  } else if(newMode == COMPLETED) {
    //TODO: fill in 
    if(ball!=null) {
      world.deleteFromGroups(ball);
      ball = null; 
    }
  }
  
  mode = newMode;
}

/**
 * Factory helper method for filling canvas with bubbles
 */
void makeBubbles() {
  Cell[][] grid = canvas.getGrid();
  for(int i=0; i<canvasNumCellsX; i++) {
   for(int j=0; j<canvasNumCellsY; j++) {
     if(!grid[i][j].hasTool()) {
       Bubble bubble = new Bubble(new PVector(canvasLeftX+(i*cellSideLength)+(cellSideLength/2), containerTopY+(j*cellSideLength)+(cellSideLength/2)));
       world.addToGroup(bubble, bubbleGroup);//add to bubbleGroup
     }
   } 
  }
}

///////////////////////////////////////////////////
// PAPPLET
///////////////////////////////////////////////////

void setup() {
  size(frameWidth, frameHeight);
  Hermes.setPApplet(this);
 
  cam = new HCamera();
  po = new PostOffice(8080, 8000);
  world = new World(po, cam);
  world.lockUpdateRate(50);
  
  rectMode(CORNER);
  setMode(BUILD);
  
  //instantiate groups
  ballGroup = new Group(world);
  goalGroup = new Group(world);
  toolGroup = new Group(world);
  canvasGroup = new Group(world);
  bubbleGroup = new Group(world);
  
  //containers
  canvas = new Canvas(po);
  world.registerBeing(canvas, true);
  world.addToGroup(canvas, canvasGroup);
  po.registerMouseSubscription(canvas,NO_BUTTON,canvas.getShape());
  po.registerMouseSubscription(canvas,LEFT_BUTTON,canvas.getShape());
  toolBox = new ToolBox(canvas);
  world.registerBeing(toolBox, false);
  po.registerMouseSubscription(toolBox,NO_BUTTON);
  po.registerMouseSubscription(toolBox,LEFT_BUTTON);
  
  //buttons
  RunButton runButton = new RunButton(po);
  world.registerBeing(runButton, true);
  po.registerKeySubscription(runButton,R);
  po.registerMouseSubscription(runButton,NO_BUTTON,runButton.getShape());
  po.registerMouseSubscription(runButton,LEFT_BUTTON,runButton.getShape());
  RandomButton randomButton = new RandomButton(canvas,po);
  world.registerBeing(randomButton,false);
  po.registerKeySubscription(randomButton,SPACE);
  po.registerMouseSubscription(randomButton,NO_BUTTON,randomButton.getShape());
  po.registerMouseSubscription(randomButton,LEFT_BUTTON,randomButton.getShape());
  
  //key for SelectedToolAttributeSwitcher
  SelectedToolAttributeSwitcher selectedToolAttributeSwitcher = new SelectedToolAttributeSwitcher();
  po.registerKeySubscription(selectedToolAttributeSwitcher,E);
  po.registerKeySubscription(selectedToolAttributeSwitcher,W);
  
  //make the mousehandler and register subscriptions with the postoffice
  /*MouseHandler mouseHandler = new MouseHandler(canvas, toolBox, runButton, randomButton);
  po.registerMouseSubscription(mouseHandler, LEFT_BUTTON);
  po.registerMouseSubscription(mouseHandler, NO_BUTTON);*/
  
  //register interactions
  world.registerInteraction(canvasGroup, ballGroup, new InsideMassedCollider());
  world.registerInteraction(canvasGroup, bubbleGroup, new InsideMassedCollider());
  world.registerInteraction(ballGroup, goalGroup, new BallGoalCollider());
  world.registerInteraction(toolGroup, ballGroup, new MassedCollider());
  world.registerInteraction(toolGroup, bubbleGroup, new MassedCollider());
   
  smooth();

  world.start(); // gets the World thread running
}

void draw() {
    background(bgColor);
    cam.draw(); // Camera object handles drawing all the appropriate Beings
}
