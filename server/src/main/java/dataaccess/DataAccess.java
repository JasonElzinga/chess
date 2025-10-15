package dataaccess;

import datamodel.UserData;

public interface DataAccess {
    void clear();
    void User(UserData user);
    void getUser(String username);
}
