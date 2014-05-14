/*
 * File: Breakout.java
 * -------------------
 * Name: Omar Diab
 * Section Leader: Peter Pham
 * 
 * This file implements an altered version of Breakout, which I
 * dub "Sonar."  The paddle is a submarine, and the bricks are 
 * moving ships on the surface. You could only see ships if 
 * they are hit by a sonar pulse. Depth charges periodically drop
 * down and try to kill you as well.  The same concept as
 * Breakout otherwise applies.
 * 
 * NOTE: I used arrays because they were useful, but I never
 * used them before.  I just read from Chapter 11 :)
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;
	
/** Custom colors */
	public static final Color BACKGROUND_COLOR = new Color(4, 20, 25);
	public static final Color GRID_COLOR = new Color(50, 100, 120);

/** Space between gridlines */
	private static final double GRID_SPACING = 25;

/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

/** Dimensions of the submarine */
	private static final int SUBMARINE_WIDTH = 75;
	private static final int SUBMARINE_HEIGHT = 25;

/** Offset of the submarine up from the bottom */
	private static final int SUBMARINE_Y_OFFSET = 60;

/** Number of battleships per row */
	private static final int NSHIPS_PER_ROW = 2;

/** Number of rows of battleships */
	private static final int NSHIP_ROWS = 10;
	
/** Total number of battleships */
	private static final int TOTAL_SHIPS = NSHIPS_PER_ROW * NSHIP_ROWS;

/** Separation between battleships */
	private static final int SHIP_SEP = 4;

/** Width of a battleship */
	private static final int SHIP_WIDTH = 45;

/** Height of a battleship */
	private static final int SHIP_HEIGHT = 15;

/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 4;

/** Offset of the top battleship row from the top */
	private static final int SHIP_Y_OFFSET = 70;

/** Number of turns */
	private static final int NTURNS = 3;
	
/** Number of turns before speeding up */
	private static final int CHALLENGE = 7;

/** Pause time */
	private static final int PAUSE = 4;
	
/** Point values */
	private static final int POINTS_SHIP = 100;
	
/** Submarine Y Position */
	private static final double SUBMARINE_Y = HEIGHT - SUBMARINE_Y_OFFSET - SUBMARINE_HEIGHT;

/** Distance sonar has to be from battleship hits to detect their presence */
	private static final double SONAR_TOLERANCE = 100;
	
/** Bomb size */
	private static final double BOMB_HEIGHT = 50;
	private static final double BOMB_WIDTH = 30;

/** Bomb speed */
	private static final double BOMB_SPEED = .08;

/** Bomb blast radius */
	private static final double BLAST_RADIUS = 75;

