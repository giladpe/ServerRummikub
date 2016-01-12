/*
 * This class is responsible to implemenet the web service functionality
 */

package rummikub.implementation;
import com.sun.xml.xsom.impl.scd.Iterators;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import rummikub.gameLogic.controller.rummikub.SingleMove;
import rummikub.gameLogic.model.gameobjects.Board;
import rummikub.gameLogic.model.gameobjects.Serie;
import rummikub.gameLogic.model.logic.GameLogic;
import rummikub.gameLogic.model.logic.PlayersMove;
import rummikub.gameLogic.model.logic.SeriesGenerator;
import rummikub.gameLogic.model.logic.Settings;
import rummikub.gameLogic.model.player.ComputerSingleMoveGenerator;
import rummikub.gameLogic.model.player.Player;
import rummikub.gameLogic.model.gameobjects.Tile;
import rummikub.gameLogic.model.player.HumanPlayer;
import rummikub.gameLogic.view.ioui.Utils;
import ws.rummikub.*;

public class RummikubWsImplementation {
    
    //Constants:
    private static final boolean WITH_TILE_LIST = true;
    private static final boolean LOADED_FROM_XML = true;
    private static int INDEX_NORMALIZATION = 1;
    private static final int START_OF_THE_SERIES = 0;

    //private static int TIME = 1;

    
    //private members:
    
    //Game logic members
    private GameLogic rummikubLogic;
    private SeriesGenerator serieGenerator;
    private ComputerSingleMoveGenerator newMoveGenerator;
    private PlayersMove currentPlayerMove;
    
    //Server related members
    //private ArrayList<PlayerDetails> playerDetailesList;
    private HashMap<PlayerId,PlayerDetails> playerDetailes;
    private GameStatus gameStatus;
    private boolean isLoadedFromXML;
    private ArrayList<Event> gameEventList;
    
    //Constractors:
    public RummikubWsImplementation() {
        init(!LOADED_FROM_XML);
    }
    
    public RummikubWsImplementation(boolean isLoadedFromXML) {
        init(isLoadedFromXML);
    }
    
    private void init(boolean isLoadedFromXML) {
        this.rummikubLogic = new GameLogic();
        this.serieGenerator = new SeriesGenerator();
        this.newMoveGenerator = new ComputerSingleMoveGenerator();
        
        //this.playerDetailesList = new ArrayList<>();
        this.playerDetailes = new HashMap<>();
        this.gameStatus = GameStatus.WAITING;
        this.isLoadedFromXML = isLoadedFromXML;
        this.gameEventList = new ArrayList<>();
    }

    // <editor-fold defaultstate="collapsed" desc="Public functions used by the Web Service">

    //********** Public functions used by the Web Service - START **********/

    //DONE - maybe still things to add if i understood wrong the instructions - releated to TIMER
    public List<Event> getEvents(int playerId, int eventId) throws InvalidParameters_Exception {
        validateParamsAndThrowExceptionInIlegalCase(playerId, eventId);
        
        List<Event> eventsList;
        int indexOfLastEvent = indexOfLastEvent();

        if(eventId == 0) {
            eventsList = this.gameEventList;
        }
        else if(indexOfLastEvent == eventId) {
            eventsList = new ArrayList<>();
        }
        else { 
            eventsList = new ArrayList<>(this.gameEventList.subList(eventId + 1, indexOfLastEvent));
        }
        
//        if (hasEventThatNeedUserInput(eventsList)) {
//            //Timer timer = new Timer();
//        }
//        else {
//            
//        }

        return eventsList;
    }

    //TODO
    public String createGameFromXML(String xmlData) throws DuplicateGameName_Exception,
                                                           InvalidParameters_Exception, 
                                                           InvalidXML_Exception {
        String result = "";
        
        //TODO - finish wrting the function
        validateParamsAndThrowExceptionInIlegalCaseOfLoadingGameFromXML(xmlData);

        
        
        return result;
    }
    
    //DONE
    public List<PlayerDetails> getPlayersDetails(String gameName) throws GameDoesNotExists_Exception {
        
        validateParamsAndThrowExceptionInIlegalCase(gameName);

        List<PlayerDetails> playerDetailsList;
        
        playerDetailsList = makePlayerDetailsListWithoutTilesList();

        return playerDetailsList;
    }

    //DONE
    public void createGame(String gameName, int humanPlayers, int computerizedPlayers) throws DuplicateGameName_Exception,
                                                                                              InvalidParameters_Exception {
        
        validateParamsAndThrowExceptionInIlegalCase(gameName, humanPlayers, computerizedPlayers);

        Settings gameSettings = new Settings(gameName, humanPlayers, computerizedPlayers);
        createNewGame(gameSettings);
        
        //init only computer players
        initPlayerDetailesList();
        
    }
    
    //DONE
    public GameDetails getGameDetails(String gameName) throws GameDoesNotExists_Exception {
        validateParamsAndThrowExceptionInIlegalCase(gameName);
        
        GameDetails currentGameDetals = new GameDetails();
        Settings currGameSetings = this.rummikubLogic.getGameSettings();
        
        currentGameDetals.setComputerizedPlayers(currGameSetings.getNumOfCpuPlayers());
        currentGameDetals.setHumanPlayers(currGameSetings.getNumOfHumanPlayers());
        currentGameDetals.setJoinedHumanPlayers(this.rummikubLogic.getNumberOfJoinedHumanPlayers());
        currentGameDetals.setLoadedFromXML(this.isLoadedFromXML);
        currentGameDetals.setName(currGameSetings.getGameName());
        currentGameDetals.setStatus(this.gameStatus);
        
        return currentGameDetals;
    }

