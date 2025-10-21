package service;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.LoginResponse;
import model.RegisterResponse;
import model.UserData;
import java.util.UUID;

public class UserService {
    private DataAccess dataAccess;
    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    public RegisterResponse register(UserData user) throws DataAccessException {
        if (user == null || user.username() == null || user.email() == null || user.password() == null) {
            throw new DataAccessException("Error: Bad Request");
        }
        if (dataAccess.getUser(user.username()) != null) {
            throw new DataAccessException("Error: Username already taken");
        }

        String authToken = generateToken();
        var auth = new AuthData(user.username(), authToken);
        dataAccess.storeAuth(auth);

        dataAccess.createUser(user);
        return new RegisterResponse(user.username(), authToken);
    }

    public LoginResponse login(UserData user) throws DataAccessException {
        if (user == null || user.username() == null || user.password() == null) {
            throw new DataAccessException("Error: Bad Request");
        }
//        if (dataAccess.getUser(user.username()) != null) {
//            throw new DataAccessException("Error: Username already taken");
//      }

        var actualUser = dataAccess.getUser(user.username());
        if (actualUser == null || (!actualUser.password().equals(user.password()))) {
            throw new DataAccessException("Error: Unauthorized");
        }

        String authToken = generateToken();
        var auth = new AuthData(user.username(), authToken);
        dataAccess.storeAuth(auth);

        return new LoginResponse(actualUser.username(), authToken);
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }


    public void logout(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        var authData = dataAccess.getAuthData(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        dataAccess.deleteAuthData(authData);
    }


    public String getUser(UserData users) {
        return users.username();
    }

    public void clear() throws DataAccessException{
        dataAccess.clear();
    }
}
