package service;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserService {
    private DataAccess dataAccess;
    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    public RegisterResponse register(UserData user) throws DataAccessException {
        if (user == null || user.username() == null || user.email() == null || user.password() == null) {
            throw new DataAccessException("Error: Bad Request");
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw new DataAccessException("Error: Username already taken");
        }

        String authToken = generateToken();
        var auth = new AuthData(user.username(), authToken);
        dataAccess.storeAuth(auth);

        dataAccess.createUser(user);
        return new RegisterResponse(user.username(), authToken);
    }

    public LoginResponse login(UserData user) throws DataAccessException {
        if (user == null || user.username() == null || user.password() == null) {
            throw new DataAccessException("Error: Bad Request");
        }
//        if (dataAccess.getUser(user.username()) != null) {
//            throw new DataAccessException("Error: Username already taken");
//      }

        var actualUser = dataAccess.getUser(user.username());
        if (actualUser == null || (!actualUser.password().equals(user.password()))) {
            throw new DataAccessException("Error: Unauthorized");
        }

        String authToken = generateToken();
        var auth = new AuthData(user.username(), authToken);
        dataAccess.storeAuth(auth);

        return new LoginResponse(actualUser.username(), authToken);
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }


    public void logout(String authToken) throws DataAccessException {
        var authData = getAuthData(authToken);
        dataAccess.deleteAuthData(authData);
    }

    public CreateGameResponse createGame(String gameName, String authToken) throws DataAccessException{
        if (gameName == null) {
            throw new DataAccessException("Error: Bad Request");
        }
        var authData = getAuthData(authToken);

        var createGameResponse = dataAccess.createGame(gameName);
        return createGameResponse;
    }

    private AuthData getAuthData(String authToken) throws DataAccessException{
        if (authToken == null) {
            throw new DataAccessException("Error: Bad Request");
        }

        var authData = dataAccess.getAuthData(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return authData;
    }

    public void clear() throws DataAccessException{
        dataAccess.clear();
    }

    public ListGameResponse listGames(String authToken) throws DataAccessException{
        if (authToken == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        getAuthData(authToken);
        var allGameData = dataAccess.listGames();
        List<IndividualGameData> summaries = new ArrayList<>();

        for (var g: allGameData) {
            summaries.add(new IndividualGameData(g.gameID(), g.whiteUsername(), g.blackUsername(), g.gameName()));
        }

        return new ListGameResponse(summaries);
    }

    public void joinGame(String playerColor, Integer gameID, String authToken) throws DataAccessException{
        if (authToken == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        if (playerColor == null || (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) || gameID == null) {
            throw new DataAccessException("Error: Bad request");
        }

        var user = getAuthData(authToken);
        var game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: Bad request");
        }
        if ((playerColor.equals("WHITE") && game.whiteUsername() != null) ||
                (playerColor.equals("BLACK") && game.blackUsername() != null)) {
            throw new DataAccessException("Error: already taken");
        }
        dataAccess.joinGame(user.username(), playerColor, gameID);
    }
}
