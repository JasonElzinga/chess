package dataaccess;

import model.UserData;

public interface DataAccess {
    void clear();
    UserData getUser(String username);
    void createUser(UserData user);
}
