package service;
import dataaccess.DataAccess;
import model.RegisterResponse;
import model.UserData;

public class UserService {
    private DataAccess dataAccess;
    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    public RegisterResponse register(UserData user) {
        dataAccess.createUser(user);
        return new RegisterResponse(user.username(), "zyyz");
    }


}
