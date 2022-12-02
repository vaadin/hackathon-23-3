package com.example.application.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Game {
    private String id;
    private int matrixSize;
    private LocalDateTime startDateTime;
    private GameLevel level;
    private List<Tile> tiles = new ArrayList<>();
    private int clickCount = 0;
}
