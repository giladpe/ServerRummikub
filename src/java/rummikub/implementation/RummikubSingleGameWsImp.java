/*
 * This class is responsible to implemenet the web service functionality of single game
 */

package rummikub.implementation;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
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

public class RummikubSingleGameWsImp {
    
    //Constants:
    private static final boolean WITH_TILE_LIST = true;
    private static final boolean LOADED_FROM_XML = true;
    private static final boolean DEAMON_THREAD = true;
    private static final boolean ADD_EVENT = true;
    private static int INDEX_NORMALIZATION = 1;
    private static final int START_OF_THE_SERIES = 0;
    private static final long TIMER_DELAY = TimeUnit.MINUTES.toMillis(2);
    private static final long DELAY_FOR_COMPUTER_MOVE = 1500;
    private static final int NOT_RELATED_TO_ANY_PLAYER = -1;
    private static final String CREATED_BY_FILE = "";
    private static final SingleMove IS_FINISHED_TURN = null;
    private static final int DISABLED_TIMER = 0;

    //Private Members:
    
    //Game logic members
    private GameLogic rummikubLogic;
    private SeriesGenerator serieGenerator;
    private ComputerSingleMoveGenerator newMoveGenerator;
    private PlayersMove currentPlayerMove;
    
    //Server related members
    private HashMap<PlayerId,PlayerDetails> playerDetailes;
    private GameStatus gameStatus;
    private boolean isLoadedFromXML;
    private Timer timer;
    private EventManager eventManager;
    
    //Constractors:
    public RummikubSingleGameWsImp() {
        init(!LOADED_FROM_XML);
    }
    
    public RummikubSingleGameWsImp(boolean isLoadedFromXML) {
        init(isLoadedFromXML);
    }
    
    private void init(boolean isLoadedFromXML) {
        this.serieGenerator = new SeriesGenerator();
        this.newMoveGenerator = new ComputerSingleMoveGenerator();
        
        this.playerDetailes = new HashMap<>();
        this.gameStatus = GameStatus.WAITING;
        this.isLoadedFromXML = isLoadedFromXML;
        this.timer = new Timer(DEAMON_THREAD);
        this.eventManager = new EventManager();
    }

    // <editor-fold defaultstate="collapsed" desc="Public functions used by the Web Service">

    //********** Public functions used by the Web Service - START **********/

    //DONE - maybe still things to add if i understood wrong the instructions - releated to TIMER
    public synchronized List<Event> getEvents(int playerId, int eventId) throws InvalidParameters_Exception {
       
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
            newEventsListForCurrPlayer = new ArrayList<>();
            
            for (int i = eventId+1; i < allEventsList.size(); i++) {
                newEventsListForCurrPlayer.add(allEventsList.get(i));
            }
        }
  
        return newEventsListForCurrPlayer;
    }
    
    // <editor-fold defaultstate="collapsed" desc="MOVED TO: RummikubWsImplementation">
//    public String createGameFromXML(String xmlData) throws DuplicateGameName_Exception, InvalidParameters_Exception, 
//                                                           InvalidXML_Exception {
//        try {
//            if (this.gameStatus == GameStatus.FINISHED) {
//                initGameComponetsToPrepareForNextGame();
//            }            
//            JaxBXmlParser.loadSettingsFromXml(xmlData);
//
//            checkCaseOfDuplicateGameName(JaxBXmlParser.getGameName());
//            this.isLoadedFromXML = LOADED_FROM_XML;
//            this.rummikubLogic = new GameLogic();
//            this.rummikubLogic.initGameFromFile(JaxBXmlParser.getPlayerArray(), JaxBXmlParser.getBoard(),
//                                           JaxBXmlParser.getCurrPlayer(), JaxBXmlParser.getGameName());
//            initPlayerDetailesListFromFile();
//            initCurrentPlayerMove();
//                
//        } catch (SAXException | IOException ex) {
//            InvalidXML invalidXML = new InvalidXML();
//            RummikubFault rummikubFualt = new RummikubFault();
//
//            rummikubFualt.setFaultCode(null);
//            rummikubFualt.setFaultString("xml file error");
//            invalidXML.setFaultInfo(rummikubFualt);
//            invalidXML.setMessage(Utils.Constants.ErrorMessages.FAIL_LOADING_FILE_MSG);
//            throw new InvalidXML_Exception(Utils.Constants.ErrorMessages.FAIL_LOADING_FILE_MSG, invalidXML);
//        } 
//        
//        return this.rummikubLogic.getGameSettings().getGameName();
//    }
    
//    public List<PlayerDetails> getPlayersDetails(String gameName) throws GameDoesNotExists_Exception {
//        
//        validateParamsAndThrowExceptionInIlegalCase(gameName);
//
//        List<PlayerDetails> playerDetailsList = makePlayerDetailsListWithoutTilesList();
//
//        return playerDetailsList;
//    }

//    public void createGame(String gameName, int humanPlayers, int computerizedPlayers) throws DuplicateGameName_Exception,
//                                                                                              InvalidParameters_Exception {
//        if (this.gameStatus == GameStatus.FINISHED) {
//            initGameComponetsToPrepareForNextGame();
//        }
//        validateParamsAndThrowExceptionInIlegalCase(gameName, humanPlayers, computerizedPlayers);
//        
//        Settings gameSettings = new Settings(gameName, humanPlayers, computerizedPlayers);
//        createNewGame(gameSettings);
//    }
    
//    public GameDetails getGameDetails(String gameName) throws GameDoesNotExists_Exception {
//        validateParamsAndThrowExceptionInIlegalCase(gameName);
//        
//        GameDetails currentGameDetals = new GameDetails();
//        Settings currGameSetings = this.rummikubLogic.getGameSettings();
//        
//        currentGameDetals.setComputerizedPlayers(currGameSetings.getNumOfCpuPlayers());
//        currentGameDetals.setHumanPlayers(currGameSetings.getNumOfHumanPlayers());
//        currentGameDetals.setJoinedHumanPlayers(this.rummikubLogic.getNumberOfJoinedHumanPlayers());
//        currentGameDetals.setLoadedFromXML(this.isLoadedFromXML);
//        currentGameDetals.setName(currGameSetings.getGameName());
//        currentGameDetals.setStatus(this.gameStatus);
//        
//        return currentGameDetals;
//    }

    // <editor-fold defaultstate="collapsed" desc="TODO - WHEN WE SUPPORT MULTIPLE GAMES - NOW RETURNS CURRENT GAME NAME IN THE LIST">
    //TODO:
    //when we will support multiple game we have to add other game's name's
    // </editor-fold>
//    public List<String> getWaitingGames() {
//        List<String> waitingGameList = new ArrayList<>();
//        String gameName;
//        
//        if(this.rummikubLogic != null && this.gameStatus == GameStatus.WAITING ) {
//            gameName = this.rummikubLogic.getGameSettings().getGameName();
//            waitingGameList.add(gameName);
//        }
//
//        return waitingGameList;
//    }

