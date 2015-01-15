package com.teampik.game;


import java.util.ArrayList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.HexagonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;


public class MyGdxGame extends Game {
	SpriteBatch batch;
	Texture imgLoading;
	Texture img;
	Texture img2;
	
	Texture labelBackgroundRed;
	Texture labelBackgroundBlue;
	
	Texture player1Turn;
	Texture player2Turn;
	Texture endOfTurn;
	
	TextureRegion trDefault;
	TextureRegion trWater;
	TextureRegion trLand;
	TextureRegion trTrack;
	TextureRegion trZoo;
	TextureRegion trSelected;
	TextureRegion[] trBorders = new TextureRegion[6];
	Vector2 currentlySelectedTile = new Vector2(-1,-1);
	
	TiledMap tiledMap;
    OrthographicCamera camera;
    TiledMapRenderer tiledMapRenderer;
    
    LoadingScreen loadingScreen;
    MainMenuScreen mainMenuScreen;
    InGameScreen inGameScreen;
    BitmapFont font;
    
    GameMap map;
    Vector3 cameraInitPos;
    
    Player player1;
    Player player2;
 
    
    InputMultiplexer inputMultiplexer = new InputMultiplexer();
	
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		imgLoading = new Texture("tempSplashscreenLoading.png");
		img = new Texture("tempSplashscreen.png");
		img2 = new Texture("tempInGame.png");
		
		loadingScreen = new LoadingScreen(this);
		mainMenuScreen = new MainMenuScreen(this);
        inGameScreen = new InGameScreen(this);
        
        Gamestate.MoveToGamestate(Gamestate.LOADING);
        setScreen(loadingScreen);
		
		
		LoadAssets();

       
        
