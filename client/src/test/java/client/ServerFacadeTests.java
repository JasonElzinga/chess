package client;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import service.UserService;

import java.util.List;



public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() throws Exception {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        String url = "http://localhost:" + port;
        facade = new ServerFacade(url);
        facade.clear();

    }

    @BeforeEach
    void reset() throws Exception {
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
    public void registerNegativeTest() {
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
        var testData = new UserData("jason1", "epicPassword", "j@gmail");

        var res = facade.register(testData);
        facade.logout(res.authToken());
    }


    @Test
    public void createGameSuccess() throws Exception {
        var testData = new UserData("jason", "epicPassword", "j@gmail");


        var res = facade.register(testData);
        var res1 = facade.createGame(new CreateGameRequest("epicGame"), res.authToken());


        Assertions.assertEquals(1, res1.gameID());
    }

    @Test
    public void createGameFail() throws Exception {
        var res = facade.register(new UserData("jason", "123", "jason@mail"));
        Assertions.assertThrows(Exception.class, ()-> facade.createGame(new CreateGameRequest("epicGame"), "FakeAuthToken"));
        Assertions.assertThrows(Exception.class, ()-> facade.createGame(null, res.authToken()));
    }


    @Test
    public void listGameSuccess() throws Exception {
        var user = new UserData("bob2323", "123", "jason@mail");

        var res = facade.register(user);
        facade.createGame(new CreateGameRequest("bob2323"), res.authToken());
        facade.createGame(new CreateGameRequest("epic"), res.authToken());
        facade.createGame(new CreateGameRequest("34"), res.authToken());

        var listGames = facade.listGames(res.authToken());

        var expectedGame = new ListGameResponse(List.of(new IndividualGameData(1, null, null, "bob2323"),
                new IndividualGameData(2, null, null, "epic"),
                new IndividualGameData(3, null, null, "34")));
        Assertions.assertEquals(expectedGame,listGames);
    }

    @Test
    public void listGameFail() throws Exception {
        var user = new UserData("bob123", "123", "jason@mail");

        var res = facade.register(user);
        facade.createGame(new CreateGameRequest("EpicGame"), res.authToken());
        facade.createGame(new CreateGameRequest("SillyGame"), res.authToken());
        facade.createGame(new CreateGameRequest("EpicGame24"), res.authToken());
        facade.createGame(new CreateGameRequest("WhatGame"), res.authToken());


        Assertions.assertThrows(Exception.class, ()-> facade.listGames(null));
    }

    @Test
    public void joinGameSuccess() throws Exception {
        var user = new UserData("bob123", "123", "jason@mail");
        var user2 = new UserData("bob2", "123", "jason@mail");


        var res = facade.register(user);
        var res1 = facade.register(user2);
        facade.createGame(new CreateGameRequest("epicGame"), res.authToken());
        facade.joinGame(new JoinGameRequest("WHITE", 1),  res.authToken());
        facade.joinGame(new JoinGameRequest("BLACK", 1),  res1.authToken());


        var res2 = facade.listGames(res.authToken());
        var expectedGame = new ListGameResponse(List.of
                (new IndividualGameData(1, "bob123", "bob2", "epicGame")));

        Assertions.assertNotNull(res2);
        Assertions.assertEquals(expectedGame, res2);
    }

    @Test
    public void joinGameFail() throws DataAccessException {
        var dataAccess = new MemoryDataAccess();
        var facade = new UserService(dataAccess);

        var res = facade.register(new UserData("bob123", "123", "jason@mail"));
        facade.createGame("Epic Game", res.authToken());

        Assertions.assertThrows(DataAccessException.class, ()-> {
            facade.joinGame(null, 1234, res.authToken());
            facade.joinGame("WHITE", 1234, res.authToken());
            facade.joinGame("WHITE", 1234, "Fake authToken");
        });
    }
}
