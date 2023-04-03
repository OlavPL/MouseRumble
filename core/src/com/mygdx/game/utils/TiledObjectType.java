package com.mygdx.game.utils;

public enum TiledObjectType {
    ARENA_EDGE (0),
    ARENA_BOUNDS (1),
    COLLISION   (2),
    SPAWN(5);

    public final int objectType;
    private TiledObjectType(int value){
        objectType = value;
    }
}