    //DONE
    // <editor-fold defaultstate="collapsed" desc="TODO - WHEN WE SUPPORT MULTIPLE GAMES - NOW RETURNS CURRENT GAME NAME IN THE LIST">
    //TODO:
    //when we will support multiple game we have to add other game's name's
    // </editor-fold>
    public List<String> getWaitingGames() {
        List<String> waitingGameList = new ArrayList<>();
        String gameName;
        
        if(this.rummikubLogic != null && this.gameStatus == GameStatus.WAITING ) {
            gameName = this.rummikubLogic.getGameSettings().getGameName();
            waitingGameList.add(gameName);
        }

        return waitingGameList;
    }

    //DONE
    public int joinGame(String gameName, String playerName) throws GameDoesNotExists_Exception, 
                                                                   InvalidParameters_Exception {
        validateParamsAndThrowExceptionInIlegalCase(gameName, playerName);
        
        Player newPlayer;
        int playerId;
        //before change playerDetails from list to map
        //int indexOfPlayerInHisGame;
        
        newPlayer = new HumanPlayer(gameName);
        this.rummikubLogic.addNewHumanPlayer(newPlayer);

        //before change playerDetails from list to map

        //indexOfPlayerInHisGame = indexOfLastEvent();
        //playerId = PlayerId.getPlayerId(playerName, gameName, indexOfPlayerInHisGame);

        playerId = addPlayerToPlayerDetailesList(newPlayer, PlayerStatus.JOINED, WITH_TILE_LIST);

        //finish wrtining that method
        updateGameStatus();
        
        return playerId;
    }

    //DONE
    public PlayerDetails getPlayerDetails(int playerId) throws GameDoesNotExists_Exception, 
                                                               InvalidParameters_Exception {
        
        validateParamsAndThrowExceptionGameNotExsistsOrInvalidParams(playerId);

        return findPlayerByPlayerId(playerId);
    }

    //TODO
    public void createSequence(int playerId, List<ws.rummikub.Tile> tiles) throws InvalidParameters_Exception {

        //TODO - finish writing that method - validation of tiles list
        validateParamsAndThrowExceptionInIlegalCase(playerId, tiles);

        addCreateSequenceEvent(playerId, tiles);
        
        //TODO - FINISH WRTITING ADDING NEW SERIE LOGIC
    }

    //TODO
    public void addTile(int playerId, ws.rummikub.Tile tile, int sequenceIndex, int sequencePosition) 
                                                                      throws InvalidParameters_Exception {
        
        //TODO - finish writing that method
        validateParamsAndThrowExceptionInIlegalCase(playerId, tile, sequenceIndex, sequencePosition);
        //we know the params are VALID
        
        Serie serie = this.currentPlayerMove.getBoardAfterMove().getSeries(sequenceIndex);
        final int END_OF_THE_SERIES = serie.isEmptySeries()? 0 : serie.getSizeOfSerie() - INDEX_NORMALIZATION;
        
        //TODO - FINISH WRTITING LOGIC
        addTileaddedEvent(playerId, tile, sequenceIndex, sequencePosition);
        
        if(sequencePosition > START_OF_THE_SERIES && sequencePosition < END_OF_THE_SERIES ) {
            
        }
        
        Point target = new Point(sequenceIndex, sequencePosition);
        Tile logicalTile = getLogicalTile(tile);
       
        int IndexInHand = this.currentPlayerMove.getHandAfterMove().indexOf(logicalTile);
        SingleMove singleMove = new SingleMove(target, IndexInHand, SingleMove.MoveType.HAND_TO_BOARD);
        
        if (dealWithSingleMoveResualt(singleMove)) {
            PlayerDetails playerDetails = findPlayerByPlayerId(playerId);
            playerDetails.getTiles().remove(IndexInHand);
        }
        // make single move - if it is legal - need to update player details tile list (function to do it)
    }
    
    //TODO
    public void takeBackTile(int playerId, int sequenceIndex, int sequencePosition) 
                                                            throws InvalidParameters_Exception {
        //TODO - finish writing that method
        validateParamsAndThrowExceptionInIlegalCase(playerId, sequenceIndex, sequencePosition);

        addTakeBackTileEvent(playerId, sequenceIndex, sequencePosition);
        
        //TODO - FINISH WRTITING LOGIC
    }

    //TODO
    public void moveTile(int playerId, int sourceSequenceIndex, 
                         int sourceSequencePosition, int targetSequenceIndex, 
                         int targetSequencePosition) throws InvalidParameters_Exception {
        
        //TODO - finish writing that method
        validateParamsAndThrowExceptionInIlegalCase(playerId, sourceSequenceIndex, sourceSequencePosition, targetSequenceIndex, targetSequencePosition);
        addMoveTileEvent(playerId, sourceSequenceIndex, sourceSequencePosition, targetSequenceIndex, targetSequencePosition);

        //TODO - FINISH WRTITING LOGIC
    }

    //TODO
    public void finishTurn(int playerId) throws InvalidParameters_Exception {

        validateParamsAndThrowExceptionInIlegalCase(playerId);
        
        addFinishTurnEvent(playerId);

        //TODO - FINISH WRTITING LOGIC
    }

    //TODO
    public void resign(int playerId) throws InvalidParameters_Exception {
       
        validateParamsAndThrowExceptionInIlegalCase(playerId);
        addResignEvent(playerId);


        //TODO - FINISH WRTITING LOGIC
    }
    
    //********** Public functions used by the Web Service - END **********/
    // </editor-fold>

    //private methods:
    
    private void createNewGame(Settings gameSetting) {
        this.rummikubLogic = new GameLogic();
        this.rummikubLogic.setGameSettings(gameSetting);
        
        //A: i changed it to new....
        //this.rummikubLogic.setGameOriginalInputedSettings(gameSetting);
        this.rummikubLogic.setGameOriginalInputedSettings( new Settings(gameSetting));
        
        this.rummikubLogic.initGameViaWebRequest();
                
        //probbly not need it here
        //initCurrentPlayerMove();
    }
    
