/*
 * This class is the web service api.
 */

package rummikub.ws;

import javax.jws.WebService;
import rummikub.implementation.RummikubWsImplementation;


@WebService(serviceName = "RummikubWebServiceService", portName = "RummikubWebServicePort", endpointInterface = "ws.rummikub.RummikubWebService", targetNamespace = "http://rummikub.ws/", wsdlLocation = "WEB-INF/wsdl/RummikubWS/RummikubWebServiceService.wsdl")
public class RummikubWS {

    private final RummikubWsImplementation serverImplementation = new RummikubWsImplementation();
    
    public java.util.List<ws.rummikub.Event> getEvents(int playerId, int eventId) throws ws.rummikub.InvalidParameters_Exception {
        return serverImplementation.getEvents(playerId, eventId);
    }

    public java.lang.String createGameFromXML(java.lang.String xmlData) throws ws.rummikub.DuplicateGameName_Exception, ws.rummikub.InvalidParameters_Exception, ws.rummikub.InvalidXML_Exception {
        return serverImplementation.createGameFromXML(xmlData);
    }

    public java.util.List<ws.rummikub.PlayerDetails> getPlayersDetails(java.lang.String gameName) throws ws.rummikub.GameDoesNotExists_Exception {
        return serverImplementation.getPlayersDetails(gameName);
    }

    public void createGame(java.lang.String name, int humanPlayers, int computerizedPlayers) throws ws.rummikub.InvalidParameters_Exception, ws.rummikub.DuplicateGameName_Exception {
       serverImplementation.createGame(name, humanPlayers, computerizedPlayers);
    }

    public ws.rummikub.GameDetails getGameDetails(java.lang.String gameName) throws ws.rummikub.GameDoesNotExists_Exception {
        return serverImplementation.getGameDetails(gameName);
    }

    public java.util.List<java.lang.String> getWaitingGames() {
        return serverImplementation.getWaitingGames();
    }

    public int joinGame(java.lang.String gameName, java.lang.String playerName) throws ws.rummikub.GameDoesNotExists_Exception, ws.rummikub.InvalidParameters_Exception {
        return serverImplementation.joinGame(gameName, playerName);
    }

    public ws.rummikub.PlayerDetails getPlayerDetails(int playerId) throws ws.rummikub.GameDoesNotExists_Exception, ws.rummikub.InvalidParameters_Exception {
        return serverImplementation.getPlayerDetails(playerId);
    }

    public void createSequence(int playerId, java.util.List<ws.rummikub.Tile> tiles) throws ws.rummikub.InvalidParameters_Exception {
        serverImplementation.createSequence(playerId, tiles);
    }

    public void addTile(int playerId, ws.rummikub.Tile tile, int sequenceIndex, int sequencePosition) throws ws.rummikub.InvalidParameters_Exception {
        serverImplementation.addTile(playerId, tile, sequenceIndex, sequencePosition);
    }

    public void takeBackTile(int playerId, int sequenceIndex, int sequencePosition) throws ws.rummikub.InvalidParameters_Exception {
        serverImplementation.takeBackTile(playerId, sequenceIndex, sequencePosition);
    }

    public void moveTile(int playerId, int sourceSequenceIndex, int sourceSequencePosition, int targetSequenceIndex, int targetSequencePosition) throws ws.rummikub.InvalidParameters_Exception {
        serverImplementation.moveTile(playerId, sourceSequenceIndex, sourceSequencePosition, targetSequenceIndex, targetSequencePosition);
    }

    public void finishTurn(int playerId) throws ws.rummikub.InvalidParameters_Exception {
        serverImplementation.finishTurn(playerId);
    }

    public void resign(int playerId) throws ws.rummikub.InvalidParameters_Exception {
        serverImplementation.resign(playerId);
    }
}
