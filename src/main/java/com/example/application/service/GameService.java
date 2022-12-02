package com.example.application.service;

import java.util.Map;
import java.util.WeakHashMap;

import org.springframework.stereotype.Service;

import com.example.application.component.GameGenerator;
import com.example.application.model.Game;
import com.example.application.model.GameLevel;

@Service
public class GameService {
    
    private Map<String, Game> gameMap = new WeakHashMap<>();

    public Game create(GameLevel level){
        Game game = GameGenerator.create(level);
        gameMap.put(game.getId(), game);
        return game;
    }
    public Game getCurrent(String id){
        if(gameMap.containsKey(id)){
            return gameMap.get(id);
        }
        return null;
    }
    public void update(Game game){
        gameMap.put(game.getId(), game);
    }
}
