package com.example.application.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tile {
    private String id;
    private int row;
    private int col;
    private int value;
    private boolean visible;
    private boolean found;
}
