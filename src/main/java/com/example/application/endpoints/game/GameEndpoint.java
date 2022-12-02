package com.example.application.endpoints.game;

import com.example.application.component.GameGenerator;
import com.example.application.model.Game;
import com.example.application.model.GameLevel;
import com.example.application.service.GameService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import dev.hilla.Endpoint;
import lombok.RequiredArgsConstructor;

@Endpoint
@AnonymousAllowed
@RequiredArgsConstructor
public class GameEndpoint {
    
    private final GameService gameService;
    public Game createGame(GameLevel level){
        return gameService.create(level);
    }

    public void update(Game game){
        gameService.update(game);
    }
    public Game getCurrent(String id){
        return gameService.getCurrent(id);
    }
}
