package dataaccess;

import chess.ChessGame;
import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MemoryDataAccess implements DataAccess{
    private HashMap<String, UserData> users = new HashMap<>();
    private HashMap<String, AuthData> authData = new HashMap<>();
    private HashMap<Integer, GameData> gameData = new HashMap<>();

    private Integer nextGameID = 1234;

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public void updateGame(ChessGame game, Integer gameID) throws DataAccessException {
        //
    }

    @Override
    public void clear() {
        users.clear();
        nextGameID = 1234;
        gameData.clear();
        authData.clear();
    }

    @Override
    public void storeAuth(AuthData auth) {
        authData.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuthData(String authToken) {
        return authData.get(authToken);
    }

    @Override
    public void deleteAuthData(AuthData auth) {
        authData.remove(auth.authToken());
    }

    @Override
    public CreateGameResponse createGame(String gameName) {
        gameData.put(nextGameID, new GameData(nextGameID, null, null, gameName, new ChessGame()));
        nextGameID++;
        return new CreateGameResponse(nextGameID-1);
    }

    @Override
    public List<GameData> listGames() {
        return new ArrayList<>(gameData.values());
    }

    @Override
    public GameData getGame(Integer gameID) {
        return gameData.get(gameID);
    }

    @Override
    public void updateUserGame(ChessGame game, Integer gameID, ChessGame.TeamColor color) throws DataAccessException {
        //
    }

    @Override
    public void joinGame(String username, String playerColor, Integer gameID) {
        var currentGame = getGame(gameID);
        var replacedGame = new GameData(0, null, null, null, null);

        if (playerColor.equals("WHITE")) {
            replacedGame =
                    new GameData(gameID, username, currentGame.blackUsername(),
                    currentGame.gameName(), currentGame.game());
        }
        else {
            replacedGame =
                    new GameData(gameID, currentGame.whiteUsername(), username,
                            currentGame.gameName(), currentGame.game());
        }

        gameData.replace(gameID, replacedGame);
    }

}
