package service;
import dataaccess.DataAccess;
import model.UserData;

public class UserService {
    private DataAccess dataAccess;
    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public UserData register(UserData user) {
        dataAccess.saveUser(user.username());
        return new RegistrationResult(user.username(), "zyyz");
    }
}
