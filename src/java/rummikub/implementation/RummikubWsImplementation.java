/*
 * This class is responsible to implemenet the web service functionality
 */

package rummikub.implementation;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.xml.sax.SAXException;
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
import rummikub.gameLogic.view.ioui.JaxBXmlParser;
import rummikub.gameLogic.view.ioui.Utils;
import ws.rummikub.*;

public class RummikubWsImplementation {
    
    //Constants:
    private static final boolean WITH_TILE_LIST = true;
    private static final boolean LOADED_FROM_XML = true;
    private static final boolean DEAMON_THREAD = true;
    private static int INDEX_NORMALIZATION = 1;
    private static final int START_OF_THE_SERIES = 0;
    private static final long TIMER_DELAY = TimeUnit.MINUTES.toMillis(2);
    private static final long DELAY_FOR_COMPUTER_MOVE = 1000;

    private static final int DISABLED_TIMER = 0;


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
    private Timer timer;
    private EventManager eventManager;
    
    //Constractors:
    public RummikubWsImplementation() {
        init(!LOADED_FROM_XML);
    }
    
    public RummikubWsImplementation(boolean isLoadedFromXML) {
        init(isLoadedFromXML);
    }
    
    private void init(boolean isLoadedFromXML) {
        //this.rummikubLogic = new GameLogic();
        this.serieGenerator = new SeriesGenerator();
        this.newMoveGenerator = new ComputerSingleMoveGenerator();
        
        //this.playerDetailesList = new ArrayList<>();
        this.playerDetailes = new HashMap<>();
        this.gameStatus = GameStatus.WAITING;
        this.isLoadedFromXML = isLoadedFromXML;
        this.timer = new Timer(DEAMON_THREAD);
        this.eventManager = new EventManager();
    }

    // <editor-fold defaultstate="collapsed" desc="Public functions used by the Web Service">

    //********** Public functions used by the Web Service - START **********/

    //DONE - maybe still things to add if i understood wrong the instructions - releated to TIMER
    public List<Event> getEvents(int playerId, int eventId) throws InvalidParameters_Exception {
       
        validateParamsAndThrowExceptionInIlegalCase(playerId, eventId);
        
        List<Event> newEventsListForCurrPlayer;
        List<Event> allEventsList = this.eventManager.getGameEventList();
        
        int indexOfLastEvent = this.eventManager.indexOfLastEvent();

        if(eventId == 0) {
            newEventsListForCurrPlayer = allEventsList;
        }
        else if(indexOfLastEvent == eventId) {
            newEventsListForCurrPlayer = new ArrayList<>();
        }
        else { 
            newEventsListForCurrPlayer = new ArrayList<>(allEventsList.subList(eventId + 1, indexOfLastEvent));
        }
  
        return newEventsListForCurrPlayer;
    }

    //TODO
    public String createGameFromXML(String xmlData) throws DuplicateGameName_Exception, InvalidParameters_Exception, 
                                                           InvalidXML_Exception {
        try {
            
            JaxBXmlParser.loadSettingsFromXml(xmlData);

            checkCaseOfDuplicateGameName(JaxBXmlParser.getGameName());
            this.rummikubLogic = new GameLogic();
            this.rummikubLogic.initGameFromFile(JaxBXmlParser.getPlayerArray(), JaxBXmlParser.getBoard(),
                                           JaxBXmlParser.getCurrPlayer(), JaxBXmlParser.getGameName());
            initCurrentPlayerMove();
                
        } catch (SAXException | IOException ex) {
            InvalidXML invalidXML = new InvalidXML();
            RummikubFault rummikubFualt = new RummikubFault();

            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("xml file error");
            invalidXML.setFaultInfo(rummikubFualt);
            invalidXML.setMessage(Utils.Constants.ErrorMessages.FAIL_LOADING_FILE_MSG);
            throw new InvalidXML_Exception(Utils.Constants.ErrorMessages.FAIL_LOADING_FILE_MSG, invalidXML);
        } 
        this.isLoadedFromXML = LOADED_FROM_XML;
        return this.rummikubLogic.getGameSettings().getGameName();
    }
    
    //DONE
    public List<PlayerDetails> getPlayersDetails(String gameName) throws GameDoesNotExists_Exception {
        
        validateParamsAndThrowExceptionInIlegalCase(gameName);

        List<PlayerDetails> playerDetailsList = makePlayerDetailsListWithoutTilesList();

        return playerDetailsList;
    }

