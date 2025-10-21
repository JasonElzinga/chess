package dataaccess;

import model.*;

import java.util.List;

public interface DataAccess {
    void clear();
    UserData getUser(String username);
    void createUser(UserData user);
    void storeAuth(AuthData auth);
    AuthData getAuthData(String authToken);
    void deleteAuthData(AuthData auth);
    CreateGameResponse createGame(String gameName);
    List<GameData> listGames();
    void joinGame(String username, String playerColor, Integer gameID);
    GameData getGame(Integer gameID);
}
