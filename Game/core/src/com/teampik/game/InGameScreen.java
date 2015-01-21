package com.teampik.game;

import java.util.Random;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class InGameScreen implements Screen{
	
	Player currentPlayer;

	final static int endOfTurnProcessing = 0;
	final static int player1Turn = 1;
	final static int player2Turn = 2;

	public int currentState = player1Turn;
	public int turnCount = 0;
	public int turnLimit = 20;
	
	
	//Location of the player's trains
	public ArrayList<Vector2> p1Trains = new ArrayList<Vector2>();
	public ArrayList<Vector2> p2Trains = new ArrayList<Vector2>();
	
	
	//Store the actual train objects used
	public ArrayList<Train> p1TrainsObjects = new ArrayList<Train>();
	public ArrayList<Train> p2TrainsObjects = new ArrayList<Train>();
	public ArrayList<Train> allTrains = new ArrayList<Train>();
	
	//maximum distance which can be moved each turn
	public ArrayList<Integer> p1TrainsMaxMovement = new ArrayList<Integer>();
	public ArrayList<Integer> p2TrainsMaxMovement = new ArrayList<Integer>();
	
	//distance which can be moved this turn; resets at the end of each go
	public ArrayList<Integer> p1TrainsMovement = new ArrayList<Integer>();
	public ArrayList<Integer> p2TrainsMovement = new ArrayList<Integer>();
	
	public ArrayList<Integer> p1Powerups = new ArrayList<Integer>();
	public ArrayList<Integer> p2Powerups = new ArrayList<Integer>(); 	
	
	public Vector2 selectTrain = new Vector2();
	public int mode = 0;
	public int whichTrain;

	InGameUI UI = new InGameUI();	
	
	MyGdxGame game;

	public InGameScreen(MyGdxGame game){
		this.game = game;
		currentPlayer = game.player1;


		currentState = endOfTurnProcessing;

		currentState = player1Turn;
		
		UI = new InGameUI();
		
		//Button listener added here so we take change state easily.
		UI.btnEndTurn.addListener(new ClickListener(){
			@Override 
			public void clicked(InputEvent event, float x, float y){
				currentState = endOfTurnProcessing;
			}
		});

		

	}
	
	public void SwitchToInGameScreen(){ //Called whenever this state is entered. Effectively a reset.
		
		
		turnCount = 1;
		
		currentPlayer = game.player1;

		currentState = player1Turn;

		
		game.player1.changeName(game.mainMenuScreen.UI.tfPlayer1Name.getText()); //Assigns Names from menu text boxes to Players 
		game.player2.changeName(game.mainMenuScreen.UI.tfPlayer2Name.getText());
		
		game.player1.inventory.trains.clear();
		game.player2.inventory.trains.clear();
		game.player1.goals.clear();

		game.player2.goals.clear();
		
		//Clear all trains off map
		for (Train t : game.map.deployedTrains){
			TiledMapTileLayer trainLayer = (TiledMapTileLayer) game.map.getLayers().get(GameMap.trainLayerIndex);

			//Visibly select the tile.
			Cell toBeRemoved = trainLayer.getCell((int)t.location.x, (int) t.location.y);

			if (toBeRemoved != null)
			{
				toBeRemoved.setTile(null);
			}
		}
		
		game.map.deployedTrains.clear();
		
		ProcessEndOfTurn(currentPlayer);
		
		
		RefreshInventory();
		RefreshGoals();
		
		
		Gamestate.MoveToGamestate(Gamestate.IN_GAME);
		game.setScreen(game.inGameScreen);
		game.inputMultiplexer.addProcessor(UI.stage);
		game.inputMultiplexer.removeProcessor(game.mainMenuScreen.UI.stage);
		game.inputMultiplexer.removeProcessor(game.instructionScreen.UI.stage);
		
	}

	@Override
	public void render(float delta) {
		// TODO Auto-generated method stub

		game.batch.begin();
		game.batch.draw(game.imgInGame, 0, 0);
		//context.font.draw(context.batch, coords.toString(), 200, 200);
		game.batch.end();

		game.camera.update();
		game.tiledMapRenderer.setView(game.camera);
		game.tiledMapRenderer.render();

		
		game.batch.begin();
		UI.stage.draw();
		switch (currentState){
		case endOfTurnProcessing:			
			//End of turn processing to be done here.
			

			if (turnCount % 2 == 0){
				currentState = player2Turn;
				currentPlayer = game.player2;
				ProcessEndOfTurn(game.player2);
				
			}
			else{
				currentState = player1Turn;
				currentPlayer = game.player1;
				ProcessEndOfTurn(game.player1);
				
			}
			//EndRegion
			break;
		case player1Turn:
			//game.batch.draw(game.labelBackgroundRed,Gdx.graphics.getWidth() - 260f, Gdx.graphics.getHeight() - 20f); //Player 1 is Red
			currentPlayer = game.player1;
			break;
			
		case player2Turn:
			currentPlayer = game.player2;
			break;
		}
		game.batch.end();

		RefreshUI();

	}

	//Refresh the UI
	public void RefreshUI(){
		

		UI.lblPlayer.setText(currentPlayer.playerName + "'s (Player " + currentState + "'s) turn");


		UI.lblPlayer.setText("Player " + currentState + "\n" + currentPlayer.playerName + "'s turn");
		
	}

	public void ProcessEndOfTurn(Player player){ 
		//UI.clearInventory();
		
		UI.clearGoal();
		System.out.println("Turn " + (turnCount) + " just ended. Turn " + (turnCount+1) + " is now starting.");
		
		if (turnCount == turnLimit){			
			game.mainMenuScreen.SwitchToMainMenuScreen();
		}
		else {
			turnCount++;
			
			for (Goal g : player.getAllGoals()){
				g.goalTurnCount++;
			}
			
		}
		
		//Create and give a goal to the next player.
		Random rdm = new Random();		
		int randomTrainInt = rdm.nextInt(5);		
		player.inventory.addTrain(new Train(game.trTrains[randomTrainInt][player.playerNumber], Train.trainType.values()[randomTrainInt], player));
		refreshMovement();
		
		//RefreshInventory();
		/*
		for (Train t : player.inventory.trains){

			UI.addToInventory(player,t);
		} */
		

		
		int ranNumber = rdm.nextInt(4);
		Goal g = new Goal(ranNumber, game.map, player);
		player.addGoal(g);
		
		for (Goal goal : player.goals){ 
			UI.addToGoals(player, goal);
			System.out.println(goal.toString()); 
		}
		
		RefreshInventory();
	}
	
	public ArrayList<Vector2> findNeighbours(Vector2 tile){
		//Gives the neighbouring cells of the input cell which have track on them
		ArrayList<Vector2> targets = new ArrayList<Vector2>();
		ArrayList<Vector2> track = new ArrayList<Vector2>();
		if (tile.x % 2 == 0){	//Find neighbours
			targets.add(new Vector2(tile.x, tile.y+1));
			targets.add(new Vector2(tile.x, tile.y-1));
			targets.add(new Vector2(tile.x+1, tile.y+1));
			targets.add(new Vector2(tile.x-1, tile.y+1));
			targets.add(new Vector2(tile.x+1, tile.y));
			targets.add(new Vector2(tile.x-1, tile.y));
		}
		else{
			targets.add(new Vector2(tile.x, tile.y+1));
			targets.add(new Vector2(tile.x, tile.y-1));
			targets.add(new Vector2(tile.x+1, tile.y-1));
			targets.add(new Vector2(tile.x-1, tile.y-1));
			targets.add(new Vector2(tile.x+1, tile.y));
			targets.add(new Vector2(tile.x-1, tile.y));
		}
		track = game.getTrackList();
		for (int i=0; i<targets.size();i++){
			if (track.contains(targets.get(i))==false){
				targets.remove(i);
			}
		}
		return targets;
	}
	
	public Vector2 findSelected(Vector2 tile){
		//Looks if the tile selected has a train on it
		//Returns id of train in array and the player who owns it
		int player = 0;
		int which = 0;
		for (int i=0;i<p1Trains.size();i++){
			if (tile.x == p1Trains.get(i).x && tile.y == p1Trains.get(i).y){
				which = i;				
				player = 1;
			}
		}
		for (int i=0;i<p2Trains.size();i++){
			if (tile.x == p2Trains.get(i).x && tile.y == p2Trains.get(i).y){
				which = i;
				player = 2;
			}
		}		
		return new Vector2(which,player);
	}
	
	public void doMovement(ArrayList<Vector2> trainList, ArrayList<Train> trainObjects, ArrayList<Integer> movement, Vector2 tile, int whichTrain){
		// This method moves the train and renders it on the map
		ArrayList<Vector2> targets = new ArrayList<Vector2>();
		targets = findNeighbours(tile);
		if (trainList.size() > 0){				
			targets = findNeighbours(trainList.get(whichTrain));				
			System.out.println("Train is at" + trainList.get(whichTrain));
			System.out.println("The targets are" + targets);
			for (int i=0; i<trainList.size();i++){
				for (int j=0; j<targets.size();j++){					
					if (tile.x == targets.get(j).x && tile.y == targets.get(j).y){
						if (movement.get(whichTrain) > 0){
							System.out.println("Moving to" + tile);	
							game.map.removeTrainTile(trainList.get(whichTrain));
							trainList.set(whichTrain, new Vector2(tile));	//Move train							
							//game.map.deployTraintoTile(new Vector2(tile),allTrains.get(whichTrain));
							game.map.deployTraintoTile(new Vector2(tile),trainObjects.get(whichTrain));
							movement.set(whichTrain, movement.get(whichTrain)-1);
						}
						else{
							System.out.println("Train out of movement");
						}													
					}
				}			
			}			
		}
	}
	
	public void refreshMovement(){
		//Gives trains movement distance again at the end of the turn
		p1TrainsMovement = new ArrayList<Integer>(p1TrainsMaxMovement);
		p2TrainsMovement = new ArrayList<Integer>(p2TrainsMaxMovement);
	}
	
	public void moveTrain(Vector2 tile){
		//This method handles the movement of the trains
		
		System.out.println(allTrains.size());		
		Vector2 playerTrain = new Vector2();
		System.out.println("tile is" + tile);		

		playerTrain = findSelected(tile);		//which = x, player = y
			
		if (currentPlayer == game.player1){	//Player 1's turn
			doMovement(p1Trains,p1TrainsObjects,p1TrainsMovement,tile,(int) playerTrain.x);
		}
		else{
			System.out.println("player 2 has trains??");
			doMovement(p2Trains,p2TrainsObjects,p2TrainsMovement,tile,(int) playerTrain.x);
		}
	}


	public void selectTile(int x, int y){

		Vector3 cameraPosition = game.camera.position;

		Vector2 tileCoords = GameMap.getCoordsFromPoint(x, y, game.camera);

		TiledMapTileLayer selectedLayer = (TiledMapTileLayer) game.map.getLayers().get(GameMap.selectedTileLayerIndex);


		//Visibly select the tile.
		Cell toBeRemoved = selectedLayer.getCell((int)game.currentlySelectedTile.x, (int) game.currentlySelectedTile.y);

		if (toBeRemoved != null)
		{
			toBeRemoved.setTile(null);
		}

		Cell cell = new Cell();
		cell.setTile(new MapTile(game.trSelected));		
		selectedLayer.setCell((int) tileCoords.x,  (int) tileCoords.y, cell);

		game.currentlySelectedTile = tileCoords.cpy();
		
		
		Train train = (Train) game.map.getTile((int)tileCoords.x, (int)tileCoords.y, GameMap.trainLayerIndex);
		MapTile tile = game.map.getTile((int)tileCoords.x, (int)tileCoords.y, GameMap.baseLayerIndex);
		ZooTile ztile = (ZooTile) game.map.getTile((int)tileCoords.x, (int)tileCoords.y, GameMap.zooLayerIndex);
		TrackTile ttile = (TrackTile) game.map.getTile((int)tileCoords.x, (int)tileCoords.y, GameMap.trackLayerIndex[Direction.MIDDLE]);

		//Debug, outputs tile info to console.
		
		moveTrain(tileCoords);

				
		System.out.println("\n" + tileCoords.toString());
		System.out.println("Mouse position : " + Integer.toString(x) + ", " + Integer.toString(y));
		System.out.println("Camera position : " + cameraPosition.toString());		

		
		
		//Since there is lots of things on the same tile, checking what has been clicked on is cascaded down. Train then zoo then track then maptile.
		if (train != null){
			//Select train and move it when new tile is selected.
			
			Vector2 newTile = p1Trains.get(0);
			System.out.println("We want a train at" + newTile);			
			
		}
		
		else if (ztile != null){
			System.out.println(ztile.toString());	
			
			//Deploy Train if it has been selected
			if (currentPlayer.inventory.selectedTrain != null){
				int distance = 0;
				
				switch(currentPlayer.inventory.selectedTrain.type){	//Set movement distances
					case HOVER:
						distance = 10;						
						break;
					case BULLET:
						distance = 8;
						break;
					case ELECTRIC:
						distance = 6;
						break;
					case DIESEL:
						distance = 4;
						break;
					case STEAM:
						distance = 2;
						break;
				}
				
				game.map.deployTraintoTile(tileCoords, currentPlayer.inventory.selectedTrain);
				if (currentPlayer == game.player1){
					p1TrainsObjects.add(currentPlayer.inventory.selectedTrain);
				}
				else{
					p2TrainsObjects.add(currentPlayer.inventory.selectedTrain);
				}
				
				//allTrains.add(currentPlayer.inventory.selectedTrain);
				//currentPlayer.inventory.trains.remove(currentPlayer.inventory.selectedTrain);				
				game.map.deployedTrains.add(currentPlayer.inventory.selectedTrain);
				currentPlayer.inventory.selectedTrain.setLocation(tileCoords);
				currentPlayer.inventory.selectedTrain = null;
							
				if (currentPlayer == game.player1){
					p1Trains.add(new Vector2(tileCoords));
					p1TrainsMovement.add(new Integer(distance));
					p1TrainsMaxMovement.add(new Integer(distance));
				}
				else{
					p2Trains.add(new Vector2(tileCoords));
					p2TrainsMovement.add(new Integer(distance));
					p2TrainsMovement.add(new Integer(distance));
				}
				
				RefreshInventory();
				RefreshGoals();
				System.out.println("Train deployed");
				System.out.println("Player 1 has trains at" + p1Trains);
				System.out.println("Player 1 has trains which go" + p1TrainsMovement);
				System.out.println("Player 2 has trains at" + p2Trains);
				System.out.println("Player 2 has trains which go" + p2TrainsMovement);
			}
		}
		else if (ttile != null){
			System.out.println(ttile.toString());
		}
		else{		
			//If the tile has a border then display that info, otherwise don't
			if (tile != null)
			{
				if (tile.borders[0] || tile.borders[1] || tile.borders[2] || tile.borders[3] || tile.borders[4] || tile.borders[5]){
					System.out.println(tile.toString());
				}	
			}
		}
	}
	
	public void RefreshInventory(){
		UI.clearInventory();
		UI.addToInventory(currentPlayer);
		System.out.println(currentPlayer.playerNumber+" player number after inventory refresh");
	}
	public void RefreshGoals(){
		UI.clearGoal();
		for (Goal goal : currentPlayer.goals){
			UI.addToGoals(currentPlayer,goal);
		}
	}
	
	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub


	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