    //DONE
    public void createGame(String gameName, int humanPlayers, int computerizedPlayers) throws DuplicateGameName_Exception,
                                                                                              InvalidParameters_Exception {
        if (this.gameStatus == GameStatus.FINISHED) {
            initGameComponetsToPrepareForNextGame();
        }
        validateParamsAndThrowExceptionInIlegalCase(gameName, humanPlayers, computerizedPlayers);
        
        Settings gameSettings = new Settings(gameName, humanPlayers, computerizedPlayers);
        createNewGame(gameSettings);
        
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

        if (this.isLoadedFromXML){
            newPlayer = this.rummikubLogic.getPlayerByName(playerName);
        }
        else {
            newPlayer = new HumanPlayer(playerName);
            this.rummikubLogic.addNewHumanPlayer(newPlayer);
        }


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

        return findPlayerDetails(playerId);
    }

    //TODO
    public void createSequence(int playerId, List<ws.rummikub.Tile> tiles) throws InvalidParameters_Exception {

        setTimerForPlayerResponse(playerId);

        validateParamsAndThrowExceptionInIlegalCase(playerId, tiles);
        this.eventManager.addCreateSequenceEvent(playerId, tiles);
        
        Board currBoard = this.currentPlayerMove.getBoardAfterMove();
        int sequenceIndex = currBoard.isEmpty()? 0 : currBoard.boardSize();
        int sequencePosition = 0;
        
        for (ws.rummikub.Tile tile : tiles) {
            moveTileFromHandToBoard(playerId, tile, sequenceIndex, sequencePosition);
            sequencePosition++;
        }
        /**
        i dont think this is the right one
        for (ws.rummikub.Tile tile : tiles) {
            addTile(playerId, tile, sequenceIndex, sequencePosition);
            sequencePosition++;
        }
        */
    }

    //TODO
    public void addTile(int playerId, ws.rummikub.Tile tile, int sequenceIndex, int sequencePosition) 
                                                                      throws InvalidParameters_Exception {
        

        validateParamsAndThrowExceptionInIlegalCase(playerId, tile, sequenceIndex, sequencePosition);
        
        setTimerForPlayerResponse(playerId);
        //we know the params are VALID
        
        Serie serie = this.currentPlayerMove.getBoardAfterMove().getSeries(sequenceIndex);
        final int END_OF_THE_SERIES = serie.isEmptySeries()? 0 : serie.getSizeOfSerie() - INDEX_NORMALIZATION;
        
        if (sequencePosition > START_OF_THE_SERIES && sequencePosition < END_OF_THE_SERIES ) {
            //split case
            this.eventManager.addCreateSequenceEvent(playerId, /*no need for this param so far*/null);
            int targetSequencePosition = 1;
            int indexLastSerie = this.currentPlayerMove.getBoardAfterMove().isEmpty()? 
                    0 : this.currentPlayerMove.getBoardAfterMove().boardSize();
            
            this.eventManager.addTileAddedEvent(playerId, tile, sequenceIndex, sequencePosition);
            moveTileFromHandToBoard(playerId, tile, indexLastSerie, START_OF_THE_SERIES);
            
            for (int indexSourceTile = sequencePosition ; indexSourceTile < serie.getSizeOfSerie(); indexSourceTile++) {
                moveTile(playerId, sequenceIndex, indexSourceTile, indexLastSerie, targetSequencePosition);
                targetSequencePosition++;
            }
        }
        else {
            //adding tile to end or start of serie
            this.eventManager.addTileAddedEvent(playerId, tile, sequenceIndex, sequencePosition);
            moveTileFromHandToBoard(playerId, tile, sequenceIndex, sequencePosition);
        }
    }
    
    //TODO
    public void takeBackTile(int playerId, int sequenceIndex, int sequencePosition) 
                                                            throws InvalidParameters_Exception {

        validateParamsAndThrowExceptionInIlegalCase(playerId, sequenceIndex, sequencePosition);

        setTimerForPlayerResponse(playerId);
        this.eventManager.addTakeBackTileEvent(playerId, sequenceIndex, sequencePosition);
        
        Point source = new Point(sequenceIndex, sequencePosition);
        SingleMove singleMove = new SingleMove(source, SingleMove.MoveType.BOARD_TO_HAND);
        Tile logicTile  = this.currentPlayerMove.getBoardAfterMove().getSpecificTile(sequenceIndex, sequencePosition);
        ws.rummikub.Tile jaxbTile = convertLogicTileToWsTile(logicTile);
        
        if (dealWithSingleMoveResualt(singleMove)) {
            PlayerDetails playerDetails = findPlayerDetails(playerId);
            playerDetails.getTiles().add(jaxbTile);
        }
    }

