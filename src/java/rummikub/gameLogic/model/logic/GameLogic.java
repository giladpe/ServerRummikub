/*
 * this class deals with the logic of the game.
 * it deals with moves of the player, when games ends and enforces the game rules
 */
package rummikub.gameLogic.model.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import rummikub.gameLogic.model.gameobjects.Board;
import rummikub.gameLogic.model.gameobjects.Heap;
import rummikub.gameLogic.model.gameobjects.Tile;
import rummikub.gameLogic.model.player.ComputerPlayer;
import rummikub.gameLogic.model.player.HumanPlayer;
import rummikub.gameLogic.model.player.Player;


public class GameLogic {

    // Constants
    private static final boolean MOVE_DONE = true;
    
    // Data members
    private int indexOfCurrentPlayer;
    private boolean isGameOver;
    private boolean isTie;
    private Settings gameSettings;
    private Settings gameOriginalInputedSettings;
    private ArrayList<Player> players;
    private Player currentPlayer;
    private Board gameBoard;
    private final Heap gameHeap;
    
    // Constractor
    public GameLogic() {
        gameSettings = null;
        players = new ArrayList<>();
        indexOfCurrentPlayer = 0;
        currentPlayer = new HumanPlayer("");
        gameBoard = new Board();
        gameHeap = Heap.getInstance();
        isTie = isGameOver = false;
    }    

    // Getter && Setter
    public Settings getGameSettings() {
        return gameSettings;
    }
    
    public ArrayList<Player> getPlayers() {
        return players;
    }
    
    public void setGameSettings(Settings gameSettings) {
        this.gameSettings = gameSettings;
    }
    
    public void setGameOriginalInputedSettings(Settings gameSettings) {
        this.gameOriginalInputedSettings = gameSettings;
    }
    
    public boolean isTie() {
        return isTie;
    }
    
    public Board getGameBoard() {
        return gameBoard;
    }
    
    public Heap getHeap() {
        return gameHeap;
    }
    
    public Settings getGameOriginalInputedSettings() {
        return gameOriginalInputedSettings;
    }
    
    // Private Methods
    private boolean isAtleastOnePlayerMoved() {
        boolean isAtleastOneMoved = false;
        for (Iterator<Player> it = players.iterator(); it.hasNext() && !isAtleastOneMoved;) {
            Player player = it.next();
            isAtleastOneMoved = player.isNormalMoveDone();
        }
        return isAtleastOneMoved;
    }
    
    private void addTileToCurrentPlayerHand() {
        if(!this.gameHeap.isEmptyHeap()){
            currentPlayer.AddTileToPlayersTiles(gameHeap.getRandomTile());
        }
    }

    private boolean pickOneTilesWhenIllegalMove() {
        boolean turnSucceded = false;

        addTileToCurrentPlayerHand();
        this.currentPlayer.setNormalMoveDone(!MOVE_DONE);
        
        return turnSucceded;
    }
        
    private boolean pickThreeTilesWhenIllegalMove() {
        boolean turnSucceded = false;

        addTileToCurrentPlayerHand();
        addTileToCurrentPlayerHand();
        addTileToCurrentPlayerHand();
        this.currentPlayer.setNormalMoveDone(!MOVE_DONE);
        
        return turnSucceded;
    }
    
    private boolean executePlayerFirstMoveLogic(PlayersMove currentPlayerMove) {
        boolean isFirstMoveValid = currentPlayerMove.isValidMove() &&
                                   currentPlayerMove.has30PointsSeriesMove();

        if(isFirstMoveValid) {
           this.gameBoard = currentPlayerMove.getBoardAfterMove();
           this.currentPlayer.setListPlayerTiles(currentPlayerMove.getHandAfterMove()); 
           this.currentPlayer.setFirstMoveDone(MOVE_DONE);
        }
        else {
            pickOneTilesWhenIllegalMove();
        }
        
        return isFirstMoveValid;
    }
    
    private boolean executePlayerNormalMoveLogic(PlayersMove currentPlayerMove) {
        boolean validMove = currentPlayerMove.isValidMove();
        boolean turnSucceded = validMove && currentPlayerMove.isUsedAtleastOneTile();
        
        if (turnSucceded) {
            this.gameBoard = currentPlayerMove.getBoardAfterMove();
            this.currentPlayer.setListPlayerTiles(currentPlayerMove.getHandAfterMove());
            this.currentPlayer.setNormalMoveDone(MOVE_DONE);
        } 
        else if(validMove){
            turnSucceded = pickOneTilesWhenIllegalMove();
        }
        else{
            turnSucceded = pickThreeTilesWhenIllegalMove();
        }
        
        return turnSucceded;
    }
    
    private ArrayList<Player> createPlayers() {
        ArrayList<Player> playersList = new ArrayList<>();
        
        createComputerPlayers(playersList);
        createHumanPlayers(playersList);
        initAllPlayers(playersList);
        Collections.shuffle(playersList);

        return playersList;
    }
    
    private void createComputerPlayers(ArrayList<Player> playersList) {
        for (int i = 0; i < this.gameSettings.getNumOfCpuPlayers(); i++) {
            playersList.add(new ComputerPlayer());
        }
    }
    
    private void createHumanPlayers(ArrayList<Player> playersList) {
        for (int i = 0; i < this.gameSettings.getNumOfHumanPlayers(); i++) {
            playersList.add(new HumanPlayer(this.gameSettings.getHumanPlayersNames().get(i)));
        }
    }
    
