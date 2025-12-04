package dataaccess;

import chess.ChessGame;
import model.*;

import java.sql.SQLException;
import java.util.List;

public interface DataAccess {
    void clear() throws DataAccessException, SQLException;
    UserData getUser(String username) throws DataAccessException;
    void createUser(UserData user) throws DataAccessException;

    void updateGame(ChessGame game, Integer gameID) throws DataAccessException;

    void storeAuth(AuthData auth) throws DataAccessException;
    AuthData getAuthData(String authToken) throws DataAccessException;
    void deleteAuthData(AuthData auth) throws DataAccessException;
    CreateGameResponse createGame(String gameName) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    void joinGame(String username, String playerColor, Integer gameID) throws DataAccessException;
    GameData getGame(Integer gameID) throws DataAccessException;

    void updateUserGame(ChessGame game, Integer gameID, ChessGame.TeamColor color) throws DataAccessException;

}
