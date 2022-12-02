package com.example.application.component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.example.application.model.Game;
import com.example.application.model.GameLevel;
import com.example.application.model.Tile;

public class GameGenerator {


    public static Game create(GameLevel level){
        int matrixSize = 4;
        switch(level){
            case EASY:
                matrixSize = 4;
                break;
            case MEDIUM:
                matrixSize = 6;
                break;
            case HARD:
                matrixSize = 10;
                break;
        }
        List<Integer> integerList = new ArrayList<>();
        int numberSize = matrixSize * matrixSize ;
        numberSize /= 2;
        for (int i = 0; i < numberSize; i++) {
            integerList.add(i + 1);
            integerList.add(i + 1);
        }
        Game game = new Game();
        game.setId(UUID.randomUUID().toString());
        game.setMatrixSize(matrixSize);
        game.setStartDateTime(LocalDateTime.now());
        game.setLevel(level);
        Random rnd = new Random();
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                Tile tile = new Tile();
                tile.setId(UUID.randomUUID().toString());
                tile.setRow(i);
                tile.setCol(j);
                int index = rnd.nextInt(integerList.size());
                System.out.println(index);
                tile.setValue(integerList.get(index));
                integerList.remove(index);
                tile.setVisible(false);
                game.getTiles().add(tile);
            }
        }
        return game;
    }
}