/* Declaring variables */
	//Integers
	private int shipNum = TOTAL_SHIPS;
	private int score = 0;
	
	private RandomGenerator rgen = RandomGenerator.getInstance();
	
	//GObjects
	private GOval ball = new GOval(WIDTH/2 - BALL_RADIUS, HEIGHT/2 - BALL_RADIUS, BALL_RADIUS*2, BALL_RADIUS*2);
	private GImage submarine = new GImage("submarine.gif", (WIDTH - SUBMARINE_WIDTH)/2, SUBMARINE_Y);
	private GLabel scoreLabel = new GLabel("", submarine.getX(), SUBMARINE_Y + 2 * SUBMARINE_HEIGHT);	
	private GImage[] battleships = new GImage[TOTAL_SHIPS];
	private GOval sonArc = new GOval(WIDTH/2, HEIGHT, 0, 0);
	private GImage bomb = new GImage("bomb.gif", rgen.nextDouble(.1, .9) * WIDTH, HEIGHT/2);
	private GOval bombRadius = new GOval((bomb.getX() + BOMB_WIDTH/2) - BLAST_RADIUS, (bomb.getY() + BOMB_HEIGHT/2) - BLAST_RADIUS, 2*BLAST_RADIUS, 2*BLAST_RADIUS);

	//Doubles [movement]
	private double vy = 1.0;
	private double vx = rgen.nextDouble(.3, 1);
	private double[] shipVx = new double [TOTAL_SHIPS];
	
	//Variables that check the state of objects
	private GObject collider = null;
	private boolean startCheck = false;
		
	/* Method: run() */
	/** Runs the Breakout program. */
	public void run(){
		setBackground(BACKGROUND_COLOR);
		InitGrid();
		PlaceShips();
		InitSubmarine();
		Intro();
		PlayGame();
	}
	
	private void InitGrid(){
		for (int row = 1; row < HEIGHT/GRID_SPACING; row++){
			double yPosition = row * GRID_SPACING;
			for (int column = 1; column < WIDTH/GRID_SPACING; column ++){
				double xPosition = column * GRID_SPACING;
				GLine columnLine = new GLine (xPosition, 0, xPosition, HEIGHT);
				columnLine.setColor(GRID_COLOR);
				add(columnLine);
				columnLine.sendToBack();
			}
			GLine rowLine = new GLine (0, yPosition, WIDTH, yPosition);
			rowLine.setColor(GRID_COLOR);
			add(rowLine);
			rowLine.sendToBack();
		}
	}
	
	private void PlaceShips(){ //This function sets up the initial configuration of battleships.		
		for (int row = 0; row < NSHIP_ROWS; row++){
			for (int column = 0; column < NSHIPS_PER_ROW; column++) {
				//Defines x and y position of each battleship laid.  The math is based on which row and which column the battleship is being placed in.
				double xPosition = rgen.nextDouble(0, 1) * (WIDTH - SHIP_WIDTH); //Randomizes where on the x-axis ships are located.
				double yPosition = SHIP_Y_OFFSET + (NSHIP_ROWS - row) * (SHIP_HEIGHT + SHIP_SEP);
				
				//The expression (row * 2 + column) indexes each battleship in the array from 0 to the number of ships - 1.
				battleships[row * 2 + column] = new GImage ("battleship.gif", xPosition, yPosition);
				battleships[row * 2 + column].setSize(SHIP_WIDTH, SHIP_HEIGHT); //Resizes the ship.
				battleships[row * 2 + column].setVisible(false); //Invisible so their position are not given away at the start.
				add (battleships[row * 2 + column]);
			}
		}
	}
	
	public void InitSubmarine(){
		submarine.setSize(SUBMARINE_WIDTH, SUBMARINE_HEIGHT);
		add(submarine);
		
		//Defines the aesthetic properties of the score label beneath the submarine.
		scoreLabel.setColor(Color.CYAN);
		add (scoreLabel);
		
		addMouseListeners(); //Initializes mouse control.
	}
	
	//Defines aesthetic propreties of the sonar pulses, and adds them.
	private void InitSonar(){
		sonArc.setColor(Color.CYAN);
		add(sonArc);
		sonArc.sendToBack();
	}
	
	//Defines aesthetic properties of the bomb and the blast indicator, and adds them.
	private void InitDepthCharge(){
		bomb.setVisible(false);
		bomb.setSize(BOMB_WIDTH, BOMB_HEIGHT);
		add(bomb);
		
		bombRadius.setVisible(false);
		bombRadius.setColor(Color.GRAY);
		bombRadius.setFilled(true);
		bombRadius.setFillColor(Color.RED);
		add(bombRadius);
		
		bomb.sendToBack();
		bombRadius.sendToBack();
	}
	
	private void Intro(){
		GLabel start = new GLabel ("CLICK TO START", 0, 0); //This is the GLabel that prompts you to start the game by clicking.
		start.setFont(new Font("Courier New", Font.PLAIN, 40));
		start.setColor(Color.CYAN);
		start.setLocation ((WIDTH - start.getWidth())/2, (HEIGHT - start.getHeight())/2);
		add(start);
		
		while (startCheck == false); //This is an infinite loop that only breaks when you click--thus, you must click to start.
		remove(start);
	}
	
	private void PlayGame(){
		int lives = NTURNS; //Tracks number of lives remaining.
		boolean bombSurvive = true;
		
		InitBall();
		InitSonar();
		InitDepthCharge();
		VelocityRandomizer();
		
		//This while loop terminates when all the battleships are gone, signifying that the player won.
		while (shipNum != 0 && lives != 0){
			scoreLabel.setLabel("Score: " + score); //This sets the score on the label beneath the submarine.
			
			MovementIterator();
			
			Sonar(); //This method emits the sonar pulses.
			Reveal(); //This method reveals objects that are hit by sonar.
			bombSurvive = DepthCharge(); //The function defines the motion of the bomb, and returns if the bomb killed the sub or not.
			pause(PAUSE/2); //Pause between iterations of the code to allow for time for motion.
			bombRadius.setFillColor(Color.BLACK); //This is placed in between the pauses to make the blast radius indicator flash.
			pause(PAUSE/2);
			
			collider = getCollidingObject(); //This both detects and reacts to collisions, and identifies what the ball collided with.
			
			PointCounter();
			lives = EndMessager(lives, bombSurvive);
		}
	}
	
	private void MovementIterator(){
		//This method defines the motion of the ball
		ball.move(vx, vy);
		
		//These methods define the motion of the battleships
		for (int shipIndex = 0; shipIndex < TOTAL_SHIPS; shipIndex++){
			battleships[shipIndex].move(shipVx[shipIndex], 0);
			if (battleships[shipIndex].getX() > WIDTH - SHIP_WIDTH || battleships[shipIndex].getX() < 0) shipVx[shipIndex] = -shipVx[shipIndex];
		}
	}
	
	private void PointCounter (){
		if (collider instanceof GImage && collider != submarine){
			score = score + POINTS_SHIP;
			remove (collider); //This removes the battleship that got hit.
			shipNum--; //After a battleship gets hit, it is removed, so this logs the total number of battleships remaining.
		}
	}
	
	//This function provides feedback if you lose lives, and tells the game what to do if you do lose a life. It also tells you if you won.
	private int EndMessager(int lives, boolean bombSurvive){
		//This label gives messages like "Game Over" or "Try again" after losing a life.
		GLabel messageLabel = new GLabel("", 0, 0);
		messageLabel.setFont(new Font("Courier New", Font.PLAIN, 15));
		messageLabel.setColor(Color.CYAN);
		messageLabel.setLocation ((WIDTH - messageLabel.getWidth())/2, (HEIGHT - messageLabel.getHeight())/2);
		
		if (ball.getY() > HEIGHT || bombSurvive == false){ //The condition implies the ball fell off the screen, or you were exploded.
			lives--;
			ResetBombLocation();
			
			if (lives > 0){ //i.e. when you're alive...
				if (lives > 1) messageLabel.setLabel("Ouch!  You have " +lives + " lives left. Try again!"); //... tells # of lives left.
				else messageLabel.setLabel("Ouch!  You have " +lives + " life left. Try again!"); //If you only have 1 life, this fixes grammar.
				
				messageLabel.setLocation ((WIDTH - messageLabel.getWidth())/2, (HEIGHT - messageLabel.getHeight())/2); //Centers the label.
				add(messageLabel);
				
				startCheck = false;
				while (startCheck == false); //This is an infinite loop that only ends when you click so that you can decide when to start the next ball.
				
				remove(messageLabel);
				ball.setLocation(WIDTH/2 - BALL_RADIUS, HEIGHT/2 - BALL_RADIUS); //Resets position of the ball.
			}else{
				messageLabel.setLabel("Game Over!");
				messageLabel.setLocation ((WIDTH - messageLabel.getWidth())/2, (HEIGHT - messageLabel.getHeight())/2);
				add(messageLabel);
			}
		}
		if (shipNum == 0) {
			messageLabel.setLabel("Congratulations! You win!");
			messageLabel.setLocation ((WIDTH - messageLabel.getWidth())/2, (HEIGHT - messageLabel.getHeight())/2);
			add(messageLabel);
		}
		
		return lives;
	}
	
	private void InitBall(){
		//These methods set the aesthetic properties of the ball.
		ball.setFilled(true);
		ball.setColor(Color.GRAY);
		add(ball);
	}
	
	private void VelocityRandomizer(){
		//This randomizes the x-direction of the ball & the velocity of the ships so their starting directions are not predictable.
		if (rgen.nextBoolean(0.5)) vx = -vx; //Randomizes initial direction of the ball.
		for (int shipIndex = 0; shipIndex < TOTAL_SHIPS; shipIndex++){ 
			shipVx[shipIndex] = rgen.nextDouble(.1, .3) * .3; //Randomizes speed of the ships
			if (rgen.nextBoolean(0.5)) shipVx[shipIndex] = -shipVx[shipIndex]; //Randomizes initial direction of the ships.
		}
	}
	
	private void Sonar(){
		AudioClip sonarClip = MediaTools.loadAudioClip("sonarping.au"); //Sonar sound
		
		if (sonArc.getHeight() == 0) sonarClip.play(); //Plays when the sonar pulse is initialized
		
		//This defines the propagation of the sonar pulse, which is actually a growing circle.
		if (sonArc.getHeight() <= 2*HEIGHT){ 
			sonArc.setSize(sonArc.getWidth() + 1, sonArc.getHeight() + 1);
			sonArc.setLocation((WIDTH - sonArc.getWidth())/2, HEIGHT - sonArc.getHeight()/2);
		}
		if (sonArc.getHeight() == 2*HEIGHT){ //If the sonar reaches the top, this reinitializes it.
			sonArc.setSize(0, 0);
		}
	}
	
	private void Reveal(){
		/*This for loop iterates over all ships in the array to check if they are within a distance from the sonar pulse.
		 * If they are, they are revealed so long as they remain within SOLAR_TOLERANCE, a set distance from the sonar signal.
		 */
		for (int shipIndex = 0; shipIndex < TOTAL_SHIPS; shipIndex++){
			double distanceFromSub = Distance(battleships[shipIndex].getX(), battleships[shipIndex].getY()); //Fetches the distance from the submarine.
			double distanceFromSonar = sonArc.getHeight()/2 - distanceFromSub; //Fetches the distance from the sonar signal.
			if (0 < distanceFromSonar && distanceFromSonar < SONAR_TOLERANCE){ //Determines if the ship shouldbe revealed
				battleships[shipIndex].setVisible(true);
			}
			else battleships[shipIndex].setVisible(false);
		}
		
		//The same functions as above, but changed to operate on individual objects (the bomb and the blast indicator), not an array.
		double bombDistance = Distance(bomb.getX(), bomb.getY());
		double bombDistFromSonar = sonArc.getHeight()/2 - bombDistance;
		if (0 < bombDistFromSonar && bombDistFromSonar < SONAR_TOLERANCE){
			bomb.setVisible(true);
			bombRadius.setVisible(true);
		}
		else {
			bomb.setVisible(false);
			bombRadius.setVisible(false);
		}
	}
	
	private boolean DepthCharge(){
		//This variable finds the absolute distance between the bomb and the submarine.
		double bombSubDistance = (bomb.getX() - BOMB_WIDTH/2) - (submarine.getX() + SUBMARINE_WIDTH/2); 
		if (bombSubDistance < 0) bombSubDistance = -bombSubDistance;
		
		AudioClip explodeClip = MediaTools.loadAudioClip("explosion.au"); //Explosion quote
		
		//Defines the motion of the bomb & blast indicator.
		bomb.move(0, BOMB_SPEED);
		bombRadius.move(0, BOMB_SPEED);
		
		//If the bomb starts getting close to the sub, it is revealed since it is within viewing distance.
		if (bomb.getY() >= 3 * (HEIGHT - SUBMARINE_Y_OFFSET)/4) { //Y-coordinate is adjusted so the bomb doesn't fall too deep.
			bombRadius.setVisible(true);
			bombRadius.setFillColor(Color.RED);
			bomb.setVisible(true);
		}
		
		//This if-condition tells whether or not the bomb hit the sub when it falls all the way down.
		if (bomb.getY() + BOMB_HEIGHT/2 >= submarine.getY()){
			if (bombSubDistance < BLAST_RADIUS){
				explodeClip.play();
				return false;
			}
			else {
				ResetBombLocation(); //Resets so the bomb can fall again from a new location.
				explodeClip.play();
			}
		}
		return true; //If the bomb didn't explode, the sub is still alive.
	}
	
	//Finds the bomb a new home somewhere in the playing field after it explodes.
	private void ResetBombLocation(){
		bomb.setLocation(rgen.nextDouble(.1, .9) * WIDTH, HEIGHT/2);
		bombRadius.setLocation((bomb.getX() + BOMB_WIDTH/2) - BLAST_RADIUS, (bomb.getY() + BOMB_HEIGHT/2) - BLAST_RADIUS);
		
		//Invisible so its position isn't immediately revealed.
		bomb.setVisible(false);
		bombRadius.setVisible(false);
	}
	
	//Finds distances using the distance formula.
	private double Distance(double x, double y){
		double result = Math.sqrt(Math.pow(WIDTH/2 - x, 2) + Math.pow(HEIGHT - y, 2));
		return result;
	}
	
	private GObject getCollidingObject(){
		//Defines the edges of the ball
		double leftEdge = ball.getX();
		double rightEdge = ball.getX() + 2 * BALL_RADIUS;
		double topEdge = ball.getY();
		double bottomEdge = ball.getY() + 2 * BALL_RADIUS;
		
		WallHit(); //This function tells the ball what to do if it hits a wall.
		
		//Each if statement covers detection from a different corner of the ball's frame.
		if (getElementAt(leftEdge, topEdge) instanceof GImage && getElementAt(leftEdge, topEdge) != bomb){ //Top-left corner.
			BounceMethod(leftEdge, topEdge); 
			return getElementAt(leftEdge, topEdge);
			//These subsequent conditions are all identical to the one above, but specifying different corners of the ball's frame.
		}else if(getElementAt(leftEdge, bottomEdge) instanceof GImage && getElementAt(leftEdge, bottomEdge) != bomb){ //Bottom-left corner
			BounceMethod(leftEdge, bottomEdge);
			return getElementAt(leftEdge, bottomEdge);
		}else if (getElementAt(rightEdge, bottomEdge) instanceof GImage && getElementAt(rightEdge, bottomEdge) != bomb){ //Bottom-right corner
			BounceMethod(rightEdge, bottomEdge);
			return getElementAt(rightEdge, bottomEdge);
		}else if (getElementAt(rightEdge, topEdge) instanceof GImage && getElementAt(rightEdge, topEdge) != bomb){ //Top-right corner
			BounceMethod(rightEdge, topEdge);
			return getElementAt(rightEdge, topEdge);
		}
		else return null;
	}
	
	//This function tells the ball what to do if it hits a wall.
	private void WallHit(){
		//Sound clip that plays when the ball hits a wall.
		AudioClip wallBounceClip = MediaTools.loadAudioClip("tap.au");
		
		//Behavior when the ball hits a wall.
		if (ball.getX() <= 0 || ball.getX() >= WIDTH - 2*BALL_RADIUS){
			vx = -vx;
			wallBounceClip.play();
		}
		if (ball.getY() <= 0){
			vy = -vy;
			wallBounceClip.play();
		}
	}
	/*This function defines what happens when a ball bounces off of battleships or the submarine.
	 * The value that BounceMethod returns tells whether or not the ball collided with the
	 * scoreboard or not, so that it doesn't disappear when the scoreLabel comes into
	 * contact with the ball.
	 */
	private void BounceMethod(double xCollision, double yCollision){
		//Sound effects
		AudioClip submarineBounceClip = MediaTools.loadAudioClip("losine.au");
		AudioClip battleshipBounceClip = MediaTools.loadAudioClip("hisine.au");
		
		//Determines what the ball hits if it touches something horizontally.
		GObject horizontalCheck = checkHorizontalBounce();
		
		//This tells us where _on the submarine_ the ball was hit from so that we can scale x-velocity accordingly.
		double relativeSubmarinePosition = .5 - (ball.getX() - submarine.getX())/SUBMARINE_WIDTH;
		boolean challengeCheck = false; //This tells us whether or not the ball has been sped up after several turns yet.
		
		if (ball.getY() < submarine.getY() - 2 * BALL_RADIUS + 2){ //This condition keeps the ball from bouncing if it already is below the submarine [i.e. you lost].
			if (horizontalCheck == null) vy = -vy; //If the ball hits horizontally, we don't want it to change y-direction.
			if (getElementAt(xCollision, yCollision) == submarine){ //This tells the ball what to do if it hits the submarine specifically.
				vx = -1.5 * relativeSubmarinePosition;
				submarineBounceClip.play();
			if (shipNum <= TOTAL_SHIPS - CHALLENGE) challengeCheck = true;
				if (challengeCheck == true){
					vx = 2*vx;
				}
			}
			else battleshipBounceClip.play();
		}
	}
	
	//This function tells the program what to do if the ball hits a battleship horizontally as opposed to vertically.
	private GObject checkHorizontalBounce(){
		double leftEdge = ball.getX();
		double rightEdge = ball.getX() + 2*BALL_RADIUS;
		double middleHeight = ball.getY() + BALL_RADIUS;
		
		/*We want the ball to just fall down if it hits the submarine on the side since that just means you lost already.
		 * Otherwise, there are more difficult bugs with the submarine moving faster than the ball horizontally
		 * and the submarine forcing the ball into walls, which are easier to deal with by just disallowing that from
		 * happening in the first place.
		 */
		if (getElementAt(leftEdge, middleHeight) != submarine && getElementAt(rightEdge, middleHeight) != submarine){ 
			if (getElementAt(leftEdge, middleHeight) instanceof GImage){
				vx = -vx;
				return getElementAt(leftEdge, middleHeight); //If this function returns an element, the program can interpret this as a horizontal collision.
			}else if(getElementAt(rightEdge, middleHeight) instanceof GImage){
				vx = -vx;
				return getElementAt(rightEdge, middleHeight);
			}
		}
		return null; //If the function returns null, then the collision was not horizontal.
	}
	
	//These are the mouse commands.
	public void mouseClicked(MouseEvent e){
		startCheck = true;
	}
	
	public void mouseMoved(MouseEvent e){
		submarine.setLocation(e.getX()-SUBMARINE_WIDTH/2, SUBMARINE_Y);
		scoreLabel.setLocation(submarine.getX(), SUBMARINE_Y + 2 * SUBMARINE_HEIGHT);
	}
}