        Gamestate.MoveToGamestate(Gamestate.MAIN_MENU);
        setScreen(mainMenuScreen);
       
        
        
        
	}

	private void LoadAssets() {
		
		
		
		font = new BitmapFont();
        font.setColor(Color.RED);
        
		InputChecker inputProcessor = new InputChecker(this);
		
		
        inputMultiplexer.addProcessor(inputProcessor);
        
		Gdx.input.setInputProcessor(inputMultiplexer);
		
		float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false,w,h);
        cameraInitPos = camera.position;
        
        
        
        camera.update();
        
        player1Turn = new Texture("Turns/player1.png");
        player2Turn = new Texture("Turns/player2.png");
        endOfTurn = new Texture("Turns/endofturn.png");
        
        
        
        trDefault = new TextureRegion(new Texture("perfectHexagon.png"));
        trWater = new TextureRegion(new Texture("perfectHexagonBlue.png"));
        trLand = new TextureRegion(new Texture("perfectHexagonGreen.png"));
        trTrack = new TextureRegion(new Texture("track.png"));
        trZoo = new TextureRegion(new Texture("zoo.png"));
        trSelected = new TextureRegion(new Texture("perfectHexagonSelected.png"));
        trBorders[Direction.NORTH] = new TextureRegion(new Texture("Borders/borderNorth.png"));
        trBorders[Direction.NORTH_EAST] = new TextureRegion(new Texture("Borders/borderNorthEast.png"));
        trBorders[Direction.SOUTH_EAST] = new TextureRegion(new Texture("Borders/borderSouthEast.png"));
        trBorders[Direction.SOUTH] = new TextureRegion(new Texture("Borders/borderSouth.png"));
        trBorders[Direction.SOUTH_WEST] = new TextureRegion(new Texture("Borders/borderSouthWest.png"));
        trBorders[Direction.NORTH_WEST] = new TextureRegion(new Texture("Borders/borderNorthWest.png"));
        
        labelBackgroundRed = new Texture("RED.png");
        labelBackgroundBlue = new Texture("BLUE.png");
        
                
        
        MapLayout m = new MapLayout(this, GetTestTileLayout(), GetTestBorderList(), GetTestTrackList(), GetTestZooList(), 45, 30);
        
        map = GameMap.createMap(this, m);
        tiledMapRenderer = new HexagonalTiledMapRenderer(map);
        
        player1 = new Player();
        player2 = new Player();
	}
	
	
	//Just an example list of borders
	private ArrayList<Vector2>[] GetTestBorderList(){
		ArrayList<Vector2>[] lstBorderCoords = (ArrayList<Vector2>[]) new ArrayList[6];
		
		for (int i = Direction.NORTH; i <= Direction.NORTH_WEST; i++){
        	lstBorderCoords[i] = new ArrayList<Vector2>();
        }
		
		// NORTH borders
		lstBorderCoords[Direction.NORTH].add(new Vector2(9,7));
		
		// SOUTH borders
		for (Vector2 v : lstBorderCoords[Direction.NORTH]){
			if ((int)v.y != 29) {
				lstBorderCoords[Direction.SOUTH].add(new Vector2((int)v.x,(int)v.y+1));
			}
        }
		
		// NORTHWEST borders
		lstBorderCoords[Direction.NORTH_WEST].add(new Vector2(1,1));
		lstBorderCoords[Direction.NORTH_WEST].add(new Vector2(1,2));
		lstBorderCoords[Direction.NORTH_WEST].add(new Vector2(2,1));
		lstBorderCoords[Direction.NORTH_WEST].add(new Vector2(2,2));
		
		// SOUTHEAST borders
		for (Vector2 v : lstBorderCoords[Direction.SOUTH_EAST]){
			if ((int)v.x > 0 && (int)v.y < 29) {
				if ((int)v.x/2 == 0) {
					if ((int)v.y/2 == 0) {
						lstBorderCoords[Direction.SOUTH_EAST].add(new Vector2((int)v.x+1,(int)v.y+1));
					} else {
						lstBorderCoords[Direction.SOUTH_EAST].add(new Vector2((int)v.x+1,(int)v.y-1));
					}
				} else {
					lstBorderCoords[Direction.SOUTH_EAST].add(new Vector2((int)v.x+1,(int)v.y+1));
				}
			}
        }
		
		// NORTHEAST borders
		lstBorderCoords[Direction.NORTH_EAST].add(new Vector2(1,1));
		lstBorderCoords[Direction.NORTH_EAST].add(new Vector2(1,2));
		lstBorderCoords[Direction.NORTH_EAST].add(new Vector2(2,1));
		lstBorderCoords[Direction.NORTH_EAST].add(new Vector2(2,2));
				
		// SOUTHWEST borders
		for (Vector2 v : lstBorderCoords[Direction.NORTH_EAST]){
			if ((int)v.x < 44 && (int)v.y < 29) {
				if ((int)v.x/2 == 0) {
					if ((int)v.y/2 == 0) {
						lstBorderCoords[Direction.SOUTH_WEST].add(new Vector2((int)v.x+1,(int)v.y+1));
					} else {
						lstBorderCoords[Direction.SOUTH_WEST].add(new Vector2((int)v.x+1,(int)v.y-1));
					}
				} else {
					lstBorderCoords[Direction.SOUTH_WEST].add(new Vector2((int)v.x+1,(int)v.y+1));
				}
			}
		}
		
		
		
		
		
        
		
		return lstBorderCoords;
		
	}
	
	//Example map layout.
	private int[][] GetTestTileLayout(){
		
		int w = MapLayout.WATER;
		int l = MapLayout.LAND;
		int z = MapLayout.ZOO;
		int t = MapLayout.TRACK;
		int s = MapLayout.SNOW;
		int d = MapLayout.DESERT;
		int f = MapLayout.FORREST;
		int m = MapLayout.MOUNTAIN;
		
		int[][] layout = new int[30][45];
		
		// j = 0 : far lefthand column
		// i = 0 : top row
		
		// initially cover entire map with water
		for (int i = 0; i < 30; i++) {
			for (int j = 0; j < 45; j ++){
				layout[i][j] = w;
			}
		}
		
		// j = 0
		for (int i = 24; i < 27; i++) {layout[i][0] = l;}
		// j = 1
		for (int i = 22; i < 27; i++) {layout[i][1] = l;}
		// j = 2
		for (int i = 18; i < 28; i++) {layout[i][2] = l;}
		// j = 3
		for (int i = 17; i < 27; i++) {layout[i][3] = l;}
		// j = 4
		for (int i = 19; i < 28; i++) {layout[i][4] = l;}
		// j = 5
		for (int i = 18; i < 27; i++) {layout[i][5] = l;}
		// j = 6
		for (int i = 6; i < 7; i++) {layout[i][6] = l;}
		for (int i = 19; i < 28; i++) {layout[i][6] = l;}
		// j = 7
		for (int i = 5; i < 7; i++) {layout[i][7] = l;}
		for (int i = 20; i < 27; i++) {layout[i][7] = l;}
		// j = 8
		for (int i = 0; i < 23; i++) {layout[i][8] = w;}
		for (int i = 20; i < 30; i++) {layout[i][8] = w;}
		// j = 9
		for (int i = 0; i < 21; i++) {layout[i][9] = w;}
		for (int i = 26; i < 30; i++) {layout[i][9] = w;}
		// j = 10
		for (int i = 0; i < 18; i++) {layout[i][10] = w;}
		for (int i = 28; i < 30; i++) {layout[i][10] = w;}
		// j = 11
		for (int i = 0; i < 18; i++) {layout[i][11] = w;}
		for (int i = 28; i < 30; i++) {layout[i][11] = w;}
		// j = 12
		for (int i = 0; i < 23; i++) {layout[i][12] = w;}
		for (int i = 26; i < 30; i++) {layout[i][12] = w;}
		// j = 13
		for (int i = 0; i < 21; i++) {layout[i][13] = w;}
		for (int i = 26; i < 30; i++) {layout[i][13] = w;}
		// j = 14
		for (int i = 0; i < 18; i++) {layout[i][14] = w;}
		for (int i = 28; i < 30; i++) {layout[i][14] = w;}
		// j = 15
		for (int i = 0; i < 18; i++) {layout[i][15] = w;}
		for (int i = 28; i < 30; i++) {layout[i][15] = w;}
		
		return layout;
	}
		
	
	
	private ArrayList<Vector2> GetTestTrackList(){
		ArrayList<Vector2> trackCoords = new ArrayList<Vector2>();
		
		trackCoords.add(new Vector2(5,11));
		trackCoords.add(new Vector2(6,11));
		trackCoords.add(new Vector2(7,11));
		trackCoords.add(new Vector2(8,11));
		trackCoords.add(new Vector2(9,11));
		
		return trackCoords;
		
	}
	
	private ArrayList<Vector2> GetTestZooList(){
		ArrayList<Vector2> zooCoords = new ArrayList<Vector2>();
		
		zooCoords.add(new Vector2(10,10));
		zooCoords.add(new Vector2(4,11));
		zooCoords.add(new Vector2(1,1));
		
		return zooCoords;
		
	}
	

	
}
