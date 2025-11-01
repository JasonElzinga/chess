package service;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserService {
    private DataAccess dataAccess;
    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    public RegisterResponse register(UserData user) throws DataAccessException {
        try {
            if (user == null || user.username() == null || user.email() == null || user.password() == null) {
                throw new DataAccessException("Error: Bad Request");
            }
            if (dataAccess.getUser(user.username()) != null) {
                throw new DataAccessException("Error: Username already taken");
            }
            var hashPwd = BCrypt.hashpw(user.password(), BCrypt.gensalt());

            String authToken = generateToken();
            var auth = new AuthData(user.username(), authToken);
            dataAccess.storeAuth(auth);
            var updatedUser = new UserData(user.username(), hashPwd, user.email());
            dataAccess.createUser(updatedUser);

            return new RegisterResponse(user.username(), authToken);
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("Error: database failure");
        }
    }

    public LoginResponse login(UserData user) throws DataAccessException {
        try {
        if (user == null || user.username() == null || user.password() == null) {
            throw new DataAccessException("Error: Bad Request");
        }
//        var hashPwd = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        var actualUser = dataAccess.getUser(user.username());
        if (actualUser == null) {
            throw new DataAccessException("Error: Unauthorized");
        }

        boolean passwordMatches = BCrypt.checkpw(user.password(), actualUser.password());
        if (!passwordMatches) {
            throw new DataAccessException("Error: Unauthorized");
        }

        String authToken = generateToken();
        var auth = new AuthData(user.username(), authToken);
        dataAccess.storeAuth(auth);

        return new LoginResponse(actualUser.username(), authToken);
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("Error: database failure");
        }
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }


    public void logout(String authToken) throws DataAccessException {
        try {
            var authData = getAuthData(authToken);
            dataAccess.deleteAuthData(authData);
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("Error: database failure");
        }
    }

    public CreateGameResponse createGame(String gameName, String authToken) throws DataAccessException{
        try {
            if (gameName == null) {
                throw new DataAccessException("Error: Bad Request");
            }
            var authData = getAuthData(authToken);

            var createGameResponse = dataAccess.createGame(gameName);
            return createGameResponse;
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("Error: database failure");
        }
    }

    private AuthData getAuthData(String authToken) throws DataAccessException{
        try {
            if (authToken == null) {
                throw new DataAccessException("Error: Bad Request");
            }

            var authData = dataAccess.getAuthData(authToken);
            if (authData == null) {
                throw new DataAccessException("Error: unauthorized");
            }
            return authData;
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("Error: database failure");
        }
    }

    public void clear() throws DataAccessException, SQLException {
        try {
            dataAccess.clear();
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("Error: database failure");
        }
    }

    public ListGameResponse listGames(String authToken) throws DataAccessException{
        try {
            if (authToken == null) {
                throw new DataAccessException("Error: unauthorized");
            }
            getAuthData(authToken);
            var allGameData = dataAccess.listGames();
            List<IndividualGameData> summaries = new ArrayList<>();

            for (var g : allGameData) {
                summaries.add(new IndividualGameData(g.gameID(), g.whiteUsername(), g.blackUsername(), g.gameName()));
            }

            return new ListGameResponse(summaries);
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("Error: database failure");
        }
    }

    public void joinGame(String playerColor, Integer gameID, String authToken) throws DataAccessException {
        try {
            if (authToken == null) {
                throw new DataAccessException("Error: unauthorized");
            }
            if (playerColor == null || (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) || gameID == null) {
                throw new DataAccessException("Error: Bad Request");
            }

            var user = getAuthData(authToken);
            var game = dataAccess.getGame(gameID);
            if (game == null) {
                throw new DataAccessException("Error: Bad Request");
            }
            if ((playerColor.equals("WHITE") && game.whiteUsername() != null) ||
                    (playerColor.equals("BLACK") && game.blackUsername() != null)) {
                throw new DataAccessException("Error: already taken");
            }

            dataAccess.joinGame(user.username(), playerColor, gameID);
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("Error: database failure");
        }
    }
}