    //TODO
    public void moveTile(int playerId, int sourceSequenceIndex, 
                         int sourceSequencePosition, int targetSequenceIndex, 
                         int targetSequencePosition) throws InvalidParameters_Exception {


        //TODO - finish writing that method
        validateParamsAndThrowExceptionInIlegalCase(playerId, sourceSequenceIndex, sourceSequencePosition, targetSequenceIndex, targetSequencePosition);
        
        setTimerForPlayerResponse(playerId);
        this.eventManager.addMoveTileEvent(playerId, sourceSequenceIndex, sourceSequencePosition, targetSequenceIndex, targetSequencePosition);

        Point source = new Point(sourceSequenceIndex, sourceSequencePosition);
        Point target = new Point(targetSequenceIndex, targetSequencePosition);

        SingleMove singleMove = new SingleMove(target, source, SingleMove.MoveType.BOARD_TO_BOARD);
        dealWithSingleMoveResualt(singleMove);
    }

    //TODO
    public void finishTurn(int playerId) throws InvalidParameters_Exception {

        //REALY?????? MAYBE WITH DIFF ID??
        
        validateParamsAndThrowExceptionInIlegalCase(playerId);
        
        this.timer.cancel();
        this.eventManager.addFinishTurnEvent(playerId);

        if (!this.rummikubLogic.playSingleTurn(this.currentPlayerMove)) {
            revertTheTurn(playerId);
        }
        
        if (this.rummikubLogic.isReachedOneOfEndGameConditions()) {
            onGameOverActions();
        }
        else {
            onSwapTurnActions();
        }
        
        
    }

    //TODO
    public void resign(int playerId) throws InvalidParameters_Exception {
       //means player cant swap turn, he has to take back all his tiles?????
        
        validateParamsAndThrowExceptionInIlegalCase(playerId);
        this.timer.cancel();
        //finish wrtiting this method - used with timer and here
        doWhenPlayerResign(playerId);
        


        //TODO - FINISH WRTITING LOGIC
    }
    
    //********** Public functions used by the Web Service - END **********/
    // </editor-fold>

    //private methods:
    
    private void createNewGame(Settings gameSetting) {
        this.rummikubLogic = new GameLogic();
        this.isLoadedFromXML = !LOADED_FROM_XML;

        this.rummikubLogic.setGameSettings(gameSetting);
        
        //A: i changed it to new....
        //this.rummikubLogic.setGameOriginalInputedSettings(gameSetting);
        this.rummikubLogic.setGameOriginalInputedSettings( new Settings(gameSetting));
        
        this.rummikubLogic.initGameViaWebRequest();
                
        //inits only computer players
        initPlayerDetailesList();
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
        cheackCaseTileLocationIndexesAreInvalid(sequenceIndex, sequencePosition);
    }
    
    private void validateParamsAndThrowExceptionInIlegalCase(int playerId, ws.rummikub.Tile tile, int sequenceIndex, int sequencePosition) throws InvalidParameters_Exception {
        checkCaseOfPlayerNotExsists(playerId);
        ArrayList<ws.rummikub.Tile> tiles = new ArrayList<>();
        tiles.add(tile);
        checkCaseOfIlegalTileListThatRepresentsSeries(tiles);
        cheackCaseTileLocationIndexesAreInvalid(sequenceIndex, sequencePosition);
    }
        
