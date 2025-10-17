package dataaccess;

import model.model.UserData;

public interface DataAccess {
    void clear();
    void User(UserData user);
    void getUser(String username);
}
