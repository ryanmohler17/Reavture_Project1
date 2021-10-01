package com.ryan.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.ryan.data.UserDataAccess;
import com.ryan.models.User;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LoginHandler implements Handler {

    private UserDataAccess userDataAccess;
    private Gson gson;
    private byte[] secret;
    public LoginHandler(UserDataAccess userDataAccess, Gson gson, byte[] secret) {
        this.userDataAccess = userDataAccess;
        this.gson = gson;
        this.secret = secret;
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {
        context.contentType("application/json");
        JsonObject jsonObject = JsonParser.parseString(context.body()).getAsJsonObject();
        String username = jsonObject.get("username").getAsString();
        String password = jsonObject.get("password").getAsString();
        User user = userDataAccess.getUserByName(username);
        JsonObject returnJson = new JsonObject();
        int status;
        if (user != null) {
            if (user.verifyPassword(password)) {
                returnJson.addProperty("logged_in", true);
                status = 200;
                JWSSigner signer = new MACSigner(secret);

                JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                        .issuer("ryan")
                        .jwtID(String.valueOf(user.getId()))
                        .subject("login")
                        .expirationTime(new Date(new Date().getTime() + TimeUnit.DAYS.toMillis(1)))
                        .build();

                SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claimsSet);
                signedJWT.sign(signer);

                String jwt = signedJWT.serialize();
                context.cookie("token", jwt);
            } else {
                returnJson.addProperty("logged_in", false);
                status = 401;
            }
        } else {
            returnJson.addProperty("logged_in", false);
            status = 401;
        }
        context.status(status);
        context.contentType("application/json");
        context.result(gson.toJson(returnJson));
    }

}
