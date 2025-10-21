package servicetests;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.UserService;

public class ServiceTest {

    @Test
    public void registerNormal() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        var res = userService.register(new UserData("cow", "rat", "john@1"));

        Assertions.assertNotEquals(null, res.username());
        Assertions.assertNotEquals(null, res.authToken());
    }

    @Test
    public void registerNotNull() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        var res = userService.register(new UserData("cow", "rat", "john@1"));

        Assertions.assertNotNull(res);
        Assertions.assertNotEquals(null, res.username());
        Assertions.assertNotEquals(null, res.authToken());
    }

    @Test
    public void registerSameUsername() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);


        var res = userService.register(new UserData("cow", "rat", "john@1"));
        Assertions.assertEquals("cow", res.username());
        Assertions.assertNotNull(res.authToken());


        Assertions.assertThrows(DataAccessException.class, ()-> {
            userService.register(new UserData("cow", "rat", "john@1"));
        });
    }

    @Test
    public void loginSuccess() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        userService.register((new UserData("cow1", "rat1", "cow1@bob")));
        var res = userService.login(new UserData("cow1", "rat1", null));
        System.out.println(res.username());
        Assertions.assertEquals("cow1", res.username());
        Assertions.assertNotNull(res.authToken());

    }

    @Test
    public void badLoginSuccess() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        Assertions.assertThrows(DataAccessException.class, ()-> {
            userService.login(new UserData("cow", "fakepassword", null));
        });
    }

    @Test
    public void badLogout() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        Assertions.assertThrows(DataAccessException.class, ()-> {
            userService.logout("FakeAuthToken");
        });
    }

    @Test
    public void goodLogout() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        var res = userService.register(new UserData("bob", "123", "bob@mail"));
        userService.logout(res.authToken());
    }


}
