/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rummikub.implementation;

import java.util.Objects;


/**
 *
 * @author Arthur
 */
public class PlayerId {
  
    private final String playerName;
    private final String gameName;
    private final int indexOfPlayerInHisGame;
    private final int playerId;

    public PlayerId(String playerName, String gameName, int indexOfPlayerInHisGame) {
        this.playerName = playerName;
        this.gameName = gameName;
        this.indexOfPlayerInHisGame = indexOfPlayerInHisGame;
        this.playerId = generatePlayerId();
    }
    
    private int generatePlayerId() {
        return hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.playerName);
        hash = 41 * hash + Objects.hashCode(this.gameName);
        hash = 41 * hash + this.indexOfPlayerInHisGame;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlayerId other = (PlayerId) obj;
        if (!Objects.equals(this.playerName, other.playerName)) {
            return false;
        }
        if (!Objects.equals(this.gameName, other.gameName)) {
            return false;
        }
        if (this.indexOfPlayerInHisGame != other.indexOfPlayerInHisGame) {
            return false;
        }
        return true;
    }
    
    public int getPlayerId() {
        return this.playerId;
    }
    
    public String getPlayerName() {
        return this.playerName;
    }
//    
//    public static int getPlayerId(String playerName, String gameName, int indexOfPlayerInHisGame) {
//        PlayerId playerId = new PlayerId(playerName, gameName, indexOfPlayerInHisGame);
//        return playerId.hashCode();
//    }
    
}
