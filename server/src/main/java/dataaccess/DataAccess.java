package dataaccess;

import model.*;

import java.sql.SQLException;
import java.util.List;

public interface DataAccess {
    void clear() throws DataAccessException, SQLException;
    UserData getUser(String username) throws DataAccessException;
    void createUser(UserData user) throws DataAccessException;
    void storeAuth(AuthData auth) throws DataAccessException;
    AuthData getAuthData(String authToken);
    void deleteAuthData(AuthData auth) throws DataAccessException;
    CreateGameResponse createGame(String gameName) throws DataAccessException;
    List<GameData> listGames();
    void joinGame(String username, String playerColor, Integer gameID);
    GameData getGame(Integer gameID);
}
