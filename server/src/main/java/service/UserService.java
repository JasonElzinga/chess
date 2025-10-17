package service;
import dataaccess.DataAccess;
import model.RegistrationResult;
import model.model.UserData;

public class UserService {
    private DataAccess dataAccess;
    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }
    public RegistrationResult register(UserData user) {
        dataAccess.saveUser(user);
        return new RegistrationResult(user.username(), "zyyz");
    }
}
