package com.ryan.handlers;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.Test;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class GetRequestsHandlerTest {

    @Test
    public void testGetRequestsHandler() throws JOSEException {
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



    }

}