    private void initCurrentPlayerMove() {
        //init variables in the statrt of the turn
        Board printableBoard = new Board(new ArrayList<>(rummikubLogic.getGameBoard().getListOfSerie()));
        boolean isFirstMoveDone = rummikubLogic.getCurrentPlayer().isFirstMoveDone();
        Player printablePlayer = rummikubLogic.getCurrentPlayer().clonePlayer();
        this.currentPlayerMove = new PlayersMove(printablePlayer.getListPlayerTiles(), printableBoard, isFirstMoveDone);
    }
    
    private void validateParamsAndThrowExceptionInIlegalCase(String gameName, int humanPlayers, int computerizedPlayers) throws DuplicateGameName_Exception,
                                                                                               InvalidParameters_Exception {
        checkCaseOfDuplicateGameName(gameName);
        validateNumberOfHumanAndComputerPlayers(humanPlayers, computerizedPlayers);
    }
    
    private void validateParamsAndThrowExceptionInIlegalCase(String gameName) throws GameDoesNotExists_Exception {
        checkCaseOfGameDoesNotExists(gameName);
    }
    
    private void validateParamsAndThrowExceptionInIlegalCase(String gameName, String playerName) throws GameDoesNotExists_Exception, 
                                                                                                        InvalidParameters_Exception {
        checkCaseOfGameDoesNotExists(gameName);
        checkCaseOfPlayerAlreadyExsists(playerName);
        checkCaseOfGameStatusIsNotWaiting(gameName);
    }

    private void validateParamsAndThrowExceptionGameNotExsistsOrInvalidParams(int playerId) throws GameDoesNotExists_Exception, 
                                                                                  InvalidParameters_Exception {
        checkCaseOfPlayerNotExsists(playerId);
        checkCaseOfGameDoesNotExists(playerId);
    }

    private void validateParamsAndThrowExceptionInIlegalCase(int playerId) throws InvalidParameters_Exception {
        checkCaseOfPlayerNotExsists(playerId);
    }
    
    private void validateParamsAndThrowExceptionInIlegalCase(int playerId, int eventId) throws InvalidParameters_Exception {
        checkCaseOfPlayerNotExsists(playerId);
        checkCaseOfIlegalEventId(eventId);
    } 
    
    private void validateParamsAndThrowExceptionInIlegalCase(int playerId, List<ws.rummikub.Tile> tiles) throws InvalidParameters_Exception {
        checkCaseOfPlayerNotExsists(playerId);
        checkCaseOfIlegalTileListThatRepresentsSeries(tiles);
    }
    
    private void validateParamsAndThrowExceptionInIlegalCase(int playerId, int sequenceIndex, int sequencePosition) throws InvalidParameters_Exception {
        checkCaseOfPlayerNotExsists(playerId);
    }
    
    private void validateParamsAndThrowExceptionInIlegalCase(int playerId, ws.rummikub.Tile tile, int sequenceIndex, int sequencePosition) throws InvalidParameters_Exception {
        checkCaseOfPlayerNotExsists(playerId);
    }
        
    private void validateParamsAndThrowExceptionInIlegalCase(int playerId, int sourceSequenceIndex, 
                                                             int sourceSequencePosition, int targetSequenceIndex, 
                                                             int targetSequencePosition) throws InvalidParameters_Exception {
        checkCaseOfPlayerNotExsists(playerId);
    }
    
    private void checkCaseOfDuplicateGameName(String gameName) throws DuplicateGameName_Exception {
        
        if (isGameNameAlreadyExsists(gameName)) {
            DuplicateGameName duplicateGameName  = new DuplicateGameName();
            RummikubFault rummikubFualt = new RummikubFault();

            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("name already exsists");
            duplicateGameName.setFaultInfo(rummikubFualt);
            duplicateGameName.setMessage(Utils.Constants.ErrorMessages.GAME_NAME_ALREADY_EXSIST);
            throw new DuplicateGameName_Exception(Utils.Constants.ErrorMessages.GAME_NAME_ALREADY_EXSIST, duplicateGameName);
        }
    }
    
    private void checkCaseOfGameDoesNotExists(String gameName) throws GameDoesNotExists_Exception {

        checkCaseOfEmptyStringOrNullOrContainsWhiteSpacesOfGameNotExsists(gameName); 
             
        if (!isGameNameAlreadyExsists(gameName)) {
            GameDoesNotExists gameDoesNotExsists = new GameDoesNotExists();
            RummikubFault rummikubFualt = new RummikubFault();

            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("name not exsists");
            gameDoesNotExsists.setFaultInfo(rummikubFualt);
            gameDoesNotExsists.setMessage(Utils.Constants.ErrorMessages.GAME_NAME_NOT_EXSIST);
            throw new GameDoesNotExists_Exception(Utils.Constants.ErrorMessages.GAME_NAME_NOT_EXSIST, gameDoesNotExsists);
        }
    }
    
    private void checkCaseOfGameDoesNotExists(int playerId) throws GameDoesNotExists_Exception {

        PlayerDetails playerDetails = findPlayerByPlayerId(playerId);
        
        if (playerDetails == null) {
            GameDoesNotExists gameDoesNotExsists = new GameDoesNotExists();
            RummikubFault rummikubFualt = new RummikubFault();

            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("player id not exsists");
            gameDoesNotExsists.setFaultInfo(rummikubFualt);
            gameDoesNotExsists.setMessage(Utils.Constants.ErrorMessages.PLAYER_ID_NOT_EXSISTS);
            throw new GameDoesNotExists_Exception(Utils.Constants.ErrorMessages.PLAYER_ID_NOT_EXSISTS, gameDoesNotExsists);
        }
    }
    