    private void validateParamsAndThrowExceptionInIlegalCase(int playerId, int sourceSequenceIndex, 
                                                             int sourceSequencePosition, int targetSequenceIndex, 
                                                             int targetSequencePosition) throws InvalidParameters_Exception {
        checkCaseOfPlayerNotExsists(playerId);
        cheackCaseTileLocationIndexesAreInvalid(sourceSequenceIndex, sourceSequencePosition);
        cheackCaseTileLocationIndexesAreInvalid(targetSequenceIndex, targetSequencePosition);
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
        //**IMPORTANT**:: isGameNameAlreadyExsists(gameName) = > always returns false
        if (isGameNameAlreadyExsists(gameName)) {
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

        PlayerDetails playerDetails = findPlayerDetails(playerId);
        
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
        
        PlayerDetails playerDetails = findPlayerDetails(playerId);
        
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
        
        if (this.eventManager.indexOfLastEvent() < eventId) {
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
        this.rummikubLogic.getPlayers().stream().forEach((currPlayer) -> {
            addPlayerToPlayerDetailesList(currPlayer);
        });
        //for (Player currPlayer : this.rummikubLogic.getPlayers()) {
            //addPlayerToPlayerDetailesList(currPlayer);
        //}
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
        playerDetails.getTiles().clear();

        for (Tile currTile : currPlayer.getListPlayerTiles()) {
            ws.rummikub.Tile jaxbTile = convertLogicTileToWsTile(currTile);
            playerDetails.getTiles().add(jaxbTile);
        }
    }
    
    private ws.rummikub.Tile convertLogicTileToWsTile(Tile currTile) {
        ws.rummikub.Tile jaxbTile = new ws.rummikub.Tile();

        setJaxbTileColor(jaxbTile, currTile);
        jaxbTile.setValue(currTile.getEnumTileNumber().getTileNumberValue());

        return jaxbTile;
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
            playerDetailsToCopy.getTiles().stream().forEach((currTile) -> { playerDetails.getTiles().add(currTile); });
            //for (ws.rummikub.Tile currTile : playerDetailsToCopy.getTiles()) {
            //    playerDetails.getTiles().add(currTile);
            //}
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
            this.playerDetailes.values().stream().forEach((currPlayerDetailes) -> { 
                currPlayerDetailes.setStatus(PlayerStatus.ACTIVE); });
            
            //for (PlayerDetails currPlayerDetailes : this.playerDetailes.values()) {
            //    currPlayerDetailes.setStatus(PlayerStatus.ACTIVE);
            //}
            
            
            //walk throw details list and set status to active????
//            for (PlayerDetails playerDetailes : this.playerDetailesList) {
//                playerDetailes.setStatus(PlayerStatus.ACTIVE);
//            }

            initCurrentPlayerMove();
            this.rummikubLogic.shufflePlayersBeforeStartingGame();
            
            this.eventManager.addGameStartEvent();
            this.eventManager.addPlayerTurnEvent(this.rummikubLogic.getCurrentPlayer().getName());
            int playerId = findPlayerId(this.rummikubLogic.getCurrentPlayer().getName()).getPlayerId();

            
            if (this.rummikubLogic.getCurrentPlayer().getIsHuman()) {
                setTimerForPlayerResponse(playerId);
            }
            else {
                onComputerTurn(playerId);
            }
        }
    }

    private PlayerDetails findPlayerDetails(int playerId) {
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
    
    private PlayerId findPlayerId(String name) {
        boolean found = false;
        PlayerId playerId = null;
        
        for (Iterator<PlayerId> iterator = this.playerDetailes.keySet().iterator(); !found && iterator.hasNext();) {
            PlayerId currPlayerId = iterator.next();
            
            found = currPlayerId.getPlayerName().equals(name);
            if(found) {
                playerId = currPlayerId; 
            }
        }
        
        return playerId;
    }
    

    private void checkCaseOfIlegalTileListThatRepresentsSeries(List<ws.rummikub.Tile>  tiles) throws InvalidParameters_Exception {
        boolean foundProblem = tiles.isEmpty();
        
        if(!foundProblem) {
            foundProblem = tiles.size() == Tile.MAX_TILE_VALUE;
            for (Iterator<ws.rummikub.Tile> it = tiles.iterator(); !foundProblem && it.hasNext();) {
                ws.rummikub.Tile tile = it.next();
                foundProblem = tile.getColor() != null && tile.getValue() >= Tile.MIN_TILE_VALUE && tile.getValue() <= Tile.MAX_TILE_VALUE; 
            }
        }
        
        if (foundProblem) {
            InvalidParameters invalidParameters = new InvalidParameters();
            RummikubFault rummikubFualt = new RummikubFault();

            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("invalid tile list");
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.INVALID_TILE_LIST);

            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.INVALID_TILE_LIST,
                        invalidParameters);
        }
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

    private Tile convertWsTileToLogicTile(ws.rummikub.Tile tile) {
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

    private void revertTheTurn(int playerId) {
        ArrayList<Serie> lastTurnBoard = this.rummikubLogic.getGameBoard().getListOfSerie();
        ArrayList<ws.rummikub.Tile> jaxbTilesList = new ArrayList<>();
        
        this.eventManager.addRevertEvent(playerId);
        PlayerDetails currentPlayerDetails = findPlayerDetails(playerId);
        initPlayerDetailsTileList(currentPlayerDetails, this.rummikubLogic.getCurrentPlayer());
        
        for (Serie serie : lastTurnBoard) {
            for (Tile logicTile : serie.getSerieOfTiles()) {
                jaxbTilesList.add(convertLogicTileToWsTile(logicTile));
            }
            
            this.eventManager.addCreateSequenceEvent(playerId, jaxbTilesList);
            jaxbTilesList.clear();
            //this.eventManager.addCreateSequenceEvent(playerId,/*not used right now*/ null);
        }
        
        onSwapTurnActions();
    }

    private void cheackCaseTileLocationIndexesAreInvalid(int sequenceIndex, int sequencePosition) throws InvalidParameters_Exception {

        InvalidParameters invalidParameters = new InvalidParameters();
        RummikubFault rummikubFualt = new RummikubFault();

        if (isNegativeNumber(sequenceIndex)) {
            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("Negative sequence index:" + String.valueOf(sequenceIndex));
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.NEGATIVE_SEQUENCE_INDEX + String.valueOf(sequenceIndex));
            
            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.NEGATIVE_SEQUENCE_INDEX + String.valueOf(sequenceIndex),
                                                  invalidParameters);
        }
        
        if (isNegativeNumber(sequencePosition)) {
            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("Negative position index:" + String.valueOf(sequencePosition));
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.NEGATIVE_TILE_POSITION_INDEX + String.valueOf(sequencePosition));
            
            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.NEGATIVE_TILE_POSITION_INDEX + String.valueOf(sequencePosition),
                                                  invalidParameters);
        }
        
        if (sequenceIndex <= this.currentPlayerMove.getBoardAfterMove().boardSize()) {
            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("such index of serie not exsists:" + String.valueOf(sequenceIndex));
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.ILEGAL_SEQUENCE_INDEX + String.valueOf(sequenceIndex));
            
            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.ILEGAL_SEQUENCE_INDEX  + String.valueOf(sequenceIndex),
                                                  invalidParameters);
        }

        if (sequencePosition <= this.currentPlayerMove.getBoardAfterMove().getSeries(sequenceIndex).getSizeOfSerie()) {
            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("such tile position of tile not exsists:" + String.valueOf(sequencePosition));
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.ILEGAL_TILE_POSITION_INDEX + String.valueOf(sequencePosition));
            
            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.ILEGAL_TILE_POSITION_INDEX + String.valueOf(sequencePosition),
                                                  invalidParameters);
        }
    }

    private void onSwapTurnActions() {
        this.rummikubLogic.swapTurns();
        initCurrentPlayerMove();
        this.eventManager.addPlayerTurnEvent(this.rummikubLogic.getCurrentPlayer().getName());
        int playerId = findPlayerId(this.rummikubLogic.getCurrentPlayer().getName()).getPlayerId();
        
        if(this.rummikubLogic.getCurrentPlayer().getIsHuman()) {
            setTimerForPlayerResponse(playerId);
        }
        else {
            onComputerTurn(playerId);
        }
    }

    private void onGameOverActions() {
        this.gameStatus = GameStatus.FINISHED;
        String gameResult = this.rummikubLogic.gameResult();
        this.eventManager.addGameOverEvent();
        this.eventManager.addGameWinnerEvent(gameResult);
    }

    private void addEventsAfterComputerMove(SingleMove singleMove, int playerId) {
        if (singleMove != null) {
            int indexToAddNewSerieToBoard = this.currentPlayerMove.getBoardAfterMove().boardSize();

            if(indexToAddNewSerieToBoard == singleMove.getpTarget().getX()) {
                ArrayList<ws.rummikub.Tile> tileList = new ArrayList<>();
                tileList.add(convertLogicTileToWsTile(this.currentPlayerMove.getHandAfterMove().get(singleMove.getnSource())));
                try{
                    createSequence(playerId, tileList);
                }
                catch (InvalidParameters_Exception ex) {}
            }
            else {
                ws.rummikub.Tile jaxbTile = convertLogicTileToWsTile(this.currentPlayerMove.getHandAfterMove().get(singleMove.getnSource()));
                try{
                    addTile(playerId, jaxbTile, singleMove.getpTarget().x, singleMove.getpTarget().y);
                }
                catch (InvalidParameters_Exception ex) {}
            }    
        }
        else {
            currentPlayerMove.setIsTurnSkipped(PlayersMove.USER_WANT_SKIP_TRUN);
        }
        
    }

    private void initGameComponetsToPrepareForNextGame() {

        this.rummikubLogic = null;
        this.serieGenerator = new SeriesGenerator();
        this.newMoveGenerator = new ComputerSingleMoveGenerator();
        //private PlayersMove currentPlayerMove;

        this.playerDetailes.clear();
        this.gameStatus = GameStatus.WAITING;
        this.isLoadedFromXML = !LOADED_FROM_XML;
        this.timer = new Timer(DEAMON_THREAD);
        this.eventManager.clearAllEvents();
    }

    
    private class EventManager{

        //Private members
        private final ArrayList<Event> gameEventList;

        //Constractor
        public EventManager() {
            this.gameEventList = new ArrayList<>();
        }
        
        // <editor-fold defaultstate="collapsed" desc="Public function to add game events">
        
        // <editor-fold defaultstate="collapsed" desc="Game Events Description">
        /*
        • Game Start – game started
        • Game Over – game ended
        • Game Winner – the winner of the game (play name will be in the event)
        • Player Turn – indicates who’s the current player
        • Player Finished Turn – player finished making his moves
        • Player Resigned – player resigned from game
        • Sequence Created – indicates a sequence was created
        • Tile Added – indicates a tile was added from a player to the board
        • Tile Moved – indicates a tile was moved on the board
        • Tile Returned – indicates a tile was taken from the board back to a player
        • Revert – indicates the players’ moves did not sum up to a valid board, thus the board is reverted back to the state before the players’ moves.
        */
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Each Game Event Methods">
        /*
        • int getEventID()
        • int getTimeoutCount() – will be 0 in case no timer is active
        • int getEventType()
        • String getPlayerName() – the player to which this event is related to
        • Tile[] getTiles()
        • int getSourceSequenceIndex()
        • int getSourceSequencePosition()
        • int getTargetSequenceIndex()
        • int getTargetSequencePosition()
        */
        // </editor-fold>

        public void addGameStartEvent() {
            Event gameStartEvent = new Event();

            gameStartEvent.setId(indexForNewtEvent());
            gameStartEvent.setTimeout(DISABLED_TIMER);
            //gameStartEvent.getTiles();
            //gameStartEvent.setPlayerName(findPlayerByPlayerId(playerId).getName(/*MAYBE NOT NEED THAT SET*/null));
            //gameStartEvent.setSourceSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //gameStartEvent.setSourceSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            //gameStartEvent.setTargetSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //gameStartEvent.setTargetSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            gameStartEvent.setType(EventType.GAME_START);
            this.gameEventList.add(gameStartEvent);
        }
        
        //TODO
        public void addGameOverEvent() {
            Event gameOverEvent = new Event();

            gameOverEvent.setId(indexForNewtEvent());
            gameOverEvent.setTimeout(DISABLED_TIMER);
            //gameOverEvent.getTiles();
            //gameOverEvent.setPlayerName(findPlayerByPlayerId(playerId).getName(/*MAYBE NOT NEED THAT SET*/null));
            //gameOverEvent.setSourceSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //gameOverEvent.setSourceSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            //gameOverEvent.setTargetSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //gameOverEvent.setTargetSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            gameOverEvent.setType(EventType.GAME_OVER);
            this.gameEventList.add(gameOverEvent);
        }

        //TODO
        public void addGameWinnerEvent(String winnerNameOrTie) {
            Event gameWinnerEvent = new Event();

            gameWinnerEvent.setId(indexForNewtEvent());
            gameWinnerEvent.setTimeout(DISABLED_TIMER);
            //gameWinnerEvent.getTiles();
            gameWinnerEvent.setPlayerName(winnerNameOrTie);
            //gameWinnerEvent.setSourceSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //gameWinnerEvent.setSourceSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            //gameWinnerEvent.setTargetSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //gameWinnerEvent.setTargetSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            gameWinnerEvent.setType(EventType.GAME_WINNER);
            this.gameEventList.add(gameWinnerEvent);
        }    

        //TODO
        public void addPlayerTurnEvent(String name) {
            Event playerTrunEvent = new Event();

            playerTrunEvent.setId(indexForNewtEvent());
            playerTrunEvent.setTimeout((int)TIMER_DELAY);
            //playerTrunEvent.getTiles();
            playerTrunEvent.setPlayerName(name);
            //playerTrunEvent.setSourceSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //playerTrunEvent.setSourceSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            //playerTrunEvent.setTargetSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //playerTrunEvent.setTargetSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            playerTrunEvent.setType(EventType.PLAYER_TURN);
            this.gameEventList.add(playerTrunEvent);
        } 
        
        //TODO - TIMER and GET_TILES
        public void addFinishTurnEvent(int playerId) {
            Event finishTurnEvent = new Event();

            finishTurnEvent.setId(indexForNewtEvent());
            finishTurnEvent.setPlayerName(findPlayerDetails(playerId).getName());
            finishTurnEvent.setTimeout(DISABLED_TIMER);
            //finishTurnEvent.getTiles();
            //finishTurnEvent.setSourceSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //finishTurnEvent.setSourceSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            //finishTurnEvent.setTargetSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //finishTurnEvent.setTargetSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            finishTurnEvent.setType(EventType.PLAYER_FINISHED_TURN);
            gameEventList.add(finishTurnEvent);
        }

        //TODO - TIMER and GET_TILES
        public void addResignEvent(int playerId) {
            Event resignEvent = new Event();

            resignEvent.setId(indexForNewtEvent());
            resignEvent.setPlayerName(findPlayerDetails(playerId).getName());
            resignEvent.setTimeout(DISABLED_TIMER);
            //resignEvent.getTiles();
            //resignEvent.setSourceSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //resignEvent.setSourceSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            //resignEvent.setTargetSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //resignEvent.setTargetSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            resignEvent.setType(EventType.PLAYER_RESIGNED);
            gameEventList.add(resignEvent);
        }

        //TODO - TIMER and GET_TILES
        public void addCreateSequenceEvent(int playerId, List<ws.rummikub.Tile> tiles) {
            Event newSequenceCtearedEvent = new Event();
            int index = currentPlayerMove.getBoardAfterMove().isEmpty()? 
                        0 : currentPlayerMove.getBoardAfterMove().boardSize();
            
            newSequenceCtearedEvent.setId(indexForNewtEvent());
            newSequenceCtearedEvent.setPlayerName(findPlayerDetails(playerId).getName());
            newSequenceCtearedEvent.setTimeout((int)TIMER_DELAY);
            tiles.stream().forEach((tile) -> { newSequenceCtearedEvent.getTiles().add(tile); });
            //newSequenceCtearedEvent.setSourceSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //newSequenceCtearedEvent.setSourceSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            newSequenceCtearedEvent.setTargetSequenceIndex(index);
            //newSequenceCtearedEvent.setTargetSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            newSequenceCtearedEvent.setType(EventType.SEQUENCE_CREATED);
            gameEventList.add(newSequenceCtearedEvent);
        }
               
        //TODO - TIMER and GET_TILES
        public void addTileAddedEvent(int playerId, ws.rummikub.Tile tile, int sequenceIndex, int sequencePosition) {
            Event addTileEvent = new Event();

            addTileEvent.setId(indexForNewtEvent());
            addTileEvent.setPlayerName(findPlayerDetails(playerId).getName());
            addTileEvent.setTimeout((int)TIMER_DELAY);
            addTileEvent.getTiles().add(tile);
            //addTileEvent.setSourceSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //addTileEvent.setSourceSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            addTileEvent.setTargetSequenceIndex(sequenceIndex);
            addTileEvent.setTargetSequencePosition(sequencePosition);
            addTileEvent.setType(EventType.TILE_ADDED);
            gameEventList.add(addTileEvent);
        }

        //TODO - TIMER and GET_TILES
        public void addMoveTileEvent(int playerId, int sourceSequenceIndex, int sourceSequencePosition, int targetSequenceIndex, int targetSequencePosition) {
            Event tileMovedEvent = new Event();

            tileMovedEvent.setId(indexForNewtEvent());
            tileMovedEvent.setPlayerName(findPlayerDetails(playerId).getName());
            tileMovedEvent.setTimeout((int)TIMER_DELAY);
            //tileMovedEvent.getTiles();
            tileMovedEvent.setSourceSequenceIndex(sourceSequenceIndex);
            tileMovedEvent.setSourceSequencePosition(sourceSequencePosition);
            tileMovedEvent.setTargetSequenceIndex(targetSequenceIndex);
            tileMovedEvent.setTargetSequencePosition(targetSequencePosition);
            tileMovedEvent.setType(EventType.TILE_MOVED);
            gameEventList.add(tileMovedEvent);
        }

        //TODO - TIMER and GET_TILES
        public void addTakeBackTileEvent(int playerId, int sequenceIndex, int sequencePosition) {
            Event takeTileBackEvent = new Event();

            takeTileBackEvent.setId(indexForNewtEvent());
            takeTileBackEvent.setPlayerName(findPlayerDetails(playerId).getName());
            takeTileBackEvent.setTimeout((int)TIMER_DELAY);
            //takeTileBackEvent.getTiles();
            takeTileBackEvent.setSourceSequenceIndex(sequenceIndex);
            takeTileBackEvent.setSourceSequencePosition(sequencePosition);
            //takeTileBackEvent.setTargetSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //takeTileBackEvent.setTargetSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            takeTileBackEvent.setType(EventType.TILE_RETURNED);
            gameEventList.add(takeTileBackEvent);
        }

        //TODO - TIMER and GET_TILES
        public void addRevertEvent(int playerId) {
            Event revertEvent = new Event();

            revertEvent.setId(indexForNewtEvent());
            revertEvent.setTimeout(DISABLED_TIMER);
            //revertEvent.getTiles();
            revertEvent.setPlayerName(findPlayerDetails(playerId).getName());
            //revertEvent.setSourceSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //revertEvent.setSourceSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            //revertEvent.setTargetSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //revertEvent.setTargetSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            revertEvent.setType(EventType.REVERT);
            gameEventList.add(revertEvent);
        }
        
        // </editor-fold>
        
        public int indexOfLastEvent() {
            return gameEventList.isEmpty()? 0 : gameEventList.size() - INDEX_NORMALIZATION;
        }
        
        public int indexForNewtEvent() {
            return gameEventList.isEmpty()? 0 : gameEventList.size();
        }
        
        public ArrayList<Event> getGameEventList() {
            ArrayList<Event> eventsList = this.gameEventList;
            return eventsList;
        }

        private void clearAllEvents() {
            this.gameEventList.clear();
        }
    }
    

    private void setTimerForPlayerResponse(int playerId) {
        if (findPlayerDetails(playerId).getType() == PlayerType.HUMAN) {
            this.timer.cancel();
            this.timer  = new Timer(DEAMON_THREAD);

            this.timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    doWhenPlayerResign(playerId);
                }
            }, TIMER_DELAY);
        }
    }
    
    private void doWhenPlayerResign(int playerId) {
        this.eventManager.addResignEvent(playerId);
        
        this.rummikubLogic.removeCurrentPlayerFromTheGame();
        
        if (!this.rummikubLogic.isGameOver()) {
            this.rummikubLogic.swapTurns();
        }
        
        if (this.rummikubLogic.isReachedOneOfEndGameConditions()) {
            onGameOverActions();
        }
        else {
            onSwapTurnActions();
        }
    }
    
    private void moveTileFromHandToBoard(int playerId, ws.rummikub.Tile tile, int sequenceIndex, int sequencePosition) throws InvalidParameters_Exception {

        Point target = new Point(sequenceIndex, sequencePosition);
        Tile logicalTile = convertWsTileToLogicTile(tile);

        int IndexInHand = this.currentPlayerMove.getHandAfterMove().indexOf(logicalTile);
        SingleMove singleMove = new SingleMove(target, IndexInHand, SingleMove.MoveType.HAND_TO_BOARD);

        if (dealWithSingleMoveResualt(singleMove)) {
            PlayerDetails playerDetails = findPlayerDetails(playerId);
            playerDetails.getTiles().remove(IndexInHand);
        }
    }
    
     private  void onComputerTurn(int playerId) {
        boolean isComputerPlayer = !rummikubLogic.getCurrentPlayer().getIsHuman();

        if (isComputerPlayer) {

            while (isComputerPlayer) {
                SingleMove singleMove = computerPlayerMakesSingleMove();
                addEventsAfterComputerMove(singleMove, playerId);

                try {
                    Thread.sleep(DELAY_FOR_COMPUTER_MOVE);
//                        showCurrentGameBoardAndCurrentPlayerHand();
                } catch (InterruptedException ex) {
                }

                if (currentPlayerMove.getIsTurnSkipped() || this.newMoveGenerator.isTurnFinnised()) {
                    this.newMoveGenerator.initComputerSingleMoveGenerator();
 
                    try{
                        finishTurn(findPlayerId(this.rummikubLogic.getCurrentPlayer().getName()).getPlayerId());
                    }
                    catch(InvalidParameters_Exception ex) {}
                }

                isComputerPlayer = !rummikubLogic.getCurrentPlayer().getIsHuman();
            }
        } 
        else {
            onSwapTurnActions();
        }
    }
     
    private SingleMove computerPlayerMakesSingleMove() {
        SingleMove singleMove;
        Serie serie;

        if (newMoveGenerator.isFinishedGeneratingLastSerie()) {
            serie = this.serieGenerator.generateSerieMove(currentPlayerMove.getHandAfterMove(), currentPlayerMove.getIsFirstMoveDone());
            this.newMoveGenerator.setSerieToPlaceOnBoard(serie);

            if (serie != null) {
                this.newMoveGenerator.setBoardSizeBeforeMove(currentPlayerMove.getBoardAfterMove().boardSize());
            }
        }

        singleMove = newMoveGenerator.generateSingleMove(currentPlayerMove.getHandAfterMove(), currentPlayerMove.getBoardAfterMove());

        if (this.newMoveGenerator.isTurnSkipped()) {
            currentPlayerMove.setIsTurnSkipped(this.newMoveGenerator.isTurnSkipped());
        }

        return singleMove;
    }

    private void ImplementCompuerPlayerTurn(SingleMove singleMove) {
        if (singleMove != null) {
            try {
                dealWithSingleMoveResualt(singleMove);
            } catch (Exception ex) {
                currentPlayerMove.setIsTurnSkipped(PlayersMove.USER_WANT_SKIP_TRUN);
            }
        }
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

