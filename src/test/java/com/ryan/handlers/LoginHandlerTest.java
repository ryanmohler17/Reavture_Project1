package com.ryan.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.ryan.data.UserDataAccess;
import com.ryan.models.User;
import com.ryan.models.UserType;
import io.javalin.http.Context;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.text.ParseException;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class LoginHandlerTest {

    @Test
    public void testLoginHandlerSuccess() throws Exception {
        byte[] secret = new byte[64];
        new Random().nextBytes(secret);

        int userId = 1;
        String username = "user";
        String password = "1234";

        UserDataAccess userDataAccess = Mockito.mock(UserDataAccess.class);
        Mockito.when(userDataAccess.getUserByName(Mockito.anyString())).then(invocation -> {
            User user = new User(invocation.getArgument(0), null, "user@example.com", "User", "User", UserType.MANAGER);
            user.setPassword(password);
            user.setId(userId);
            return user;
        });

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Context context = Mockito.mock(Context.class);
        Mockito.when(context.body()).then(invocation -> {
            JsonObject object = new JsonObject();
            object.addProperty("username", username);
            object.addProperty("password", password);
            return gson.toJson(object);
        });

        Mockito.when(context.status(Mockito.anyInt())).then(invocation -> {
            assertEquals(200, (int)invocation.getArgument(0));
            return context;
        });

        Mockito.when(context.contentType(Mockito.anyString())).then(invocation -> {
            assertEquals("application/json", invocation.getArgument(0));
            return context;
        });

        AtomicReference<String> jwt = new AtomicReference<>();
        Mockito.when(context.cookie(Mockito.anyString(), Mockito.anyString())).then(invocation -> {
            assertEquals("token", invocation.getArgument(0));
            jwt.set(invocation.getArgument(1));
            return context;
        });

        Mockito.when(context.result(Mockito.anyString())).then(invocation -> {
            JsonObject jsonObject = JsonParser.parseString(invocation.getArgument(0)).getAsJsonObject();
            assertTrue(jsonObject.get("logged_in").getAsBoolean());
            return context;
        });

        LoginHandler loginHandler = new LoginHandler(userDataAccess, gson, secret);
        loginHandler.handle(context);

        SignedJWT signedJWT = SignedJWT.parse(jwt.get());
        JWSVerifier verifier = new MACVerifier(secret);

        assertTrue(signedJWT.verify(verifier));
        assertEquals(userId, Integer.parseInt(signedJWT.getJWTClaimsSet().getJWTID()));
        assertTrue(new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime()));
    }

}
