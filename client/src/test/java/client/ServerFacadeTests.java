package client;

import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;


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
    public void registerPositiveTest() throws Exception {
        var testData = new UserData("jason", "123", "j@gmail");
        var res = facade.register(testData);

        Assertions.assertNotNull(res.authToken());
        Assertions.assertEquals(res.username(),testData.username());
    }



}