//    public int joinGame(String gameName, String playerName) throws GameDoesNotExists_Exception, 
//                                                                   InvalidParameters_Exception {
//        validateParamsAndThrowExceptionInIlegalCase(gameName, playerName);
//        Player newPlayer;
//        int playerId;
//
//        if (this.isLoadedFromXML){
//            newPlayer = this.rummikubLogic.getPlayerByName(playerName);
//            PlayerId playerServerId = findPlayerId(newPlayer.getName());
//            PlayerDetails playerDetails = this.playerDetailes.get(playerServerId); 
//            playerId = playerServerId.getPlayerId();
//            playerDetails.setStatus(PlayerStatus.JOINED);
//        }
//        else {
//            newPlayer = new HumanPlayer(playerName);
//            this.rummikubLogic.addNewHumanPlayer(newPlayer);
//            playerId = addPlayerToPlayerDetailesList(newPlayer, PlayerStatus.JOINED, WITH_TILE_LIST);
//        }
//        
//        Thread thread = new Thread(()->{ updateGameStatus(); });
//        thread.setDaemon(DEAMON_THREAD);
//        thread.start();
//        
//        return playerId;
//    }
//
//    public PlayerDetails getPlayerDetails(int playerId) throws GameDoesNotExists_Exception, 
//                                                               InvalidParameters_Exception {
//        
//        validateParamsAndThrowExceptionGameNotExsistsOrInvalidParams(playerId);
//
//        return findPlayerDetails(playerId);
//    }
    // </editor-fold>

    public void createSequence(int playerId, List<ws.rummikub.Tile> tiles) throws InvalidParameters_Exception {

        validateParamsAndThrowExceptionInIlegalCase(playerId, tiles);
        setTimerForPlayerResponse(playerId);

        Board currBoard = this.currentPlayerMove.getBoardAfterMove();
        int sequenceIndex = currBoard.isEmpty()? 0 : currBoard.boardSize();
        int sequencePosition = 0;
        
        for (ws.rummikub.Tile tile : tiles) {
            moveTileFromHandToBoard(playerId, tile, sequenceIndex, sequencePosition);
            sequencePosition++;
        }
        
        this.eventManager.addCreateSequenceEvent(playerId, tiles);
    }
    
    public void addTile(int playerId, ws.rummikub.Tile tile, int sequenceIndex, int sequencePosition) 
                                                                      throws InvalidParameters_Exception {
        
        validateParamsAndThrowExceptionInIlegalCase(playerId, tile, sequenceIndex, sequencePosition);
        
        setTimerForPlayerResponse(playerId);
        Serie serie = this.currentPlayerMove.getBoardAfterMove().getSeries(sequenceIndex);
        final int END_OF_THE_SERIES = serie.isEmptySeries()? 0 : serie.getSizeOfSerie();
        
        if (!isPositionAtStartOrEndOfSeries(sequencePosition, START_OF_THE_SERIES, END_OF_THE_SERIES)) {
            //split case
            try{    
                ArrayList<ws.rummikub.Tile> tileList = new ArrayList<>();
                tileList.add(tile);
                int targetSequencePosition = 1;
                int indexLastSerie = this.currentPlayerMove.getBoardAfterMove().isEmpty()? 
                        0 : this.currentPlayerMove.getBoardAfterMove().boardSize();

                moveTileFromHandToBoard(playerId, tile, indexLastSerie, START_OF_THE_SERIES);
                this.eventManager.addCreateSequenceEvent(playerId, tileList);

                int numOfIterations = serie.getSizeOfSerie() - sequencePosition;

                for (int i = targetSequencePosition; i <= numOfIterations; i++) {
                    moveTileFromBoardToBoard(playerId, sequenceIndex, sequencePosition, indexLastSerie, i, ADD_EVENT);
                }
            } catch (Exception ex){
                revertTheTurn(playerId);
            }
        
        }
        else {
            //adding tile to end or start of serie
            moveTileFromHandToBoard(playerId, tile, sequenceIndex, sequencePosition);
            this.eventManager.addTileAddedEvent(playerId, tile, sequenceIndex, sequencePosition);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Old version of addTile method">
//    public void addTile(int playerId, ws.rummikub.Tile tile, int sequenceIndex, int sequencePosition) 
//                                                                      throws InvalidParameters_Exception {
//        
//        validateParamsAndThrowExceptionInIlegalCase(playerId, tile, sequenceIndex, sequencePosition);
//        
//        setTimerForPlayerResponse(playerId);
//        //we know the params are VALID
//        
//        Serie serie = this.currentPlayerMove.getBoardAfterMove().getSeries(sequenceIndex);
//        //goes with old version
//        //final int END_OF_THE_SERIES = serie.isEmptySeries()? 0 : serie.getSizeOfSerie() - INDEX_NORMALIZATION;
//        final int END_OF_THE_SERIES = serie.isEmptySeries()? 0 : serie.getSizeOfSerie();
//        
//        if (!isPositionAtStartOrEndOfSeries(sequencePosition, START_OF_THE_SERIES, END_OF_THE_SERIES)) {
//            //split case
//            ArrayList<ws.rummikub.Tile> tileList = new ArrayList<>();
//            tileList.add(tile);
//            int targetSequencePosition = 1;
//            int indexLastSerie = this.currentPlayerMove.getBoardAfterMove().isEmpty()? 
//                    0 : this.currentPlayerMove.getBoardAfterMove().boardSize();
//
//            moveTileFromHandToBoard(playerId, tile, indexLastSerie, START_OF_THE_SERIES);
//            this.eventManager.addCreateSequenceEvent(playerId, tileList);
//            
//            int numOfIterations = serie.getSizeOfSerie() - sequencePosition;
//            
//            for (int i = targetSequencePosition; i <= numOfIterations; i++) {
////                moveTile(playerId, sequenceIndex, sequencePosition, indexLastSerie, i);
//                moveTileFromBoardToBoard(playerId, sequenceIndex, sequencePosition, indexLastSerie, i);
//            }
//        }
//        else {
//            //adding tile to end or start of serie
//            moveTileFromHandToBoard(playerId, tile, sequenceIndex, sequencePosition);
//            this.eventManager.addTileAddedEvent(playerId, tile, sequenceIndex, sequencePosition);
//        }
//    }
    // </editor-fold>

    public void takeBackTile(int playerId, int sequenceIndex, int sequencePosition) 
                                                            throws InvalidParameters_Exception {

        validateParamsAndThrowExceptionInIlegalCase(playerId, sequenceIndex, sequencePosition);

        setTimerForPlayerResponse(playerId);
        
        Point source = new Point(sequenceIndex, sequencePosition);
        SingleMove singleMove = new SingleMove(source, SingleMove.MoveType.BOARD_TO_HAND);
        
        if (dealWithSingleMoveResualt(singleMove)) {
            PlayerDetails playerDetails = findPlayerDetails(playerId);
            initPlayerDetailsTileList(playerDetails, this.currentPlayerMove.getHandAfterMove());
            this.eventManager.addTakeBackTileEvent(playerId, sequenceIndex, sequencePosition);
        }
    }
    
    public void moveTile(int playerId, int sourceSequenceIndex, int sourceSequencePosition, int targetSequenceIndex, 
                         int targetSequencePosition) throws InvalidParameters_Exception {

        validateParamsAndThrowExceptionInIlegalCase(playerId, sourceSequenceIndex, sourceSequencePosition, targetSequenceIndex, targetSequencePosition);
        
        setTimerForPlayerResponse(playerId);
        Serie serie = this.currentPlayerMove.getBoardAfterMove().getSeries(targetSequenceIndex);
        final int END_OF_THE_SERIES = serie.isEmptySeries()? 0 : serie.getSizeOfSerie();
        moveTileFromBoardToBoard(playerId, sourceSequenceIndex, sourceSequencePosition, targetSequenceIndex,
                                 targetSequencePosition, ADD_EVENT);

        if (!isPositionAtStartOrEndOfSeries(targetSequencePosition, START_OF_THE_SERIES, END_OF_THE_SERIES)) {
            //split case
            try {
                final int numOfIterations = serie.getSizeOfSerie() - targetSequencePosition;
                final int indexLastSerie = this.currentPlayerMove.getBoardAfterMove().isEmpty()? 
                        0 : this.currentPlayerMove.getBoardAfterMove().boardSize();
                final int serieIndex = this.currentPlayerMove.getBoardAfterMove().indexOf(serie);
                this.eventManager.addCreateSequenceEvent(playerId, new ArrayList<>());
                
                for (int i = 0 ; i < numOfIterations; i++) {

                    moveTileFromBoardToBoard(playerId, serieIndex, targetSequencePosition,
                                             indexLastSerie, i, ADD_EVENT);
                }

            } catch (Exception ex) {
                revertTheTurn(playerId);
            }
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="old version of moveTile method (before Liron's answer) - version with ONE tile in list of sequence created event">
//    public void moveTile(int playerId, int sourceSequenceIndex, int sourceSequencePosition, int targetSequenceIndex, 
//                         int targetSequencePosition) throws InvalidParameters_Exception {
//
//        validateParamsAndThrowExceptionInIlegalCase(playerId, sourceSequenceIndex, sourceSequencePosition, targetSequenceIndex, targetSequencePosition);
//        
//        setTimerForPlayerResponse(playerId);
//        Serie serie = this.currentPlayerMove.getBoardAfterMove().getSeries(targetSequenceIndex);
//        final int END_OF_THE_SERIES = serie.isEmptySeries()? 0 : serie.getSizeOfSerie();
//        moveTileFromBoardToBoard(playerId, sourceSequenceIndex, sourceSequencePosition, targetSequenceIndex,
//                                 targetSequencePosition, ADD_EVENT);
//
//        if (!isPositionAtStartOrEndOfSeries(targetSequencePosition, START_OF_THE_SERIES, END_OF_THE_SERIES)) {
//            //split case
//            try {
//                ArrayList<ws.rummikub.Tile> tileList = new ArrayList<>();
//                Tile logicTile = serie.getSpecificTile(targetSequencePosition);
//                tileList.add(convertLogicTileToWsTile(logicTile));
//                boolean isAlreadyAdded = false;
//                final int indexLastSerie = this.currentPlayerMove.getBoardAfterMove().isEmpty()? 
//                        0 : this.currentPlayerMove.getBoardAfterMove().boardSize();
//
//                final int numOfIterations = serie.getSizeOfSerie() - targetSequencePosition;
//                
//                this.eventManager.addCreateSequenceEvent(playerId, tileList);
//                
//                for (int i = 0 ; i < numOfIterations; i++) {
//                    moveTileFromBoardToBoard(playerId, targetSequenceIndex, targetSequencePosition,
//                                             indexLastSerie, i, isAlreadyAdded);
//                    if (!isAlreadyAdded) {
//                        isAlreadyAdded = true;
//                    }
//                }
//                
//            } catch (Exception ex) {
//                revertTheTurn(playerId);
//            }
//        }
//    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Old version of moveTile method - bugged version used in Ex3 - DO NOT USE">
//    public void moveTile(int playerId, int sourceSequenceIndex, int sourceSequencePosition, int targetSequenceIndex, 
//                         int targetSequencePosition) throws InvalidParameters_Exception {
//
//        validateParamsAndThrowExceptionInIlegalCase(playerId, sourceSequenceIndex, sourceSequencePosition, targetSequenceIndex, targetSequencePosition);
//        
//        setTimerForPlayerResponse(playerId);
//        moveTileFromBoardToBoard(playerId, sourceSequenceIndex, sourceSequencePosition, targetSequenceIndex, targetSequencePosition);
//        Serie serie = this.currentPlayerMove.getBoardAfterMove().getSeries(targetSequenceIndex);
//        final int END_OF_THE_SERIES = serie.isEmptySeries()? 0 : serie.getSizeOfSerie();
//
//        if (!isPositionAtStartOrEndOfSeries(targetSequencePosition, START_OF_THE_SERIES, END_OF_THE_SERIES)) {
//            try {
//                //split case
//                ArrayList<ws.rummikub.Tile> tileList = new ArrayList<>();
//                Tile logicTile = serie.getSpecificTile(targetSequencePosition);
//                tileList.add(convertLogicTileToWsTile(logicTile));
//                boolean isAlreadyAdded = false;
//                int targetSequencePositionInNewSeries = 1;
//                int indexLastSerie = this.currentPlayerMove.getBoardAfterMove().isEmpty()? 
//                        0 : this.currentPlayerMove.getBoardAfterMove().boardSize();
//
//                int numOfIterations = serie.getSizeOfSerie() - targetSequencePosition;
//
//                for (int i = targetSequencePositionInNewSeries ; i < numOfIterations; i++) {
//                    if (!isAlreadyAdded) {
//                        this.eventManager.addCreateSequenceEvent(playerId, tileList);
//                        isAlreadyAdded = true;
//                    }
//                    moveTileFromBoardToBoard(playerId, targetSequenceIndex, targetSequencePosition, indexLastSerie, i);
//                }
//            } catch (Exception ex) {
//                revertTheTurn(playerId);
//            }
//        }
//    }
    // </editor-fold>
    
    public void finishTurn(int playerId) throws InvalidParameters_Exception {

        validateParamsAndThrowExceptionInIlegalCase(playerId);
        
        this.timer.cancel();
        this.eventManager.addFinishTurnEvent(playerId);

        if (!this.rummikubLogic.playSingleTurn(this.currentPlayerMove)) {
            revertTheTurn(playerId);
        }
        else{ 
            PlayerDetails playerDetails = findPlayerDetails(playerId);
            if (!playerDetails.isPlayedFirstSequence()) {
                playerDetails.setPlayedFirstSequence(true);
            }
        }
        
        initPlayerDetailsTileList(findPlayerDetails(playerId), this.rummikubLogic.getCurrentPlayer().getListPlayerTiles());
        
        if (this.rummikubLogic.isReachedOneOfEndGameConditions()) {
            onGameOverActions();
        }
        else {
            onSwapTurnActions();
        }
    }

    public void resign(int playerId) throws InvalidParameters_Exception {
        
        validateParamsAndThrowExceptionInIlegalCase(playerId);
        
        doWhenPlayerResign(playerId);
    }
    
    //********** Public functions used by the Web Service - END **********/
    // </editor-fold>

    //private methods:
    
    private void initCurrentPlayerMove() {
        //init variables in the statrt of the turn
        Board printableBoard = new Board(new ArrayList<>(rummikubLogic.getGameBoard().getListOfSerie()));
        boolean isFirstMoveDone = rummikubLogic.getCurrentPlayer().isFirstMoveDone();
        Player printablePlayer = rummikubLogic.getCurrentPlayer().clonePlayer();
        this.currentPlayerMove = new PlayersMove(printablePlayer.getListPlayerTiles(), printableBoard, isFirstMoveDone);
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
        cheackCaseTileIncreasingOrder(tile, sequenceIndex, sequencePosition);
    }
        
    private void validateParamsAndThrowExceptionInIlegalCase(int playerId, int sourceSequenceIndex, 
                                                             int sourceSequencePosition, int targetSequenceIndex, 
                                                             int targetSequencePosition) throws InvalidParameters_Exception {
        checkCaseOfPlayerNotExsists(playerId);
        cheackCaseTileLocationIndexesAreInvalid(sourceSequenceIndex, sourceSequencePosition);
        cheackCaseTileLocationIndexesAreInvalid(targetSequenceIndex, targetSequencePosition);
        cheackCaseTileIncreasingOrder(sourceSequenceIndex, sourceSequencePosition, targetSequenceIndex, targetSequencePosition);
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
    
    // <editor-fold defaultstate="collapsed" desc="old functions that moved to RummikubWsImplementation">
//    
//    private void validateParamsAndThrowExceptionInIlegalCase(String gameName, int humanPlayers, int computerizedPlayers) throws DuplicateGameName_Exception,
//                                                                                               InvalidParameters_Exception {
//        checkCaseOfDuplicateGameName(gameName);
//        validateNumberOfHumanAndComputerPlayers(humanPlayers, computerizedPlayers);
//    }
//    
//    private void validateParamsAndThrowExceptionInIlegalCase(String gameName) throws GameDoesNotExists_Exception {
//        checkCaseOfGameDoesNotExists(gameName);
//    }
//    
//    private void validateParamsAndThrowExceptionInIlegalCase(String gameName, String playerName) throws GameDoesNotExists_Exception, 
//                                                                                                        InvalidParameters_Exception {
//        checkCaseOfGameDoesNotExists(gameName);
//        checkCaseOfPlayerAlreadyExsists(gameName, playerName);
//        checkCaseOfGameStatusIsNotWaiting(gameName);
//    }
//
//    private void validateParamsAndThrowExceptionGameNotExsistsOrInvalidParams(int playerId) throws GameDoesNotExists_Exception, 
//                                                                                  InvalidParameters_Exception {
//        checkCaseOfPlayerNotExsists(playerId);
//        checkCaseOfGameDoesNotExists(playerId);
//    }
//    
//    private void checkCaseOfDuplicateGameName(String gameName) throws DuplicateGameName_Exception {
//        
//        if (isGameNameAlreadyExsists(gameName)) {
//            DuplicateGameName duplicateGameName  = new DuplicateGameName();
//            RummikubFault rummikubFualt = new RummikubFault();
//
//            rummikubFualt.setFaultCode(null);
//            rummikubFualt.setFaultString("name already exsists");
//            duplicateGameName.setFaultInfo(rummikubFualt);
//            duplicateGameName.setMessage(Utils.Constants.ErrorMessages.GAME_NAME_ALREADY_EXSIST);
//            throw new DuplicateGameName_Exception(Utils.Constants.ErrorMessages.GAME_NAME_ALREADY_EXSIST, duplicateGameName);
//        }
//    }
//    
//    private void checkCaseOfGameDoesNotExists(String gameName) throws GameDoesNotExists_Exception {
//
//        checkCaseOfEmptyStringOrNullOrContainsWhiteSpacesOfGameNotExsists(gameName); 
//        
//        if (isGameNameAlreadyExsists(gameName)) {
//            GameDoesNotExists gameDoesNotExsists = new GameDoesNotExists();
//            RummikubFault rummikubFualt = new RummikubFault();
//
//            rummikubFualt.setFaultCode(null);
//            rummikubFualt.setFaultString("name not exsists");
//            gameDoesNotExsists.setFaultInfo(rummikubFualt);
//            gameDoesNotExsists.setMessage(Utils.Constants.ErrorMessages.GAME_NAME_NOT_EXSIST);
//            throw new GameDoesNotExists_Exception(Utils.Constants.ErrorMessages.GAME_NAME_NOT_EXSIST, gameDoesNotExsists);
//        }
//    }
//    
//    private void checkCaseOfGameDoesNotExists(int playerId) throws GameDoesNotExists_Exception {
//
//        PlayerDetails playerDetails = findPlayerDetails(playerId);
//        
//        if (playerDetails == null) {
//            GameDoesNotExists gameDoesNotExsists = new GameDoesNotExists();
//            RummikubFault rummikubFualt = new RummikubFault();
//
//            rummikubFualt.setFaultCode(null);
//            rummikubFualt.setFaultString("player id not exsists");
//            gameDoesNotExsists.setFaultInfo(rummikubFualt);
//            gameDoesNotExsists.setMessage(Utils.Constants.ErrorMessages.PLAYER_ID_NOT_EXSISTS);
//            throw new GameDoesNotExists_Exception(Utils.Constants.ErrorMessages.PLAYER_ID_NOT_EXSISTS, gameDoesNotExsists);
//        }
//    }
//    
//    private void checkCaseOfPlayerAlreadyExsists(String gameName, String playerName) throws InvalidParameters_Exception {
//        
//        checkCaseOfEmptyStringOrNullOrContainsWhiteSpacesOfInvalidParameters(playerName);
//        
//        if (this.isLoadedFromXML) {
//            PlayerDetails playerDetails = this.playerDetailes.get(findPlayerId(playerName));
//            
//            if (!this.rummikubLogic.getGameSettings().isPlayerNameExists(playerName) || playerDetails.getStatus() == PlayerStatus.JOINED) {
//                InvalidParameters invalidParameters = new InvalidParameters();
//                RummikubFault rummikubFualt = new RummikubFault();
//
//                rummikubFualt.setFaultCode(null);
//                rummikubFualt.setFaultString("For a loaded game such player not exsists");
//                invalidParameters.setFaultInfo(rummikubFualt);
//                invalidParameters.setMessage(Utils.Constants.ErrorMessages.PLAYER_NAME_NOT_EXSISTS_IN_XML_LOADED_GAME);
//
//                throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.PLAYER_NAME_NOT_EXSISTS_IN_XML_LOADED_GAME,
//                        invalidParameters);
//            }
//        }
//        else {
//            if (this.rummikubLogic.getGameSettings().isPlayerNameExists(playerName)) {
//                InvalidParameters invalidParameters = new InvalidParameters();
//                RummikubFault rummikubFualt = new RummikubFault();
//
//                rummikubFualt.setFaultCode(null);
//                rummikubFualt.setFaultString("There is already a player with same name");
//                invalidParameters.setFaultInfo(rummikubFualt);
//                invalidParameters.setMessage(Utils.Constants.ErrorMessages.ILEGAL_PLAYER_NAME);
//
//                throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.ILEGAL_PLAYER_NAME, invalidParameters);
//            }    
//        }
//    }
//
//    private void checkCaseOfGameStatusIsNotWaiting(String gameName) throws InvalidParameters_Exception {
//        
//        if (this.gameStatus != GameStatus.WAITING) {
//            InvalidParameters invalidParameters = new InvalidParameters();
//            RummikubFault rummikubFualt = new RummikubFault();
//            
//            rummikubFualt.setFaultCode(null);
//            rummikubFualt.setFaultString("There game already stared");
//            invalidParameters.setFaultInfo(rummikubFualt);
//            invalidParameters.setMessage(Utils.Constants.ErrorMessages.GAME_NOT_IN_WAITING_STATUS);
//            
//            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.GAME_NOT_IN_WAITING_STATUS,
//                                                  invalidParameters);
//        }
//    }    
//    
//    // <editor-fold defaultstate="collapsed" desc="DONE IN RummikubWsImplementation  - WHEN WE SUPPORT MULTIPLE GAMES - NOW RETURNS ALWYAS FASLE">
//    //TODO:
//    //when we will support multiple game we have to check if the game's name already exsists - now returns always false
//    // </editor-fold>
//    private boolean isGameNameAlreadyExsists(String gameName) {
//        boolean isGameAlreadyExsists = false;
//
//        return isGameAlreadyExsists;
//    }
//
//    private void validateNumberOfHumanAndComputerPlayers(int humanPlayers, int computerizedPlayers) throws InvalidParameters_Exception {
//        InvalidParameters invalidParameters = new InvalidParameters();
//        RummikubFault rummikubFualt = new RummikubFault();
//
//        if (isNegativeNumber(humanPlayers)) {
//            rummikubFualt.setFaultCode(null);
//            rummikubFualt.setFaultString("amout of human players is negative number");
//            invalidParameters.setFaultInfo(rummikubFualt);
//            invalidParameters.setMessage(Utils.Constants.ErrorMessages.NEGATIVE_NUMBER_OF_HUMAN_PLAYERS);
//            
//            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.NEGATIVE_NUMBER_OF_HUMAN_PLAYERS,
//                                                  invalidParameters);
//        }
//        
//        if (isNegativeNumber(computerizedPlayers)) {
//            rummikubFualt.setFaultCode(null);
//            rummikubFualt.setFaultString("amout of computer players is negative number");
//            invalidParameters.setFaultInfo(rummikubFualt);
//            invalidParameters.setMessage(Utils.Constants.ErrorMessages.NEGATIVE_NUMBER_OF_COMPUTER_PLAYERS);
//            
//            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.NEGATIVE_NUMBER_OF_COMPUTER_PLAYERS,
//                                                  invalidParameters);
//        }
//        
//        if (isOutOfBounderiesOfTheGamePlayerNumber(humanPlayers, computerizedPlayers)) {
//            rummikubFualt.setFaultCode(null);
//            rummikubFualt.setFaultString("amout of players is bigger then "+ Settings.MAX_NUMBER_OF_PLAYERS + "or smaller then" + Settings.MIN_NUMBER_OF_PLAYERS);
//            invalidParameters.setFaultInfo(rummikubFualt);
//            invalidParameters.setMessage(Utils.Constants.ErrorMessages.ILEGAL_TOTAL_PLAYER_NUMBER);
//            
//            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.ILEGAL_TOTAL_PLAYER_NUMBER,
//                                                  invalidParameters);
//        }
//    }
//
//    private void checkCaseOfEmptyStringOrNullOrContainsWhiteSpacesOfGameNotExsists(String gameName) throws GameDoesNotExists_Exception {
//        
//        if(isEmptyStringOrNullOrContainsStartingWhiteSpaces(gameName)) {
//            GameDoesNotExists gameDoesNotExsists = new GameDoesNotExists();
//            RummikubFault rummikubFualt = new RummikubFault();
//
//            rummikubFualt.setFaultCode(null);
//            rummikubFualt.setFaultString("game name is empty or null or contains starting white spaces");
//            gameDoesNotExsists.setFaultInfo(rummikubFualt);
//            gameDoesNotExsists.setMessage(Utils.Constants.ErrorMessages.STRING_IS_NULL_OR_EMPTY_OR_CONTAINS_STARTING_WHITE_SPACES);
//            throw new GameDoesNotExists_Exception(Utils.Constants.ErrorMessages.STRING_IS_NULL_OR_EMPTY_OR_CONTAINS_STARTING_WHITE_SPACES,
//                                                  gameDoesNotExsists);
//        }
//    }
//
//    private void checkCaseOfEmptyStringOrNullOrContainsWhiteSpacesOfInvalidParameters(String gameName) throws InvalidParameters_Exception {
//        
//        if(isEmptyStringOrNullOrContainsStartingWhiteSpaces(gameName)) {
//            InvalidParameters invalidParameters = new InvalidParameters();
//            RummikubFault rummikubFualt = new RummikubFault();
//
//            rummikubFualt.setFaultCode(null);
//            rummikubFualt.setFaultString("player name is empty or null or contains starting white spaces");
//            invalidParameters.setFaultInfo(rummikubFualt);
//            invalidParameters.setMessage(Utils.Constants.ErrorMessages.STRING_IS_NULL_OR_EMPTY_OR_CONTAINS_STARTING_WHITE_SPACES);
//            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.STRING_IS_NULL_OR_EMPTY_OR_CONTAINS_STARTING_WHITE_SPACES,
//                                                  invalidParameters);
//        }
//    }
//    
//    private boolean isOutOfBounderiesOfTheGamePlayerNumber(int humanPlayers, int computerizedPlayers) {
//        int totalNumberOfPlayers = humanPlayers + computerizedPlayers;
//        
//        return totalNumberOfPlayers > Settings.MAX_NUMBER_OF_PLAYERS || totalNumberOfPlayers < Settings.MIN_NUMBER_OF_PLAYERS;
//    }
//
//    private boolean isEmptyStringOrNullOrContainsStartingWhiteSpaces(String stringToCheck) {
//       return !(stringToCheck != null && !stringToCheck.isEmpty() && !Character.isWhitespace(stringToCheck.charAt(0)));
//    }
//    
//    private void initGameComponetsToPrepareForNextGame() {
//
//        this.rummikubLogic = null;
//        this.serieGenerator = new SeriesGenerator();
//        this.newMoveGenerator = new ComputerSingleMoveGenerator();
//
//        this.playerDetailes.clear();
//        this.gameStatus = GameStatus.WAITING;
//        this.isLoadedFromXML = !LOADED_FROM_XML;
//        this.timer = new Timer(DEAMON_THREAD);
//        this.eventManager.clearAllEvents();
//    }
//    
//    private void ImplementCompuerPlayerTurn(SingleMove singleMove) {
//        if (singleMove != null) {
//            try {
//                dealWithSingleMoveResualt(singleMove);
//            } catch (Exception ex) {
//                currentPlayerMove.setIsTurnSkipped(PlayersMove.USER_WANT_SKIP_TRUN);
//            }
//        }
//    }
    // </editor-fold>
    
    private boolean isNegativeNumber(int num) {
        return num < 0;
    }
    
    private void checkCaseOfIlegalTileListThatRepresentsSeries(List<ws.rummikub.Tile>  tiles) throws InvalidParameters_Exception {
        boolean foundProblem = tiles.isEmpty();
        final int MIN_VALUE_FOR_WS_TILE = Tile.MIN_TILE_VALUE - 1;
        if(!foundProblem) {
            foundProblem = tiles.size() > Tile.MAX_TILE_VALUE;
            for (Iterator<ws.rummikub.Tile> it = tiles.iterator(); !foundProblem && it.hasNext();) {
                ws.rummikub.Tile tile = it.next();
                
                foundProblem = !(tile.getColor() != null && tile.getValue() >= MIN_VALUE_FOR_WS_TILE && tile.getValue() <= Tile.MAX_TILE_VALUE); 
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

    private void cheackCaseTileLocationIndexesAreInvalid(int sequenceIndex, int sequencePosition) throws InvalidParameters_Exception {

        InvalidParameters invalidParameters = new InvalidParameters();
        RummikubFault rummikubFualt = new RummikubFault();

        checkCaseOfNegativeVals(sequenceIndex,sequencePosition);
        checkCaseOutOfBoundries(sequenceIndex,sequencePosition);

        //important check
        if (!this.rummikubLogic.getCurrentPlayer().isFirstMoveDone()) {
            int legalIndexToPutTiles = this.rummikubLogic.getGameBoard().isEmpty()? 0 : this.rummikubLogic.getGameBoard().boardSize();
            if (sequenceIndex < legalIndexToPutTiles ) {
                rummikubFualt.setFaultCode(null);
                rummikubFualt.setFaultString("canot touch tiles that not belong to the player before finishing the first turn");
                invalidParameters.setFaultInfo(rummikubFualt);
                invalidParameters.setMessage(Utils.Constants.ErrorMessages.ILEGAL_CANT_TUCH_BOARD_IN_FIRST_MOVE);
                throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.ILEGAL_CANT_TUCH_BOARD_IN_FIRST_MOVE,
                                                  invalidParameters);
            }
        }
    }

    private void cheackCaseTileIncreasingOrder(ws.rummikub.Tile tile, int sequenceIndex, int sequencePosition) throws InvalidParameters_Exception {
        Tile logicalTile = convertWsTileToLogicTile(tile);
        int IndexInHand = this.currentPlayerMove.getHandAfterMove().indexOf(logicalTile);
        Tile tileToMove = this.currentPlayerMove.getHandAfterMove().get(IndexInHand);

        boolean isValid = this.currentPlayerMove.getBoardAfterMove().getSeries(sequenceIndex).isLegalPlaceOfTile(tileToMove, sequencePosition);
        
        if (!isValid) {
            InvalidParameters invalidParameters = new InvalidParameters();
            RummikubFault rummikubFualt = new RummikubFault();
            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("The tile was placed not in increasing order");
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.ILEGAL_TILE_INSERTED_NOT_IN_RIGHT_ORDER);
            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.ILEGAL_TILE_INSERTED_NOT_IN_RIGHT_ORDER,
                                              invalidParameters);
        }
    }

    private void cheackCaseTileIncreasingOrder(int sourceSequenceIndex, int sourceSequencePosition, int targetSequenceIndex, int targetSequencePosition) throws InvalidParameters_Exception {
        Tile tileToMove = this.currentPlayerMove.getBoardAfterMove().getSpecificTile(sourceSequenceIndex, sourceSequencePosition);
        boolean isValid = this.currentPlayerMove.getBoardAfterMove().getSeries(targetSequenceIndex).isLegalPlaceOfTile(tileToMove, targetSequencePosition);
        
        if(!isValid) {
            InvalidParameters invalidParameters = new InvalidParameters();
            RummikubFault rummikubFualt = new RummikubFault();
            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("The tile was placed not in increasing order");
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.ILEGAL_TILE_INSERTED_NOT_IN_RIGHT_ORDER);
            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.ILEGAL_TILE_INSERTED_NOT_IN_RIGHT_ORDER,
                                              invalidParameters);
        }
    }

    private void checkCaseOfNegativeVals(int sequenceIndex, int sequencePosition) throws InvalidParameters_Exception {
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
        
    }

    private void checkCaseOutOfBoundries(int sequenceIndex, int sequencePosition) throws InvalidParameters_Exception {
                InvalidParameters invalidParameters = new InvalidParameters();
        RummikubFault rummikubFualt = new RummikubFault();
        
        if (sequenceIndex > this.currentPlayerMove.getBoardAfterMove().boardSize()) {
            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("such index of serie not exsists:" + String.valueOf(sequenceIndex));
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.ILEGAL_SEQUENCE_INDEX + String.valueOf(sequenceIndex));
            
            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.ILEGAL_SEQUENCE_INDEX  + String.valueOf(sequenceIndex),
                                                  invalidParameters);
        }

        if (sequencePosition > this.currentPlayerMove.getBoardAfterMove().getSeries(sequenceIndex).getSizeOfSerie()) {
            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("such tile position of tile not exsists:" + String.valueOf(sequencePosition));
            invalidParameters.setFaultInfo(rummikubFualt);
            invalidParameters.setMessage(Utils.Constants.ErrorMessages.ILEGAL_TILE_POSITION_INDEX + String.valueOf(sequencePosition));
            
            throw new InvalidParameters_Exception(Utils.Constants.ErrorMessages.ILEGAL_TILE_POSITION_INDEX + String.valueOf(sequencePosition),
                                                  invalidParameters);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="PlayerDetails-WS - methods">
    //************************ maybe not need  - START ************************//

    
    private void initPlayerDetailesList() {
        this.rummikubLogic.getPlayers().stream().forEach((currPlayer) -> {
            addPlayerToPlayerDetailesList(currPlayer);
        });
    }

    private void initPlayerDetailesListFromFile() {
        this.rummikubLogic.getPlayers().stream().forEach((currPlayer) -> {
            PlayerStatus playerStatus = currPlayer.getIsHuman()? null : PlayerStatus.JOINED;
            addPlayerToPlayerDetailesList(currPlayer, playerStatus, WITH_TILE_LIST);
        });
    } 

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
    
    private PlayerDetails createPlayerDetailes(Player currPlayer, PlayerStatus playerStatus, boolean withTilesList) {
        PlayerDetails playerDetails = new PlayerDetails();
        PlayerType playerType = currPlayer.getIsHuman()? PlayerType.HUMAN : PlayerType.COMPUTER;

        playerDetails.setName(currPlayer.getName());
        playerDetails.setNumberOfTiles(currPlayer.getListPlayerTiles().size());
        playerDetails.setPlayedFirstSequence(currPlayer.isFirstMoveDone());
        playerDetails.setStatus(playerStatus);
        playerDetails.setType(playerType);
        
        if(withTilesList) {
            initPlayerDetailsTileList(playerDetails, currPlayer.getListPlayerTiles());
        }
        
        return playerDetails;
    }
    
    private void initPlayerDetailsTileList(PlayerDetails playerDetails, ArrayList<Tile> logicTileList) {
        playerDetails.getTiles().clear();

        for (Tile currTile : logicTileList) {
            ws.rummikub.Tile jaxbTile = convertLogicTileToWsTile(currTile);
            playerDetails.getTiles().add(jaxbTile);
        }
        
        playerDetails.setNumberOfTiles( playerDetails.getTiles().size());
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
    
    private PlayerDetails copyPlayerDetails(Player player) {
        PlayerDetails playerDetails = new PlayerDetails();

        playerDetails.setName(player.getName());
        playerDetails.setNumberOfTiles(player.getListPlayerTiles().size());
        playerDetails.setPlayedFirstSequence(player.isFirstMoveDone());
        PlayerStatus playerStatus = this.playerDetailes.get(findPlayerId(player.getName())).getStatus();
        //if player didnt joind the his status is null
        playerDetails.setStatus(playerStatus);
        PlayerType playerType;
        if (player.getIsHuman()) {
            playerType = PlayerType.HUMAN;
        }
        else {
            playerType = PlayerType.COMPUTER;
        }
        playerDetails.setType(playerType);
        
        return playerDetails;
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
        }
        
        return playerDetails;
    }
    
    //************************ maybe not need  - END ************************//
    // </editor-fold>
    
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
        this.eventManager.addRevertEvent(playerId);

        PlayerDetails currentPlayerDetails = findPlayerDetails(playerId);
        initPlayerDetailsTileList(currentPlayerDetails, this.rummikubLogic.getCurrentPlayer().getListPlayerTiles());
        
        createEventsAcordingToTheCurrentLogicBoard(playerId);
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
            try {
                Thread.sleep(DELAY_FOR_COMPUTER_MOVE);
            } catch (InterruptedException ex) {}
            
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
        if (singleMove != IS_FINISHED_TURN) {
            int indexToAddNewSerieToBoard = this.currentPlayerMove.getBoardAfterMove().boardSize();

            if (indexToAddNewSerieToBoard == singleMove.getpTarget().getX()) {
                ArrayList<ws.rummikub.Tile> tileList = new ArrayList<>();
                tileList.add(convertLogicTileToWsTile(this.currentPlayerMove.getHandAfterMove().get(singleMove.getnSource())));
                try {
                    createSequence(playerId, tileList);
                } catch (InvalidParameters_Exception ex) {
                } catch (Exception ex) {
                    currentPlayerMove.setIsTurnSkipped(PlayersMove.USER_WANT_SKIP_TRUN);
                }
            } else {
                ws.rummikub.Tile jaxbTile = convertLogicTileToWsTile(this.currentPlayerMove.getHandAfterMove().get(singleMove.getnSource()));
                try {
                    addTile(playerId, jaxbTile, singleMove.getpTarget().x, singleMove.getpTarget().y);
                } catch (InvalidParameters_Exception ex) {
                } catch (Exception ex) {
                    currentPlayerMove.setIsTurnSkipped(PlayersMove.USER_WANT_SKIP_TRUN);
                }
            }
        }
    }
    
    private boolean isPositionAtStartOrEndOfSeries(int sequencePosition, final int START_OF_THE_SERIES, final int END_OF_THE_SERIES) {
        return sequencePosition == START_OF_THE_SERIES || sequencePosition == END_OF_THE_SERIES;
    }

    private void moveTileFromBoardToBoard(int playerId, int sourceSequenceIndex, int sourceSequencePosition,
                                          int targetSequenceIndex, int targetSequencePosition, 
                                          boolean needToAddEvent) throws InvalidParameters_Exception {
        
        Point source = new Point(sourceSequenceIndex, sourceSequencePosition);
        Point target = new Point(targetSequenceIndex, targetSequencePosition);

        SingleMove singleMove = new SingleMove(target, source, SingleMove.MoveType.BOARD_TO_BOARD);
        dealWithSingleMoveResualt(singleMove);
        
        if (needToAddEvent) {
            this.eventManager.addMoveTileEvent(playerId, sourceSequenceIndex, sourceSequencePosition,
                                               targetSequenceIndex, targetSequencePosition);
        }
    }

    private boolean allPlayersJoinedGame() {
        int currGameNumberOfHumanPlayersNeeded = this.rummikubLogic.getGameOriginalInputedSettings().getNumOfHumanPlayers();
        boolean isAllPlayersJoin = getNumberOfJoinedHumanPlayers() == currGameNumberOfHumanPlayersNeeded;

        if (isAllPlayersJoin) {
            for (PlayerDetails currPlayerDetails : this.playerDetailes.values()) {
                isAllPlayersJoin = isAllPlayersJoin && currPlayerDetails.getStatus() == PlayerStatus.JOINED;
            }
        }
        
        return isAllPlayersJoin;
    }

    private void createEventsAcordingToTheCurrentLogicBoard(int playerId) {
        ArrayList<Serie> lastTurnBoard = this.rummikubLogic.getGameBoard().getListOfSerie();
        ArrayList<ws.rummikub.Tile> jaxbTilesList = new ArrayList<>();
        
        for (Serie serie : lastTurnBoard) {
            for (Tile logicTile : serie.getSerieOfTiles()) {
                jaxbTilesList.add(convertLogicTileToWsTile(logicTile));
            }
            
            this.eventManager.addCreateSequenceEvent(playerId, jaxbTilesList);
            jaxbTilesList.clear();
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
        this.timer.cancel();
        this.rummikubLogic.removeCurrentPlayerFromTheGame();

        if (this.gameStatus == GameStatus.WAITING) {
            this.playerDetailes.remove(findPlayerId(this.rummikubLogic.getCurrentPlayer().getName()));
            this.rummikubLogic.getGameOriginalInputedSettings().removePlayerFromGame(this.rummikubLogic.getCurrentPlayer().getIsHuman());
        }
        else {
            revertTheTurn(playerId);
            this.eventManager.addResignEvent(playerId);

            findPlayerDetails(playerId).setStatus(PlayerStatus.RETIRED);

            if (this.rummikubLogic.isReachedOneOfEndGameConditions()) {
                onGameOverActions();
            }
            else {
                onSwapTurnActions();
            }  
        }

    }
    
    private void moveTileFromHandToBoard(int playerId, ws.rummikub.Tile tile, int sequenceIndex, int sequencePosition) throws InvalidParameters_Exception {

        Point target = new Point(sequenceIndex, sequencePosition);
        Tile logicalTile = convertWsTileToLogicTile(tile);

        int IndexInHand = this.currentPlayerMove.getHandAfterMove().indexOf(logicalTile);
        SingleMove singleMove = new SingleMove(target, IndexInHand, SingleMove.MoveType.HAND_TO_BOARD);

        if (dealWithSingleMoveResualt(singleMove)) {
            PlayerDetails playerDetails = findPlayerDetails(playerId);
            initPlayerDetailsTileList(playerDetails, this.currentPlayerMove.getHandAfterMove());
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
                    catch(Exception ex) {}
                }

                isComputerPlayer = !rummikubLogic.getCurrentPlayer().getIsHuman();
            }
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

    //Public functions
    public void initGameFromFile(ArrayList<Player> playerArray, Board board, Player currPlayer, String gameName) {
     
        this.rummikubLogic = new GameLogic();
        this.rummikubLogic.initGameFromFile(JaxBXmlParser.getPlayerArray(), JaxBXmlParser.getBoard(),
                                           JaxBXmlParser.getCurrPlayer(), JaxBXmlParser.getGameName());
        
        initPlayerDetailesListFromFile();
        initCurrentPlayerMove();
    }

    public void createNewGame(Settings gameSetting) {
        this.rummikubLogic = new GameLogic();
        this.isLoadedFromXML = !LOADED_FROM_XML;

        this.rummikubLogic.setGameSettings(gameSetting);
        
        this.rummikubLogic.setGameOriginalInputedSettings( new Settings(gameSetting));
        
        this.rummikubLogic.initGameViaWebRequest();
                
        //inits only computer players
        initPlayerDetailesList();
    }

    public String getGameName() {
        return this.rummikubLogic.getGameSettings().getGameName();
    }
    
    public List<PlayerDetails> makePlayerDetailsListWithoutTilesList() {
        List<PlayerDetails> newPlayerDetailsList = new ArrayList<>();
        if (this.isLoadedFromXML) {
            for (Player player : this.rummikubLogic.getPlayers()) {
                PlayerDetails newPlayerDetails = copyPlayerDetails(player);
                newPlayerDetailsList.add(newPlayerDetails);
            }
        } else {
            for (PlayerDetails playerDetails : this.playerDetailes.values()) {
                PlayerDetails newPlayerDetails = copyPlayerDetails(playerDetails, !WITH_TILE_LIST);
                newPlayerDetailsList.add(newPlayerDetails);
            }
        }

        return newPlayerDetailsList;
    }
    
    public PlayerDetails findPlayerDetails(int playerId) {
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

    public int addPlayerThatCreatedFromXML(String playerName) {
        Player newPlayer = this.rummikubLogic.getPlayerByName(playerName);
        PlayerId playerServerId = findPlayerId(newPlayer.getName());
        PlayerDetails playerDetails = this.playerDetailes.get(playerServerId); 
        playerDetails.setStatus(PlayerStatus.JOINED);
        
        return playerServerId.getPlayerId();
    }

    public int addPlayerForNewGame(String playerName) {
        Player newPlayer = new HumanPlayer(playerName);
        this.rummikubLogic.addNewHumanPlayer(newPlayer);
        
        return addPlayerToPlayerDetailesList(newPlayer, PlayerStatus.JOINED, WITH_TILE_LIST);
    }
    
    public void updateGameStatus() {

        if (allPlayersJoinedGame()) {
            this.gameStatus = GameStatus.ACTIVE;

            //walk throw details list and set status to active????
            this.playerDetailes.values().stream().forEach((currPlayerDetailes) -> { 
                currPlayerDetailes.setStatus(PlayerStatus.ACTIVE); 
            });

            if(!this.isLoadedFromXML) {
                this.rummikubLogic.shufflePlayersBeforeStartingOnlineGame();
            }
             
            initCurrentPlayerMove();
            
            this.eventManager.addGameStartEvent();
            this.eventManager.addPlayerTurnEvent(this.rummikubLogic.getCurrentPlayer().getName());
            int playerId = findPlayerId(this.rummikubLogic.getCurrentPlayer().getName()).getPlayerId();
            
            if (this.isLoadedFromXML) {
                createEventsAcordingToTheCurrentLogicBoard(NOT_RELATED_TO_ANY_PLAYER);
            }
            
            if (this.rummikubLogic.getCurrentPlayer().getIsHuman()) {
                setTimerForPlayerResponse(playerId);
            }
            else {
                onComputerTurn(playerId);
            }
        }
    }
    
    //Getters and Setters
    public Settings getGameSettings() {
        return this.rummikubLogic.getGameSettings();
    }

    public int getNumberOfJoinedHumanPlayers() {
        int numberOfJoinedHumanPlayers = 0;
        if (this.isLoadedFromXML) {
            for (PlayerDetails playerDetails : this.playerDetailes.values()) {
                if (playerDetails.getStatus() == PlayerStatus.JOINED && playerDetails.getType() == PlayerType.HUMAN) {
                    numberOfJoinedHumanPlayers++;
                }
            }
        }
        else {
            numberOfJoinedHumanPlayers = this.rummikubLogic.getNumberOfJoinedHumanPlayers();
        }
        
        return numberOfJoinedHumanPlayers;
    }

    public boolean isLoadedFromXml() {
       return this.isLoadedFromXML;
    }

    public GameStatus getGameStatus() {
        return this.gameStatus;
    }
    
    public GameLogic getRummikubLogic() {
        return this.rummikubLogic;
    }

    public PlayerDetails getPlayerDetailsByName(String playerName) {
        return this.playerDetailes.get(findPlayerId(playerName));
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

        public void addCreateSequenceEvent(int playerId, List<ws.rummikub.Tile> tiles) {
            Event newSequenceCtearedEvent = new Event();
            int index = currentPlayerMove.getBoardAfterMove().isEmpty()? 
                        0 : currentPlayerMove.getBoardAfterMove().boardSize();
            
            newSequenceCtearedEvent.setId(indexForNewtEvent());
            
            if(playerId == NOT_RELATED_TO_ANY_PLAYER) {
                newSequenceCtearedEvent.setPlayerName(CREATED_BY_FILE);
            }
            else {
                newSequenceCtearedEvent.setPlayerName(findPlayerDetails(playerId).getName());
            }
            newSequenceCtearedEvent.setTimeout((int)TIMER_DELAY);
            tiles.stream().forEach((tile) -> { newSequenceCtearedEvent.getTiles().add(tile); });
            //newSequenceCtearedEvent.setSourceSequenceIndex(/*MAYBE NOT NEED THAT SET*/0);
            //newSequenceCtearedEvent.setSourceSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            newSequenceCtearedEvent.setTargetSequenceIndex(index);
            //newSequenceCtearedEvent.setTargetSequencePosition(/*MAYBE NOT NEED THAT SET*/0);
            newSequenceCtearedEvent.setType(EventType.SEQUENCE_CREATED);
            gameEventList.add(newSequenceCtearedEvent);
        }
               
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
}

// <editor-fold defaultstate="collapsed" desc="sample of editor fold command">
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

///*
// * This class is the web service api.
// */
//
//package rummikub.ws;
//
//import javax.jws.WebService;
//import rummikub.implementation.RummikubSingleGameWsImp;
//import rummikub.implementation.RummikubWsImplementation;
//
//
//@WebService(serviceName = "RummikubWebServiceService", portName = "RummikubWebServicePort", endpointInterface = "ws.rummikub.RummikubWebService", targetNamespace = "http://rummikub.ws/", wsdlLocation = "WEB-INF/wsdl/RummikubWS/RummikubWebServiceService.wsdl")
//public class RummikubWS {
//
//    private final RummikubWsImplementation serverImplementation = new RummikubWsImplementation();
//    private RummikubSingleGameWsImp currentGame;
//
//    
//    public java.util.List<ws.rummikub.Event> getEvents(int playerId, int eventId) throws ws.rummikub.InvalidParameters_Exception {
//        currentGame = serverImplementation.getWantedGame(playerId);
//        
//        return currentGame.getEvents(playerId, eventId);
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
//        currentGame = serverImplementation.getWantedGame(playerId);
//        currentGame.createSequence(playerId, tiles);
//    }
//
//    public void addTile(int playerId, ws.rummikub.Tile tile, int sequenceIndex, int sequencePosition) throws ws.rummikub.InvalidParameters_Exception {
//        currentGame = serverImplementation.getWantedGame(playerId);
//        currentGame.addTile(playerId, tile, sequenceIndex, sequencePosition);
//    }
//
//    public void takeBackTile(int playerId, int sequenceIndex, int sequencePosition) throws ws.rummikub.InvalidParameters_Exception {
//        currentGame = serverImplementation.getWantedGame(playerId);
//        currentGame.takeBackTile(playerId, sequenceIndex, sequencePosition);
//    }
//
//    public void moveTile(int playerId, int sourceSequenceIndex, int sourceSequencePosition, int targetSequenceIndex, int targetSequencePosition) throws ws.rummikub.InvalidParameters_Exception {
//        currentGame = serverImplementation.getWantedGame(playerId);
//        currentGame.moveTile(playerId, sourceSequenceIndex, sourceSequencePosition, targetSequenceIndex, targetSequencePosition);
//    }
//
//    public void finishTurn(int playerId) throws ws.rummikub.InvalidParameters_Exception {
//        currentGame = serverImplementation.getWantedGame(playerId);
//        currentGame.finishTurn(playerId);
//    }
//
//    public void resign(int playerId) throws ws.rummikub.InvalidParameters_Exception {
//        currentGame = serverImplementation.getWantedGame(playerId);
//        currentGame.resign(playerId);
//    }
//}
//**************************** COPY OF WEB SERVISE FUNCS IMPLEMENTATION - END ****************************// 
// </editor-fold> 

