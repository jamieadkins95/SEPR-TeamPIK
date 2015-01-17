package com.teampik.game;

import java.util.ArrayList;

public class Inventory {
	//Trains
	ArrayList<Train> trains = new ArrayList<Train>();
	Train selectedTrain = null;
	
	public void selectTrain(int index){
		selectedTrain = trains.get(index);
	}

	public int addTrain(Train train){
		
		if (trains.size() < 3) { //If there is less than 3 Trains in the inventory add the train
			trains.add(train);
			return 1;
			}
		
		else{
			return 0;	//otherwise return 0
		}

		
	}
	
	public void deployTrain(GameMap map, ZooTile zoo){
		if (selectedTrain != null){
			map.deployTraintoTile(zoo.coords,selectedTrain);
			trains.remove(selectedTrain);
		}
	}
	//Powerups
}
