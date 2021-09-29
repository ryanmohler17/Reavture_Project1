package com.ryan.models;

import static org.junit.Assert.*;
import org.junit.Test;

public class UserModelTest {

    @Test
    public void testPassword() {
        User user = new User();
        String pass = "1234";
        user.setPassword(pass);

        assertTrue(user.verifyPassword(pass));
        assertFalse(user.verifyPassword("hi"));
    }

}
