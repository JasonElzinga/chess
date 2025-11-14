package client;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.CreateGameRequest;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import service.UserService;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() throws Exception {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        //facade = new ServerFacadeTests(port); //TODO
        String url = "http://localhost:" + port;
        facade = new ServerFacade(url);
        facade.clear();

    }



    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    void clear() throws Exception {
        var user = new UserData("joe", "j@j", "j");
        var res = facade.register(user);
        Assertions.assertNotNull(res);
        facade.clear();
        Assertions.assertDoesNotThrow(() -> facade.register(user));

    }


    @Test
    public void registerPositiveTest() throws Exception {
        var testData = new UserData("jason123", "123", "j@gmail");
        var res = facade.register(testData);

        Assertions.assertNotNull(res.authToken());
        Assertions.assertEquals(res.username(),testData.username());
    }

    @Test
    public void registerNegativeTest() throws Exception {
        var testData = new UserData("jason", null, "j@gmail");
        var testData2 = new UserData(null, "123", "j@gmail");
        var testData3 = new UserData("jason", "123", null);

        //var res = facade.register(testData);

        Assertions.assertThrows(Exception.class, ()-> facade.register(testData));
        Assertions.assertThrows(Exception.class, ()-> facade.register(testData2));
        Assertions.assertThrows(Exception.class, ()-> facade.register(testData3));
    }


    @Test
    public void loginSuccess() throws Exception {

        facade.register((new UserData("cow1", "rat1", "cow1@bob")));
        var res = facade.login(new UserData("cow1", "rat1", null));
        //System.out.println(res.username());
        Assertions.assertEquals("cow1", res.username());
        Assertions.assertNotNull(res.authToken());

    }

    @Test
    public void badLoginSuccess() {

        Assertions.assertThrows(Exception.class, ()->
                facade.login(new UserData("cow", "fake password", null)));
    }


    @Test
    public void badLogout() throws Exception {
        var testData = new UserData("jason1234", "epic", "j@gmail");
        facade.register(testData);
        Assertions.assertThrows(Exception.class, ()-> facade.logout("FakeAuthToken"));
    }

    @Test
    public void goodLogout() throws Exception {
        var testData = new UserData("jason1", "epicpassword", "j@gmail");

        var res = facade.register(testData);
        facade.logout(res.authToken());
    }


    @Test
    public void createGameSuccess() throws Exception {
        var testData = new UserData("jason", "epicpassword", "j@gmail");


        var res = facade.register(testData);
        var res1 = facade.createGame(new CreateGameRequest("epicGame"), res.authToken());


        Assertions.assertEquals(1, res1.gameID());
    }

    @Test
    public void createGameFail() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var userService = new UserService(dataAccess);

        var res = userService.register(new UserData("jason", "123", "jason@mail"));

        Assertions.assertThrows(DataAccessException.class, ()-> userService.createGame("Epic Game", "FakeAuthToken"));

        Assertions.assertThrows(DataAccessException.class, ()-> userService.createGame(null, res.authToken()));
    }

}
