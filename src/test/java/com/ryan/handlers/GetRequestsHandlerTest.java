package com.ryan.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.ryan.data.RequestDataAccess;
import com.ryan.data.UserDataAccess;
import com.ryan.models.RequestsCounts;
import io.javalin.http.Context;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class GetRequestsHandlerTest {

    @Test
    public void testCounts() throws Exception {
        byte[] secret = new byte[64];
        new Random().nextBytes(secret);

        int userId = 1;

        int total = 3;
        int open = 2;
        int approved = 1;
        int denied = 0;

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

        RequestDataAccess requestDataAccess = Mockito.mock(RequestDataAccess.class);
        Mockito.when(requestDataAccess.getEmployeeRequestCounts(Mockito.anyInt())).then(invocation -> {
            assertEquals(userId, (int) invocation.getArgument(0));
            return new RequestsCounts(total, open, approved, denied);
        });

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Context context = Mockito.mock(Context.class);
        Mockito.when(context.cookie(Mockito.anyString())).then(invocation -> {
            assertEquals("token", invocation.getArgument(0));
            return jwt;
        });
        Mockito.when(context.contentType(Mockito.anyString())).then(invocation -> {
            assertEquals("application/json", invocation.getArgument(0));
            return context;
        });

        GetRequestsHandler requestsHandler = new GetRequestsHandler(properties, requestDataAccess, gson);
        requestsHandler.handle(context);
    }

}