    private void initAllPlayers(ArrayList<Player> playersList) {
        playersList.stream().forEach((currPlayer) -> { currPlayer.initPlayer(this.gameHeap.getNewHandFromHeap()); });
    }

    
    private void initGameSettingsFromFile(String gameName) {
        
        int totalNumberOfPlayer =  this.players.size();
        int numOfComputerPlayers = 0;
        ArrayList<String> playerNames = new ArrayList<>();
       
        for (Player player : players) {
            
            if (!player.getIsHuman()) {
                numOfComputerPlayers++;
            }
            
            playerNames.add(player.getName());
        }
        
        this.gameSettings = new Settings(gameName, totalNumberOfPlayer, numOfComputerPlayers, playerNames);
        this.gameOriginalInputedSettings = new Settings(gameName, totalNumberOfPlayer, numOfComputerPlayers, playerNames);
    }

    private void initHeapFromFile() {
        ArrayList<Tile> usedTiles = new ArrayList<>();
        
        players.stream().forEach((player) -> { usedTiles.addAll(player.getListPlayerTiles()); });
        usedTiles.addAll(this.gameBoard.getArrayListOfBoardTiles());
        this.gameHeap.resetTiles();
        
        usedTiles.stream().forEach((usedTile) -> { this.gameHeap.remove(usedTile); });
    }
    
    private void initGameSetting() {
        this.gameSettings = new Settings(this.gameOriginalInputedSettings.getGameName(),
                                         this.gameOriginalInputedSettings.getNumOfPlayers(),
                                         this.gameOriginalInputedSettings.getNumOfCpuPlayers(),
                                         this.gameOriginalInputedSettings.getHumanPlayersNames());
        this.gameHeap.resetTiles();
        this.indexOfCurrentPlayer = 0;
        
        if(!this.players.isEmpty()) {
            this.currentPlayer = this.players.get(indexOfCurrentPlayer);
        }
        this.gameBoard.initBoard();
        this.isGameOver = false;
        this.isTie = false;
    }
    
    // Public Methods
    public void clearGameSettings(){
        this.gameSettings = null;
    }
    
    public boolean playSingleTurn(PlayersMove currentPlayerMove) {
        boolean turnSucceded;
        
        if(currentPlayerMove.getIsTurnSkipped()) {
            turnSucceded = pickOneTilesWhenIllegalMove();
        }
        else if(currentPlayer.isFirstMoveDone()) {
            turnSucceded = executePlayerNormalMoveLogic(currentPlayerMove);
        }
        else{
            turnSucceded = executePlayerFirstMoveLogic(currentPlayerMove);
        }
        return turnSucceded;
    }
    
    public void swapTurns() {
        this.indexOfCurrentPlayer++;
        
        if (this.indexOfCurrentPlayer >= this.players.size()) {
            this.indexOfCurrentPlayer = 0;
        }
        
        this.currentPlayer = this.players.get(this.indexOfCurrentPlayer);
    }
    
    public boolean isGameOver() {
        boolean foundWinner = this.currentPlayer.getListPlayerTiles().size() == Player.EMPTY_HAND;
        
        this.isGameOver = foundWinner;
        
        if (!foundWinner){

            this.isTie = this.gameHeap.isEmptyHeap() && !isAtleastOnePlayerMoved();
            if (this.isTie){
                this.isGameOver = this.isTie;
            }
        }
        else{
            this.isTie = false;
        }
        
        return this.isGameOver;
    }

    // need to cheack if there is no tie
    public Player getWinner() {
        return currentPlayer;
    }
    
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    
    public void initGameFromUserSettings() {
        initGameSetting();
        this.players = createPlayers();
    }
    
    public void removeCurrentPlayerFromTheGame() {
        this.players.remove(currentPlayer);
        this.gameSettings.removePlayerFromGame(currentPlayer.getIsHuman());
        
        currentPlayer.getListPlayerTiles().stream().forEach((tileToAdd) -> 
                    { this.gameHeap.addTile(tileToAdd); });
        gameHeap.shuffleHeapTiles();
        indexOfCurrentPlayer--;
    }
    
    public boolean isOnlyOnePlayerLeft() {
        return this.gameSettings.isLessThenTwoPlayers();
    }
    
    public void initGameFromFile(ArrayList<Player> playerArray, Board board, Player currPlayer, String gameName) {
        this.players = playerArray;
        this.gameBoard = board;
        this.currentPlayer = currPlayer;
        
        this.indexOfCurrentPlayer = this.players.indexOf(this.currentPlayer);
        this.isGameOver = this.isTie = false;
        initGameSettingsFromFile(gameName);
        initHeapFromFile();
    }

    public boolean isHumanPlayerLeftInGame() {
        boolean foundHuman = false;
        
        for (Iterator<Player> iterator = this.players.iterator(); !foundHuman && iterator.hasNext();) {
            Player player = iterator.next();
            foundHuman = player.getIsHuman();
        }
        
        return foundHuman;
    }

    public void initGameViaWebRequest() {
        initGameSetting();
        this.players = new ArrayList<>();
        createComputerPlayers(this.players);
        initAllPlayers(this.players);
    }

    public void addNewHumanPlayer(Player newPlayer) {
        newPlayer.initPlayer(this.gameHeap.getNewHandFromHeap());
        this.players.add(newPlayer);
        this.gameSettings.addHumanPlayerName(newPlayer.getName());
        this.gameOriginalInputedSettings.addHumanPlayer(newPlayer.getName());
    }
    
    public int getNumberOfJoinedHumanPlayers() {
        int numberOfHumansJoinedTheGame = 0;
        
        for (Player currPlayer : this.players) {
            if (currPlayer.getIsHuman()) {
                numberOfHumansJoinedTheGame++;
            }
        }
        
        return numberOfHumansJoinedTheGame;
    }
    
    public void shufflePlayersBeforeStartingGame() {
        Collections.shuffle(this.players);
        this.indexOfCurrentPlayer = 0;
        this.currentPlayer = this.players.get(indexOfCurrentPlayer);
    }
}