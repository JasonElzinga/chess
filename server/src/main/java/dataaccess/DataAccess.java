package dataaccess;

import model.AuthData;
import model.UserData;

public interface DataAccess {
    void clear();
    UserData getUser(String username);
    void createUser(UserData user);
    void storeAuth(AuthData auth);
    AuthData getAuthData(String authToken);
    void deleteAuthData(AuthData auth);
}
