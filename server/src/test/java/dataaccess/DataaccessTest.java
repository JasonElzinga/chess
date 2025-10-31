package dataaccess;

import model.IndividualGameData;
import model.ListGameResponse;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DataaccessTest {

    @Test
    void clear() throws DataAccessException{
        var user = new UserData("joe", "j@j", "j");
        DataAccess da = new MemoryDataAccess();
        da.createUser(user);
        assertNotNull(da.getUser(user.username()));
        da.clear();
        assertNull(da.getUser(user.username()));
    }


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


        Assertions.assertThrows(DataAccessException.class, ()->
                userService.register(new UserData("cow", "rat", "john@1")));
    }

    @Test
    public void loginSuccess() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        userService.register((new UserData("cow1", "rat1", "cow1@bob")));
        var res = userService.login(new UserData("cow1", "rat1", null));
        //System.out.println(res.username());
        Assertions.assertEquals("cow1", res.username());
        Assertions.assertNotNull(res.authToken());

    }

    @Test
    public void badLoginSuccess() {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        Assertions.assertThrows(DataAccessException.class, ()->
                userService.login(new UserData("cow", "fake password", null)));
    }

    @Test
    public void badLogout() {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        Assertions.assertThrows(DataAccessException.class, ()-> userService.logout("FakeAuthToken"));
    }

    @Test
    public void goodLogout() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        var res = userService.register(new UserData("bob", "123", "bob@mail"));
        userService.logout(res.authToken());
    }

    @Test
    public void createGameSuccess() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        var res = userService.register(new UserData("jason", "123", "jason@mail"));
        var res1 = userService.createGame("Epic Game", res.authToken());


        Assertions.assertEquals(1234, res1.gameID());
    }

    @Test
    public void createGameFail() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        var res = userService.register(new UserData("jason", "123", "jason@mail"));

        Assertions.assertThrows(DataAccessException.class, ()-> userService.createGame("Epic Game", "FakeAuthToken"));

        Assertions.assertThrows(DataAccessException.class, ()-> userService.createGame(null, res.authToken()));
    }

    @Test
    public void listGameSuccess() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        var res = userService.register(new UserData("bob", "123", "jason@mail"));
        userService.createGame("Epic Game", res.authToken());
        var res2 = userService.listGames(res.authToken());

        var expectedGame = new ListGameResponse(List.of(new IndividualGameData(1234, null, null, "Epic Game")));
        Assertions.assertNotNull(res2);
        Assertions.assertEquals(expectedGame, res2);
    }

    @Test
    public void listGameFail() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        var res = userService.register(new UserData("bob", "123", "jason@mail"));
        userService.createGame("Epic Game", res.authToken());


        Assertions.assertThrows(DataAccessException.class, ()-> userService.listGames(null));

    }

    @Test
    public void joinGameSuccess() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        var res = userService.register(new UserData("bob", "123", "jason@mail"));
        var res1 = userService.register(new UserData("bob2", "123", "bob2@mail"));
        userService.createGame("Epic Game", res.authToken());
        userService.joinGame("WHITE", 1234, res.authToken());
        userService.joinGame("BLACK",1234, res1.authToken());

        var res2 = userService.listGames(res.authToken());
        var expectedGame = new ListGameResponse(List.of
                (new IndividualGameData(1234, "bob", "bob2", "Epic Game")));

        Assertions.assertNotNull(res2);
        Assertions.assertEquals(expectedGame, res2);
    }

    @Test
    public void joinGameFail() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        var res = userService.register(new UserData("bob", "123", "jason@mail"));
        userService.createGame("Epic Game", res.authToken());


        Assertions.assertThrows(DataAccessException.class, ()-> {
            userService.joinGame(null, 1234, res.authToken());
            userService.joinGame("WHITE", 1234, res.authToken());
            userService.joinGame("WHITE", 1234, "Fake authToken");
        });
    }



}
