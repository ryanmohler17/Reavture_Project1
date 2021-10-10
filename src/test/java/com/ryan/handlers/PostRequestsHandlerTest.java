package com.ryan.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.ryan.data.RequestDataAccess;
import com.ryan.data.UserDataAccess;
import com.ryan.models.Request;
import io.javalin.http.Context;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PostRequestsHandlerTest {

    @Test
    public void testPostRequestsHandler() throws Exception {
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

        AtomicReference<Request> req = new AtomicReference<>();
        RequestDataAccess requestDataAccess = Mockito.mock(RequestDataAccess.class);
        Mockito.when(requestDataAccess.saveItem(Mockito.any())).then(invocation -> {
            req.set(invocation.getArgument(0));
            return 1;
        });

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonArray array = new JsonArray();
        for (int i = 0; i < 50; i++) {
            JsonObject object = new JsonObject();
            object.addProperty("amount", 10.1);
            object.addProperty("description", "Test " + i);
            object.addProperty("type", "other");
            JsonArray images = new JsonArray();
            images.add("data:image/png;base64,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");
            object.add("images", images);
            array.add(object);
        }

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

        Mockito.when(context.body()).then(invocation -> gson.toJson(array));
        Mockito.when(context.result(Mockito.anyString())).then(invocation -> {
            JsonObject object = JsonParser.parseString(invocation.getArgument(0)).getAsJsonObject();
            assertTrue(object.get("success").getAsBoolean());
            return context;
        });

        PostRequestsHandler postRequestsHandler = new PostRequestsHandler(properties, requestDataAccess, gson);
        postRequestsHandler.handle(context);

        assertEquals(50, req.get().getParts().size());
    }

}