    private void checkCaseOfPlayerAlreadyExsists(String playerName) throws InvalidParameters_Exception {
        
        checkCaseOfEmptyStringOrNullOrContainsWhiteSpacesOfInvalidParameters(playerName);
        
        if (this.isLoadedFromXML) {
            if (!this.rummikubLogic.getGameSettings().isPlayerNameExists(playerName)) {
                InvalidParameters invalidParameters = new InvalidParameters();
                RummikubFault rummikubFualt = new RummikubFault();

                rummikubFualt.setFaultCode(null);
                rummikubFualt.setFaultString("For a loaded game such player not exsists");
                invalidParameters.setFaultInfo(rummikubFualt);
                invalidParameters.setMessage(Utils.Constants.ErrorMessages.PLAYER_NAME_NOT_EXSISTS_IN_XML_LOADED_GAME);

                throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.PLAYER_NAME_NOT_EXSISTS_IN_XML_LOADED_GAME,
                        invalidParameters);
            }
        }
        else {
            if (this.rummikubLogic.getGameSettings().isPlayerNameExists(playerName)) {
                InvalidParameters invalidParameters = new InvalidParameters();
                RummikubFault rummikubFualt = new RummikubFault();

                rummikubFualt.setFaultCode(null);
                rummikubFualt.setFaultString("There is already a player with same name");
                invalidParameters.setFaultInfo(rummikubFualt);
                invalidParameters.setMessage(Utils.Constants.ErrorMessages.ILEGAL_PLAYER_NAME);

                throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.ILEGAL_PLAYER_NAME, invalidParameters);
            }    
        }
    }
     
    private void checkCaseOfPlayerNotExsists(int playerId) throws InvalidParameters_Exception {
        
        PlayerDetails playerDetails = findPlayerByPlayerId(playerId);
        
        if (playerDetails == null) {
            InvalidParameters invalidParameters = new InvalidParameters();
            RummikubFault rummikubFualt = new RummikubFault();

            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("player id not exsists");
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.PLAYER_ID_NOT_EXSISTS);

            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.PLAYER_ID_NOT_EXSISTS,
                    invalidParameters);

        }
    }
    
    private void checkCaseOfGameStatusIsNotWaiting(String gameName) throws InvalidParameters_Exception {
        
        if (this.gameStatus != GameStatus.WAITING) {
            InvalidParameters invalidParameters = new InvalidParameters();
            RummikubFault rummikubFualt = new RummikubFault();
            
            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("There game already stared");
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.GAME_NOT_IN_WAITING_STATUS);
            
            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.GAME_NOT_IN_WAITING_STATUS,
                                                  invalidParameters);
        }
    }
    
    private void checkCaseOfIlegalEventId(int eventId) throws InvalidParameters_Exception {
        InvalidParameters invalidParameters = new InvalidParameters();
        RummikubFault rummikubFualt = new RummikubFault();

        if (isNegativeNumber(eventId)) {
            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("negative event id passed");
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.NEGATIVE_EVENT_ID);
            
            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.NEGATIVE_EVENT_ID, invalidParameters);
        }
        
        if (indexOfLastEvent() < eventId) {
            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("the event id the passed is bigger then the exisiting event list and therefore not exsists");
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.EVENT_ID_NOT_EXSISTS);
            
            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.EVENT_ID_NOT_EXSISTS,
                                                  invalidParameters);
        }
    }
    
    
    // <editor-fold defaultstate="collapsed" desc="TODO - WHEN WE SUPPORT MULTIPLE GAMES - NOW RETURNS ALWYAS FASLE">
    //TODO:
    //when we will support multiple game we have to check if the game's name already exsists - now returns always false
    // </editor-fold>
    private boolean isGameNameAlreadyExsists(String gameName) {
        boolean isGameAlreadyExsists = false;

        return isGameAlreadyExsists;
    }

    private void validateNumberOfHumanAndComputerPlayers(int humanPlayers, int computerizedPlayers) throws InvalidParameters_Exception {
        InvalidParameters invalidParameters = new InvalidParameters();
        RummikubFault rummikubFualt = new RummikubFault();

        if (isNegativeNumber(humanPlayers)) {
            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("amout of human players is negative number");
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.NEGATIVE_NUMBER_OF_HUMAN_PLAYERS);
            
            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.NEGATIVE_NUMBER_OF_HUMAN_PLAYERS,
                                                  invalidParameters);
        }
        
        if (isNegativeNumber(computerizedPlayers)) {
            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("amout of computer players is negative number");
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.NEGATIVE_NUMBER_OF_COMPUTER_PLAYERS);
            
            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.NEGATIVE_NUMBER_OF_COMPUTER_PLAYERS,
                                                  invalidParameters);
        }
        
        if (isOutOfBounderiesOfTheGamePlayerNumber(humanPlayers, computerizedPlayers)) {
            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("amout of players is bigger then "+ Settings.MAX_NUMBER_OF_PLAYERS + "or smaller then" + Settings.MIN_NUMBER_OF_PLAYERS);
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.ILEGAL_TOTAL_PLAYER_NUMBER);
            
            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.ILEGAL_TOTAL_PLAYER_NUMBER,
                                                  invalidParameters);
        }
    }

    private boolean isNegativeNumber(int num) {
        return num < 0;
    }

    private boolean isOutOfBounderiesOfTheGamePlayerNumber(int humanPlayers, int computerizedPlayers) {
        int totalNumberOfPlayers = humanPlayers + computerizedPlayers;
        
        return totalNumberOfPlayers > Settings.MAX_NUMBER_OF_PLAYERS || totalNumberOfPlayers < Settings.MIN_NUMBER_OF_PLAYERS;
    }

    private boolean isEmptyStringOrNullOrContainsStartingWhiteSpaces(String stringToCheck) {
       return !(stringToCheck != null && !stringToCheck.isEmpty() && !Character.isWhitespace(stringToCheck.charAt(0)));
    }
    
    private void checkCaseOfEmptyStringOrNullOrContainsWhiteSpacesOfGameNotExsists(String gameName) throws GameDoesNotExists_Exception {
        
        if(isEmptyStringOrNullOrContainsStartingWhiteSpaces(gameName)) {
            GameDoesNotExists gameDoesNotExsists = new GameDoesNotExists();
            RummikubFault rummikubFualt = new RummikubFault();

            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("game name is empty or null or contains starting white spaces");
            gameDoesNotExsists.setFaultInfo(rummikubFualt);
            gameDoesNotExsists.setMessage(Utils.Constants.ErrorMessages.STRING_IS_NULL_OR_EMPTY_OR_CONTAINS_STARTING_WHITE_SPACES);
            throw new GameDoesNotExists_Exception(Utils.Constants.ErrorMessages.STRING_IS_NULL_OR_EMPTY_OR_CONTAINS_STARTING_WHITE_SPACES,
                                                  gameDoesNotExsists);
        }
    }

    private void checkCaseOfEmptyStringOrNullOrContainsWhiteSpacesOfInvalidParameters(String gameName) throws InvalidParameters_Exception {
        
        if(isEmptyStringOrNullOrContainsStartingWhiteSpaces(gameName)) {
            InvalidParameters invalidParameters = new InvalidParameters();
            RummikubFault rummikubFualt = new RummikubFault();

            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("player name is empty or null or contains starting white spaces");
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.STRING_IS_NULL_OR_EMPTY_OR_CONTAINS_STARTING_WHITE_SPACES);
            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.STRING_IS_NULL_OR_EMPTY_OR_CONTAINS_STARTING_WHITE_SPACES,
                                                  invalidParameters);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="PlayerDetails-WS - methods">
    //************************ maybe not need  - START ************************//

    //USED WHEN THE MEMBER "this.playerDetailesList" EXSISTS
    
    private void initPlayerDetailesList() {
        for (Player currPlayer : this.rummikubLogic.getPlayers()) {
            addPlayerToPlayerDetailesList(currPlayer);
        }
    }
    
    //    USED WHEN THE MEMBER "this.playerDetailesList" is ArrayList type
