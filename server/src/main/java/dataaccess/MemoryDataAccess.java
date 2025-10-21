package dataaccess;

import model.AuthData;
import model.CreateGameResponse;
import model.GameData;
import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess{
    private HashMap<String, UserData> users = new HashMap<>();
    private HashMap<String, AuthData> authData = new HashMap<>();
    private HashMap<String, GameData> gameData = new HashMap<>();

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public void clear() {
        users.clear();
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


        return new CreateGameResponse();
    }

}
