package service;

import dataaccess.MemoryDataAccess;
import model.model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {


    @Test
    void clear() {

    }


    @Test
    void register() throws Exception {
        var user = new UserData("joe", "j@", "j");
        var at = "xyz";

        var da = new MemoryDataAccess();
        var service = new UserService(da);
        var res = service.register(user);

        assertEquals(res.username(), user.username());
        assertNotNull(res.authToken());

    }
}