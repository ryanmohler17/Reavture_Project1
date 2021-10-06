package com.ryan.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.ryan.data.RequestDataAccess;
import com.ryan.models.Request;
import com.ryan.models.RequestStatus;
import com.ryan.models.RequestsCounts;
import io.javalin.http.Context;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class GetRequestsHandlerTest {

    private String jwt;
    private Properties properties;
    private Gson gson;
    private int userId;
    private Context context;

    public void setupMocks() throws JOSEException {
        byte[] secret = new byte[64];
        new Random().nextBytes(secret);

        userId = 1;

        JWSSigner signer = new MACSigner(secret);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("ryan")
                .jwtID(String.valueOf(userId))
                .subject("login")
                .expirationTime(new Date(new Date().getTime() + TimeUnit.DAYS.toMillis(1)))
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claimsSet);
        signedJWT.sign(signer);

        jwt = signedJWT.serialize();

        properties = new Properties();
        properties.setProperty("user.secret", Base64.getEncoder().encodeToString(secret));

        gson = new GsonBuilder().setPrettyPrinting().create();

        context = Mockito.mock(Context.class);
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
    }

    @Test
    public void testCounts() throws Exception {
        setupMocks();
        int total = 3;
        int open = 2;
        int approved = 1;
        int denied = 0;

        RequestDataAccess requestDataAccess = Mockito.mock(RequestDataAccess.class);
        Mockito.when(requestDataAccess.getEmployeeRequestCounts(Mockito.anyInt())).then(invocation -> {
            assertEquals(userId, (int) invocation.getArgument(0));
            return new RequestsCounts(total, open, approved, denied);
        });

        Mockito.when(context.result(Mockito.anyString())).then(invocation -> {
            RequestsCounts counts = gson.fromJson((String) invocation.getArgument(0), RequestsCounts.class);
            assertEquals(total, counts.getTotal());
            assertEquals(open, counts.getOpen());
            assertEquals(approved, counts.getApproved());
            assertEquals(denied, counts.getDenied());
            return context;
        });

        GetRequestsHandler requestsHandler = new GetRequestsHandler(properties, requestDataAccess, gson);
        requestsHandler.handle(context);
    }

    @Test
    public void testLimitAndOffset() throws Exception {
        setupMocks();
        int limit = 10;
        int offset = 2;

        AtomicBoolean limitCalled = new AtomicBoolean(false);
        AtomicBoolean offsetCalled = new AtomicBoolean(false);
        Mockito.when(context.queryParam(Mockito.anyString())).then(invocation -> {
            String param = invocation.getArgument(0);
            if (param.equals("limit")) {
                limitCalled.set(true);
                return String.valueOf(limit);
            } else if (param.equals("offset")) {
                offsetCalled.set(true);
                return String.valueOf(offset);
            } else {
                fail("A param was asked for other then limit of offset");
                return null;
            }
        });

        RequestDataAccess requestDataAccess = Mockito.mock(RequestDataAccess.class);
        Mockito.when(requestDataAccess.getEmployeeRequests(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).then(invocation -> {
            assertEquals(userId, (int) invocation.getArgument(0));
            int limitMock = invocation.getArgument(2);
            int offsetMock = invocation.getArgument(1);
            assertEquals(limit, limitMock);
            assertEquals(offset, offsetMock);
            List<Request> requests = new ArrayList<>();
            for (int i = offsetMock; i < offsetMock + limitMock; i++) {
                Request request = new Request(userId, new Date(), RequestStatus.OPEN, new Date());
                request.setId(i);
                requests.add(request);
            }
            return requests;
        });

        Mockito.when(context.result(Mockito.anyString())).then(invocation -> {
            Type listType = new TypeToken<List<Request>>(){}.getType();
            List<Request> requests = gson.fromJson((String)invocation.getArgument(0), listType);
            assertEquals(limit, requests.size());
            assertEquals(offset, requests.get(0).getId());
            return context;
        });

        GetRequestsHandler requestsHandler = new GetRequestsHandler(properties, requestDataAccess, gson);
        requestsHandler.handle(context);
        assertTrue(limitCalled.get() && offsetCalled.get());
    }

    @Test
    public void testNotLoggedIn() throws Exception {
        byte[] secret = new byte[64];
        new Random().nextBytes(secret);

        RequestDataAccess requestDataAccess = Mockito.mock(RequestDataAccess.class);
        Context context = Mockito.mock(Context.class);
        Properties properties = new Properties();
        properties.setProperty("user.secret", Base64.getEncoder().encodeToString(secret));

        JWSSigner signer = new MACSigner(secret);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer("ryan")
                .jwtID(String.valueOf(userId))
                .subject("login")
                .expirationTime(new Date(new Date().getTime() - TimeUnit.DAYS.toMillis(1)))
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claimsSet);
        signedJWT.sign(signer);

        String jwt = signedJWT.serialize();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Mockito.when(context.cookie(Mockito.anyString())).then(invocation -> {
            assertEquals("token", invocation.getArgument(0));
            return jwt;
        });

        AtomicBoolean removedCookie = new AtomicBoolean(false);
        Mockito.when(context.removeCookie(Mockito.anyString())).then(invocation -> {
            removedCookie.set(true);
            assertEquals("token", invocation.getArgument(0));
            return context;
        });

        Mockito.when(context.status(Mockito.anyInt())).then(invocation -> {
            assertEquals(401, (int) invocation.getArgument(0));
            return context;
        });

        Mockito.when(context.contentType(Mockito.anyString())).then(invocation -> {
            assertEquals("application/json", invocation.getArgument(0));
            return context;
        });

        Mockito.when(context.result(Mockito.anyString())).then(invocation -> {
            JsonObject object = JsonParser.parseString(invocation.getArgument(0)).getAsJsonObject();
            assertTrue(object.has("error"));
            assertEquals("Not logged in", object.get("error").getAsString());
            return context;
        });

        GetRequestsHandler requestsHandler = new GetRequestsHandler(properties, requestDataAccess, gson);
        requestsHandler.handle(context);
        assertTrue(removedCookie.get());
    }


}
