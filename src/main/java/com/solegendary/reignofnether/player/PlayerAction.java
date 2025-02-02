package com.solegendary.reignofnether.player;

// actions that can be done to Player entities serverside
public enum PlayerAction {
    TELEPORT,
    ENABLE_ORTHOVIEW,
    DISABLE_ORTHOVIEW,
    START_RTS_VILLAGERS,
    START_RTS_MONSTERS,
    START_RTS_PIGLINS,
    START_RTS_SANDBOX,
    DEFEAT,
    VICTORY,
    DISABLE_RTS,
    ENABLE_RTS,
    RESET_RTS,
    SYNC_RTS_GAME_TIME,
    LOCK_RTS,
    UNLOCK_RTS,
    ENABLE_START_RTS,
    DISABLE_START_RTS,
    DISABLE_RTS_SYNCING,
    ENABLE_RTS_SYNCING,
    SYNC_MAX_POPULATION,
    SET_MIN_ORTHOVIEW_Y,
    SYNC_NEUTRAL_AGGRO,
    ENABLE_SANDBOX
}
