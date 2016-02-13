/*
 * This class is responsible to implemenet the web service functionality with multiple games
 */

package rummikub.implementation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.xml.sax.SAXException;
import rummikub.gameLogic.model.logic.GameLogic;
import rummikub.gameLogic.model.logic.Settings;
import rummikub.gameLogic.model.player.HumanPlayer;
import rummikub.gameLogic.model.player.Player;
import rummikub.gameLogic.view.ioui.JaxBXmlParser;
import rummikub.gameLogic.view.ioui.Utils;
import ws.rummikub.*;

/**
 *
 * @author Arthur
 */
public class RummikubWsImplementation {
    
    private static final boolean LOADED_FROM_XML = true;
    private static final boolean DEAMON_THREAD = true;


    
    private final HashMap<String,RummikubSingleGameWsImp> gameListByGameName;
    private final HashMap<Integer,RummikubSingleGameWsImp> gameListByPlayerId;
    private RummikubSingleGameWsImp reffToWantedGame;
    
    public RummikubWsImplementation() {
        this.gameListByGameName = new HashMap<>();
        this.gameListByPlayerId = new HashMap<>();
        this.reffToWantedGame = null;
    }
    
    public void createGame(String gameName, int humanPlayers, int computerizedPlayers) throws DuplicateGameName_Exception,
                                                                                              InvalidParameters_Exception {
//        if (this.gameStatus == GameStatus.FINISHED) {
//            initGameComponetsToPrepareForNextGame();
//        }
        validateParamsAndThrowExceptionInIlegalCase(gameName, humanPlayers, computerizedPlayers);
        
        Settings gameSettings = new Settings(gameName, humanPlayers, computerizedPlayers);
        
        RummikubSingleGameWsImp newGame = new RummikubSingleGameWsImp();
        
        newGame.createNewGame(gameSettings);

        this.gameListByGameName.put(gameName.toLowerCase(), newGame);
    }
    
    public String createGameFromXML(String xmlData) throws DuplicateGameName_Exception, InvalidParameters_Exception, 
                                                           InvalidXML_Exception {
        RummikubSingleGameWsImp newGame;
        try {
//            if (this.gameStatus == GameStatus.FINISHED) {
//                initGameComponetsToPrepareForNextGame();
//            }            
            JaxBXmlParser.loadSettingsFromXml(xmlData);

            checkCaseOfDuplicateGameName(JaxBXmlParser.getGameName());
    
            //this.isLoadedFromXML = LOADED_FROM_XML;
            newGame = new RummikubSingleGameWsImp(LOADED_FROM_XML);

            newGame.initGameFromFile(JaxBXmlParser.getPlayerArray(), JaxBXmlParser.getBoard(),
                                           JaxBXmlParser.getCurrPlayer(), JaxBXmlParser.getGameName());
            
            this.gameListByGameName.put(newGame.getGameName(), newGame);
            
        } catch (SAXException | IOException ex) {
            InvalidXML invalidXML = new InvalidXML();
            RummikubFault rummikubFualt = new RummikubFault();

            rummikubFualt.setFaultCode(null);
            rummikubFualt.setFaultString("xml file error");
            invalidXML.setFaultInfo(rummikubFualt);
            invalidXML.setMessage(Utils.Constants.ErrorMessages.FAIL_LOADING_FILE_MSG);
            throw new InvalidXML_Exception(Utils.Constants.ErrorMessages.FAIL_LOADING_FILE_MSG, invalidXML);
        } 
        
        return newGame.getGameName();
    }
    
    public List<PlayerDetails> getPlayersDetails(String gameName) throws GameDoesNotExists_Exception {
        
        validateParamsAndThrowExceptionInIlegalCase(gameName);
        
        this.reffToWantedGame = this.gameListByGameName.get(gameName.toLowerCase());
        
        List<PlayerDetails> playerDetailsList = this.reffToWantedGame.makePlayerDetailsListWithoutTilesList();

        return playerDetailsList;
    }
    
    public GameDetails getGameDetails(String gameName) throws GameDoesNotExists_Exception {
        validateParamsAndThrowExceptionInIlegalCase(gameName);
        
        GameDetails currentGameDetals = new GameDetails();
        this.reffToWantedGame = this.gameListByGameName.get(gameName.toLowerCase());
        Settings currGameSetings = this.reffToWantedGame.getGameSettings();
        
        currentGameDetals.setComputerizedPlayers(currGameSetings.getNumOfCpuPlayers());
        currentGameDetals.setHumanPlayers(currGameSetings.getNumOfHumanPlayers());
        currentGameDetals.setJoinedHumanPlayers(this.reffToWantedGame.getNumberOfJoinedHumanPlayers());
        currentGameDetals.setLoadedFromXML(this.reffToWantedGame.isLoadedFromXml());
        currentGameDetals.setName(currGameSetings.getGameName());
        currentGameDetals.setStatus(this.reffToWantedGame.getGameStatus());
        
        return currentGameDetals;
    }

