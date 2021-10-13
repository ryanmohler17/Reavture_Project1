package com.ryan.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.ryan.data.RequestDataAccess;
import com.ryan.data.UserDataAccess;
import com.ryan.models.Request;
import com.ryan.models.RequestStatus;
import com.ryan.models.User;
import com.ryan.models.UserType;
import io.javalin.http.Context;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class PostStatusHandlerTest {

    @Test
    public void testPostStatusHandler() throws Exception {
        byte[] secret = new byte[64];
        new Random().nextBytes(secret);

        int userId = 1;

        JWSSigner signer = new MACSigner(secret);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("ryan")
                .jwtID(String.valueOf(userId))
                .subject("login")
                .expirationTime(new Date(new Date().getTime() + TimeUnit.DAYS.toMillis(1)))
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claimsSet);
        signedJWT.sign(signer);

        String jwt = signedJWT.serialize();

        Properties properties = new Properties();
        properties.setProperty("user.secret", Base64.getEncoder().encodeToString(secret));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        UserDataAccess userDataAccess = Mockito.mock(UserDataAccess.class);
        Mockito.when(userDataAccess.getItem(Mockito.anyInt())).then(invocation -> {
            assertEquals(userId, (int) invocation.getArgument(0));
            User user = new User("test", "doesn't matter", "test@example.com", "Test", "User", UserType.MANAGER);
            user.setId(userId);
            return user;
        });

        int reqId = 1;
        RequestStatus changeTo = RequestStatus.APPROVED;

        RequestDataAccess requestDataAccess = Mockito.mock(RequestDataAccess.class);
        Mockito.when(requestDataAccess.getItem(Mockito.anyInt())).then(invocation -> {
            assertEquals(reqId, (int) invocation.getArgument(0));
            Request request = new Request(userId, new Date(), RequestStatus.OPEN, new Date());
            request.setId(reqId);
            return request;
        });

        Mockito.when(requestDataAccess.saveItem(Mockito.any())).then(invocation -> {
            Request request = invocation.getArgument(0);
            assertEquals(reqId, request.getId());
            assertEquals(userId, request.getResolvedBy());
            assertEquals(changeTo, request.getStatus());
            return reqId;
        });

        Context context = Mockito.mock(Context.class);
        Mockito.when(context.cookie(Mockito.anyString())).then(invocation -> {
            assertEquals("token", invocation.getArgument(0));
            return jwt;
        });
        Mockito.when(context.contentType(Mockito.anyString())).then(invocation -> {
            assertEquals("application/json", invocation.getArgument(0));
            return context;
        });
        Mockito.when(context.status(Mockito.anyInt())).then(invocation -> {
            assertEquals(200, (int) invocation.getArgument(0));
            return context;
        });

        Mockito.when(context.body()).then(invocation -> {
            JsonObject json = new JsonObject();
            json.addProperty("req", reqId);
            json.addProperty("status", changeTo.name());

            return gson.toJson(json);
        });

        Mockito.when(context.result(Mockito.anyString())).then(invocation -> {
            JsonObject json = JsonParser.parseString(invocation.getArgument(0)).getAsJsonObject();
            assertTrue(json.get("success").getAsBoolean());
            return context;
        });

        PostStatusHandler postStatusHandler = new PostStatusHandler(properties, requestDataAccess, userDataAccess, gson);
        postStatusHandler.handle(context);

    }

}