//
//    private void addPlayerToPlayerDetailesList(Player currPlayer) {
//        PlayerDetails playerDetails = createPlayerDetailes(currPlayer, PlayerStatus.JOINED, WITH_TILE_LIST);
//        this.playerDetailesList.add(playerDetails);
//    }
//    
//    private void addPlayerToPlayerDetailesList(Player currPlayer, PlayerStatus playerStatus, boolean withTilesList) {
//        PlayerDetails playerDetails = createPlayerDetailes(currPlayer, playerStatus, withTilesList);
//        this.playerDetailesList.add(playerDetails);
//    }
//****************************
    
    private void addPlayerToPlayerDetailesList(Player currPlayer) {
        PlayerDetails playerDetails = createPlayerDetailes(currPlayer, PlayerStatus.JOINED, WITH_TILE_LIST);
        int indexOfPlayerInHisGame = this.rummikubLogic.getPlayers().isEmpty()? 
                                     0 : this.rummikubLogic.getPlayers().size() - INDEX_NORMALIZATION;
        PlayerId newPlayerId = new PlayerId(currPlayer.getName(), this.rummikubLogic.getGameSettings().getGameName(),
                                            indexOfPlayerInHisGame);
        this.playerDetailes.put(newPlayerId, playerDetails);
    }
    
    private int addPlayerToPlayerDetailesList(Player currPlayer, PlayerStatus playerStatus, boolean withTilesList) {
        PlayerDetails playerDetails = createPlayerDetailes(currPlayer, playerStatus, withTilesList);
        int indexOfPlayerInHisGame = this.rummikubLogic.getPlayers().isEmpty()? 
                                     0 : this.rummikubLogic.getPlayers().size() - INDEX_NORMALIZATION;
        PlayerId newPlayerId = new PlayerId(currPlayer.getName(), this.rummikubLogic.getGameSettings().getGameName(),
                                            indexOfPlayerInHisGame);
        this.playerDetailes.put(newPlayerId, playerDetails);
        
        return newPlayerId.getPlayerId();
    }
    
//    USED WHEN THE MEMBER "this.playerDetailesList" **NOT** EXSISTS
    
//    private ArrayList<PlayerDetails> initPlayerDetailesList(PlayerStatus playerStatus, boolean withTilesList) {
//        ArrayList<PlayerDetails> playerDetailsList = new ArrayList<>();
//        
//        for (Player currPlayer : this.rummikubLogic.getPlayers()) {
//            PlayerDetails playerDetails = createPlayerDetailes(currPlayer, playerStatus, withTilesList);
//            playerDetailsList.add(playerDetails);
//        }
//        
//        return playerDetailsList;
//    }
    
    private PlayerDetails createPlayerDetailes(Player currPlayer, PlayerStatus playerStatus, boolean withTilesList) {
        PlayerDetails playerDetails = new PlayerDetails();
        PlayerType playerType = currPlayer.getIsHuman()? PlayerType.HUMAN : PlayerType.COMPUTER;

        playerDetails.setName(currPlayer.getName());
        playerDetails.setNumberOfTiles(currPlayer.getListPlayerTiles().size());
        playerDetails.setPlayedFirstSequence(currPlayer.isFirstMoveDone());
        playerDetails.setStatus(playerStatus);
        playerDetails.setType(playerType);
        
        if(withTilesList) {
            initPlayerDetailsTileList(playerDetails, currPlayer);
        }
        
        return playerDetails;
    }
    
    private void initPlayerDetailsTileList(PlayerDetails playerDetails, Player currPlayer) {
        for (Tile currTile : currPlayer.getListPlayerTiles()) {
            ws.rummikub.Tile jaxbTile = new ws.rummikub.Tile();

            setJaxbTileColor(jaxbTile, currTile);
            jaxbTile.setValue(currTile.getEnumTileNumber().getTileNumberValue());
            playerDetails.getTiles().add(jaxbTile);
        }
    }
     
    private void setJaxbTileColor(ws.rummikub.Tile JaxbTile ,Tile currTile) {
        switch (currTile.getTileColor()) {
            case BLACK:
                JaxbTile.setColor(Color.BLACK);
                break;

            case BLUE:
                JaxbTile.setColor(Color.BLUE);
                break;

            case RED: 
                JaxbTile.setColor(Color.RED);                    
                break;

            case YELLOW:
            default:
                JaxbTile.setColor(Color.YELLOW);
                break;
        }
    }
    
    //    USED WHEN THE MEMBER "this.playerDetailesList" is ArrayList type

