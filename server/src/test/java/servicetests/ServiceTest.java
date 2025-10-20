package servicetests;

import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.UserService;

public class ServiceTest {

    @Test
    public void registerNormal() {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        var res = userService.register(new UserData("cow", "rat", "john"));

        Assertions.assertNotEquals(null, res.username());
        Assertions.assertNotEquals(null, res.authToken());
    }

    @Test
    public void registerNotNull() {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        var res = userService.register(new UserData("cow", "rat", "john"));

        Assertions.assertNotNull(res);
        Assertions.assertNotEquals(null, res.username());
        Assertions.assertNotEquals(null, res.authToken());
    }


}