    // <editor-fold defaultstate="collapsed" desc="TODO - WHEN WE SUPPORT MULTIPLE GAMES - NOW RETURNS CURRENT GAME NAME IN THE LIST">
    //TODO:
    //when we will support multiple game we have to add other game's name's
    // </editor-fold>
    public List<String> getWaitingGames() {
        List<String> waitingGameList = new ArrayList<>();


//        String gameName;
//        if(this.rummikubLogic != null && this.gameStatus == GameStatus.WAITING ) {
//            gameName = this.rummikubLogic.getGameSettings().getGameName();
//            waitingGameList.add(gameName);
//        }
        
        for (String gameName : this.gameListByGameName.keySet()) {
            this.reffToWantedGame = this.gameListByGameName.get(gameName.toLowerCase());

            if(this.reffToWantedGame.getRummikubLogic() != null && this.reffToWantedGame.getGameStatus() == GameStatus.WAITING ) {
                waitingGameList.add(gameName);
            }
        }

        return waitingGameList;
    }

    public int joinGame(String gameName, String playerName) throws GameDoesNotExists_Exception, 
                                                                   InvalidParameters_Exception {
        validateParamsAndThrowExceptionInIlegalCase(gameName, playerName);
        int playerId;
        this.reffToWantedGame = this.gameListByGameName.get(gameName.toLowerCase());


        if (this.reffToWantedGame.isLoadedFromXml()){
            playerId = this.reffToWantedGame.addPlayerThatCreatedFromXML(playerName);
        }
        else {
            playerId = this.reffToWantedGame.addPlayerForNewGame(playerName);
        }

        this.gameListByPlayerId.put(playerId, this.reffToWantedGame);
        
        Thread thread = new Thread(()->{ this.reffToWantedGame.updateGameStatus(); });
        thread.setDaemon(DEAMON_THREAD);
        thread.start();
        
        return playerId;
    }

    public PlayerDetails getPlayerDetails(int playerId) throws GameDoesNotExists_Exception, 
                                                               InvalidParameters_Exception {
        
        validateParamsAndThrowExceptionGameNotExsistsOrInvalidParams(playerId);

        this.reffToWantedGame = this.gameListByPlayerId.get(playerId);
        
        return this.reffToWantedGame.findPlayerDetails(playerId);
    }
    
    public RummikubSingleGameWsImp getWantedGame(String gameName) {
        return this.gameListByGameName.get(gameName.toLowerCase());
    }
    
    public RummikubSingleGameWsImp getWantedGame(int playerId) {
        return this.gameListByPlayerId.get(playerId);
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
        checkCaseOfPlayerAlreadyExsists(gameName, playerName);
        checkCaseOfGameStatusIsNotWaiting(gameName);
    }
    
    private void validateParamsAndThrowExceptionGameNotExsistsOrInvalidParams(int playerId) throws GameDoesNotExists_Exception, 
                                                                                  InvalidParameters_Exception {
        checkCaseOfPlayerNotExsists(playerId);
        checkCaseOfGameDoesNotExists(playerId);
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

    private boolean isGameNameAlreadyExsists(String gameName) {
        boolean isGameAlreadyExsists;
        
        this.reffToWantedGame = this.gameListByGameName.get(gameName.toLowerCase());
        isGameAlreadyExsists = this.reffToWantedGame != null;
        
        return isGameAlreadyExsists;
    }

    private boolean isNegativeNumber(int num) {
        return num < 0;
    }

    private boolean isOutOfBounderiesOfTheGamePlayerNumber(int humanPlayers, int computerizedPlayers) {
        int totalNumberOfPlayers = humanPlayers + computerizedPlayers;
        
        return totalNumberOfPlayers > Settings.MAX_NUMBER_OF_PLAYERS || totalNumberOfPlayers < Settings.MIN_NUMBER_OF_PLAYERS;
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
    
    private boolean isEmptyStringOrNullOrContainsStartingWhiteSpaces(String stringToCheck) {
       return !(stringToCheck != null && !stringToCheck.isEmpty() && !Character.isWhitespace(stringToCheck.charAt(0)));
    }
    
    private void checkCaseOfPlayerNotExsists(int playerId) throws InvalidParameters_Exception {
        
        this.reffToWantedGame = this.gameListByPlayerId.get(playerId);
        PlayerDetails playerDetails = this.reffToWantedGame.findPlayerDetails(playerId);
        
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
        
        this.reffToWantedGame = this.gameListByGameName.get(gameName.toLowerCase());

        if (this.reffToWantedGame.getGameStatus() != GameStatus.WAITING) {
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

    private void checkCaseOfGameDoesNotExists(int playerId) throws GameDoesNotExists_Exception {
        
        this.reffToWantedGame = this.gameListByPlayerId.get(playerId);
        PlayerDetails playerDetails = this.reffToWantedGame.findPlayerDetails(playerId);
        
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

    private void checkCaseOfPlayerAlreadyExsists(String gameName, String playerName) throws InvalidParameters_Exception {
        
        checkCaseOfEmptyStringOrNullOrContainsWhiteSpacesOfInvalidParameters(playerName);
        this.reffToWantedGame = this.gameListByGameName.get(gameName.toLowerCase());

        if (this.reffToWantedGame.isLoadedFromXml()) {
            PlayerDetails playerDetails = this.reffToWantedGame.getPlayerDetailsByName(playerName);
            
            if (!this.reffToWantedGame.getGameSettings().isPlayerNameExists(playerName) || playerDetails.getStatus() == PlayerStatus.JOINED) {
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
            if (this.reffToWantedGame.getGameSettings().isPlayerNameExists(playerName)) {
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
}