//    private List<PlayerDetails> makePlayerDetailsListWithoutTilesList() {
//        List<PlayerDetails> playerDetailsList = new ArrayList<>();
//
//        for (PlayerDetails playerDetails : this.playerDetailesList) {
//            PlayerDetails newPlayerDetails = copyPlayerDetails(playerDetails, !WITH_TILE_LIST);
//            playerDetailsList.add(newPlayerDetails);
//        }
//        
//        return playerDetailsList;
//    }

    private List<PlayerDetails> makePlayerDetailsListWithoutTilesList() {
        List<PlayerDetails> newPlayerDetailsList = new ArrayList<>();

        for (PlayerDetails playerDetails : this.playerDetailes.values()) {
            PlayerDetails newPlayerDetails = copyPlayerDetails(playerDetails, !WITH_TILE_LIST);
            newPlayerDetailsList.add(newPlayerDetails);
        }
        
        return newPlayerDetailsList;
    }

    private PlayerDetails copyPlayerDetails(PlayerDetails playerDetailsToCopy, boolean withTilesList) {
        PlayerDetails playerDetails = new PlayerDetails();

        playerDetails.setName(playerDetailsToCopy.getName());
        playerDetails.setNumberOfTiles(playerDetailsToCopy.getNumberOfTiles());
        playerDetails.setPlayedFirstSequence(playerDetailsToCopy.isPlayedFirstSequence());
        playerDetails.setStatus(playerDetailsToCopy.getStatus());
        playerDetails.setType(playerDetailsToCopy.getType());
        
        if(withTilesList) {
            for (ws.rummikub.Tile currTile : playerDetailsToCopy.getTiles()) {
                playerDetails.getTiles().add(currTile);
            }
        }
        
        return playerDetails;
    }
    
    //************************ maybe not need  - END ************************//
    // </editor-fold>


    private void updateGameStatus() {
        int currGameNumberOfHumanPlayersNeeded = this.rummikubLogic.getGameOriginalInputedSettings().getNumOfHumanPlayers();

        if (this.rummikubLogic.getNumberOfJoinedHumanPlayers() == currGameNumberOfHumanPlayersNeeded) {
            this.gameStatus = GameStatus.ACTIVE;

            //walk throw details list and set status to active????
            for (PlayerDetails currPlayerDetailes : this.playerDetailes.values()) {
                currPlayerDetailes.setStatus(PlayerStatus.ACTIVE);
            }
            
            //walk throw details list and set status to active????
//            for (PlayerDetails playerDetailes : this.playerDetailesList) {
//                playerDetailes.setStatus(PlayerStatus.ACTIVE);
//            }

            initCurrentPlayerMove();
            
            addGameStartEvent();
        }
    }

    private PlayerDetails findPlayerByPlayerId(int playerId) {
        boolean found = false;
        PlayerDetails playerDetails = null;
        
        for (Iterator<PlayerId> iterator = this.playerDetailes.keySet().iterator(); !found && iterator.hasNext();) {
            PlayerId currPlayerId = iterator.next();
            
            found = currPlayerId.getPlayerId() == playerId;
            if(found) {
                playerDetails = this.playerDetailes.get(currPlayerId); 
            }
        }
        
        return playerDetails;
    }
    
    int indexOfLastEvent() {
        return this.gameEventList.isEmpty()? 0 : this.gameEventList.size() - INDEX_NORMALIZATION;
    }

    private void checkCaseOfIlegalTileListThatRepresentsSeries(List<ws.rummikub.Tile>  tiles) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void validateParamsAndThrowExceptionInIlegalCaseOfLoadingGameFromXML(String xmlData) throws DuplicateGameName_Exception,
                                                                                                        InvalidParameters_Exception, 
                                                                                                        InvalidXML_Exception {
        throw new DuplicateGameName_Exception(null, null);
        //throw new InvalidParameters_Exception("TEST", null);
        //throw new InvalidXML_Exception(null, null);
    }

    private Tile.Color convertToLogicColor(ws.rummikub.Color color) {
        Tile.Color newColor;
        
        switch (color) {
            case BLACK:
                newColor = Tile.Color.BLACK;
                break;

            case BLUE:
                newColor = Tile.Color.BLUE;
                break;

            case RED: 
                newColor = Tile.Color.RED;
                break;

            case YELLOW:
            default:
                newColor = Tile.Color.YELLOW;
                break;
        }
        
        return newColor;
    }

    private Tile.TileNumber convertToLogicTileNum(int value) {
        return Tile.TileNumber.getTileNumberByValue(value);
    }

    private Tile getLogicalTile(ws.rummikub.Tile tile) {
        Tile.Color tileColor = convertToLogicColor(tile.getColor());
        Tile.TileNumber tileNum = convertToLogicTileNum(tile.getValue());
        return new Tile(tileColor, tileNum);
    }
    
    
    private boolean dealWithSingleMoveResualt(SingleMove singleMove) throws InvalidParameters_Exception {
        SingleMove.SingleMoveResult singleMoveResualt = this.currentPlayerMove.implementSingleMove(singleMove);
        boolean isLegalMoveDone = false;
        InvalidParameters invalidParameters = new InvalidParameters();
        RummikubFault rummikubFualt = new RummikubFault();

        switch (singleMoveResualt) {
            case TILE_NOT_BELONG_HAND: {
                rummikubFualt.setFaultCode(null);
                rummikubFualt.setFaultString("The tile not belong to the player in the current turn");
                invalidParameters.setFaultInfo(rummikubFualt);
                invalidParameters.setMessage(Utils.Constants.ErrorMessages.ILEGAL_TILE_IS_NOT_BELONG_TO_HAND);
                throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.ILEGAL_TILE_IS_NOT_BELONG_TO_HAND,
                                                  invalidParameters);               
            }
            case NOT_IN_THE_RIGHT_ORDER: {
                rummikubFualt.setFaultCode(null);
                rummikubFualt.setFaultString("The tile was placed not in increasing order");
                invalidParameters.setFaultInfo(rummikubFualt);
                invalidParameters.setMessage(Utils.Constants.ErrorMessages.ILEGAL_TILE_INSERTED_NOT_IN_RIGHT_ORDER);
                throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.ILEGAL_TILE_INSERTED_NOT_IN_RIGHT_ORDER,
                                                  invalidParameters);
            }
            case CAN_NOT_TOUCH_BOARD_IN_FIRST_MOVE: {
                rummikubFualt.setFaultCode(null);
                rummikubFualt.setFaultString("canot touch tiles that not belong to the player before finishing the first turn");
                invalidParameters.setFaultInfo(rummikubFualt);
                invalidParameters.setMessage(Utils.Constants.ErrorMessages.ILEGAL_CANT_TUCH_BOARD_IN_FIRST_MOVE);
                throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.ILEGAL_CANT_TUCH_BOARD_IN_FIRST_MOVE,
                                                  invalidParameters);
            }
            case LEGAL_MOVE:
            default: {
                isLegalMoveDone = true;
                break;
            }
        }
        return isLegalMoveDone;
    }

    private void addCreateSequenceEvent(int playerId, List<ws.rummikub.Tile> tiles) {
        Event newSequenceCtearedEvent = new Event();
        
        newSequenceCtearedEvent.setId(indexOfLastEvent());
        newSequenceCtearedEvent.setPlayerName(findPlayerByPlayerId(playerId).getName());
        //newSequenceCtearedEvent.setSourceSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
        //newSequenceCtearedEvent.setSourceSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
        //newSequenceCtearedEvent.setTargetSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
        //newSequenceCtearedEvent.setTargetSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
        newSequenceCtearedEvent.setType(EventType.SEQUENCE_CREATED);
        this.gameEventList.add(newSequenceCtearedEvent);
    }

    private void addTileaddedEvent(int playerId, ws.rummikub.Tile tile, int sequenceIndex, int sequencePosition) {
        Event addTileEvent = new Event();
        
        addTileEvent.setId(indexOfLastEvent());
        addTileEvent.setPlayerName(findPlayerByPlayerId(playerId).getName());
        //addTileEvent.setSourceSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
        //addTileEvent.setSourceSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
        addTileEvent.setTargetSequenceIndex(sequenceIndex);
        addTileEvent.setTargetSequencePosition(sequencePosition);
        addTileEvent.setType(EventType.TILE_ADDED);
        this.gameEventList.add(addTileEvent);
    }

    private void addTakeBackTileEvent(int playerId, int sequenceIndex, int sequencePosition) {
        Event takeTileBackEvent = new Event();
        
        takeTileBackEvent.setId(indexOfLastEvent());
        takeTileBackEvent.setPlayerName(findPlayerByPlayerId(playerId).getName());
        takeTileBackEvent.setSourceSequenceIndex(sequenceIndex);
        takeTileBackEvent.setSourceSequencePosition(sequencePosition);
        //takeTileBackEvent.setTargetSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
        //takeTileBackEvent.setTargetSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
        takeTileBackEvent.setType(EventType.TILE_RETURNED);
        this.gameEventList.add(takeTileBackEvent);
    }

    private void addMoveTileEvent(int playerId, int sourceSequenceIndex, int sourceSequencePosition, int targetSequenceIndex, int targetSequencePosition) {
        Event tileMovedEvent = new Event();
        
        tileMovedEvent.setId(indexOfLastEvent());
        tileMovedEvent.setPlayerName(findPlayerByPlayerId(playerId).getName());
        tileMovedEvent.setSourceSequenceIndex(sourceSequenceIndex);
        tileMovedEvent.setSourceSequencePosition(sourceSequencePosition);
        tileMovedEvent.setTargetSequenceIndex(targetSequenceIndex);
        tileMovedEvent.setTargetSequencePosition(targetSequencePosition);
        tileMovedEvent.setType(EventType.TILE_MOVED);
        this.gameEventList.add(tileMovedEvent);
    }

    private void addFinishTurnEvent(int playerId) {
        Event finishTurnEvent = new Event();
        
        finishTurnEvent.setId(indexOfLastEvent());
        finishTurnEvent.setPlayerName(findPlayerByPlayerId(playerId).getName());
        //finishTurnEvent.setSourceSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
        //finishTurnEvent.setSourceSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
        //finishTurnEvent.setTargetSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
        //finishTurnEvent.setTargetSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
        finishTurnEvent.setType(EventType.PLAYER_FINISHED_TURN);
        this.gameEventList.add(finishTurnEvent);
    }

    private void addResignEvent(int playerId) {
        Event resignEvent = new Event();
        
        resignEvent.setId(indexOfLastEvent());
        resignEvent.setPlayerName(findPlayerByPlayerId(playerId).getName());
        //resignEvent.setSourceSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
        //resignEvent.setSourceSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
        //resignEvent.setTargetSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
        //resignEvent.setTargetSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
        resignEvent.setType(EventType.PLAYER_RESIGNED);
        this.gameEventList.add(resignEvent);
    }

    private void addGameStartEvent() {
       Event takeTileBackEvent = new Event();
        this.gameEventList.add(takeTileBackEvent);

        takeTileBackEvent.setId(indexOfLastEvent());
        //takeTileBackEvent.setPlayerName(findPlayerByPlayerId(playerId).getName(/*MAYBE NOT NEED THAT SET*/null));
        //takeTileBackEvent.setSourceSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
        //takeTileBackEvent.setSourceSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
        //takeTileBackEvent.setTargetSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
        //takeTileBackEvent.setTargetSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
        takeTileBackEvent.setType(EventType.GAME_START);
    }
}

