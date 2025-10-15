package service;
import dataaccess.DataAccess;
import datamodel.RegistrationResult;
import datamodel.UserData;

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
