package com.codenjoy.dojo.battlecity.client;

public enum GameType {
    USUALLY(0), SIMPLE(1), MOVEANDSHOOT(2), DEFEND(3), HELP(100);
    private int value;

    public static GameType getByInt(int v) {
        for (GameType gameType : GameType.values()) {
            if (gameType.value == v) return gameType;
        }
        return USUALLY;

    }

    GameType(int value) {
        this.value = value;
    }
}