// <editor-fold defaultstate="collapsed" desc="user-description">
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Web service Info">
//**************************************** Web service Info - START ****************************************//
//*Endpoint:
//  -Service Name:	{http://rummikub.ws/}RummikubWebServiceService
//  -Port Name:	{http://rummikub.ws/}RummikubWebServicePort
//
//*Information:
//  -Address:	http://localhost:8080/RummikubApi/RummikubWebServiceService
//  -WSDL:	http://localhost:8080/RummikubApi/RummikubWebServiceService?wsdl
//  -Implementation class:	rummikub.ws.RummikubWS
//**************************************** Web service Info - END ****************************************//
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="COPY OF WEB SERVISE FUNCS IMPLEMENTATION"> 

//**************************** COPY OF WEB SERVISE FUNCS IMPLEMENTATION - START ****************************// 

/*
 * This class is the web service api.
 */

//package rummikub.ws;
//
//import javax.jws.WebService;
//import rummikub.implementation.RummikubWsImplementation;
//
//
//@WebService(serviceName = "RummikubWebServiceService", portName = "RummikubWebServicePort", endpointInterface = "ws.rummikub.RummikubWebService", targetNamespace = "http://rummikub.ws/", wsdlLocation = "WEB-INF/wsdl/RummikubWS/RummikubWebServiceService.wsdl")
//public class RummikubWS {
//
//    private final RummikubWsImplementation serverImplementation = new RummikubWsImplementation();
//    
//    public java.util.List<ws.rummikub.Event> getEvents(int playerId, int eventId) throws ws.rummikub.InvalidParameters_Exception {
//        return serverImplementation.getEvents(playerId, eventId);
//    }
//
//    public java.lang.String createGameFromXML(java.lang.String xmlData) throws ws.rummikub.DuplicateGameName_Exception, ws.rummikub.InvalidParameters_Exception, ws.rummikub.InvalidXML_Exception {
//        return serverImplementation.createGameFromXML(xmlData);
//    }
//
//    public java.util.List<ws.rummikub.PlayerDetails> getPlayersDetails(java.lang.String gameName) throws ws.rummikub.GameDoesNotExists_Exception {
//        return serverImplementation.getPlayersDetails(gameName);
//    }
//
//    public void createGame(java.lang.String name, int humanPlayers, int computerizedPlayers) throws ws.rummikub.InvalidParameters_Exception, ws.rummikub.DuplicateGameName_Exception {
//       serverImplementation.createGame(name, humanPlayers, computerizedPlayers);
//    }
//
//    public ws.rummikub.GameDetails getGameDetails(java.lang.String gameName) throws ws.rummikub.GameDoesNotExists_Exception {
//        return serverImplementation.getGameDetails(gameName);
//    }
//
//    public java.util.List<java.lang.String> getWaitingGames() {
//        return serverImplementation.getWaitingGames();
//    }
//
//    public int joinGame(java.lang.String gameName, java.lang.String playerName) throws ws.rummikub.GameDoesNotExists_Exception, ws.rummikub.InvalidParameters_Exception {
//        return serverImplementation.joinGame(gameName, playerName);
//    }
//
//    public ws.rummikub.PlayerDetails getPlayerDetails(int playerId) throws ws.rummikub.GameDoesNotExists_Exception, ws.rummikub.InvalidParameters_Exception {
//        return serverImplementation.getPlayerDetails(playerId);
//    }
//
//    public void createSequence(int playerId, java.util.List<ws.rummikub.Tile> tiles) throws ws.rummikub.InvalidParameters_Exception {
//        serverImplementation.createSequence(playerId, tiles);
//    }
//
//    public void addTile(int playerId, ws.rummikub.Tile tile, int sequenceIndex, int sequencePosition) throws ws.rummikub.InvalidParameters_Exception {
//        serverImplementation.addTile(playerId, tile, sequenceIndex, sequencePosition);
//    }
//
//    public void takeBackTile(int playerId, int sequenceIndex, int sequencePosition) throws ws.rummikub.InvalidParameters_Exception {
//        serverImplementation.takeBackTile(playerId, sequenceIndex, sequencePosition);
//    }
//
//    public void moveTile(int playerId, int sourceSequenceIndex, int sourceSequencePosition, int targetSequenceIndex, int targetSequencePosition) throws ws.rummikub.InvalidParameters_Exception {
//        serverImplementation.moveTile(playerId, sourceSequenceIndex, sourceSequencePosition, targetSequenceIndex, targetSequencePosition);
//    }
//
//    public void finishTurn(int playerId) throws ws.rummikub.InvalidParameters_Exception {
//        serverImplementation.finishTurn(playerId);
//    }
//
//    public void resign(int playerId) throws ws.rummikub.InvalidParameters_Exception {
//        serverImplementation.resign(playerId);
//    }
//}
//**************************** COPY OF WEB SERVISE FUNCS IMPLEMENTATION - END ****************************// 
// </editor-fold> 

