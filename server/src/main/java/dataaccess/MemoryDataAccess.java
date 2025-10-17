package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess{
    private HashMap<String, UserData> users = new HashMap<>();


    @Override
    public void getUser(String username) {
        users.get(username);
    }

    @Override
    public void saveUser(String username) {

    }

    @Override
    public void clear() {
        //TODO delete everything
    }
}
