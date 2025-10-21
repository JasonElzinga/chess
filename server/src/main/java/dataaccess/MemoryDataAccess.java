package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess{
    private HashMap<String, UserData> users = new HashMap<>();
    private HashMap<String, AuthData> authData = new HashMap<>();


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
        authData.put(auth.username(), auth);
    }

}
