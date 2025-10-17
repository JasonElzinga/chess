package dataaccess;

import model.UserData;

public interface DataAccess {
    void clear();
    void getUser(String username);
    void saveUser(String username);
}
