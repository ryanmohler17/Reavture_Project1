package com.ryan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.ryan.data.DataConnector;
import com.ryan.data.SqlUserAccess;
import com.ryan.data.UserDataAccess;
import com.ryan.models.User;
import io.javalin.Javalin;
import io.javalin.http.JavalinServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MainServlet extends HttpServlet {

    private JavalinServlet javalinServlet;
    private DataConnector connector;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        javalinServlet.service(req, resp);
    }

    @Override
    public void destroy() {
        javalinServlet.destroy();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        Properties properties = new Properties();
        try {
            String path = config.getServletContext().getRealPath("WEB-INF/db.properties");
            properties.load(new FileInputStream(new File(path)));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        connector = new DataConnector(properties);
        UserDataAccess userDataAccess = new SqlUserAccess(connector);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        javalinServlet = Javalin.createStandalone()
                .get("/", context -> {
                    String token = context.cookie("token");
                    if (token == null) {
                        context.redirect("login");
                        return;
                    }

                    byte[] secret = Base64.getDecoder().decode(properties.getProperty("user.secret"));
                    SignedJWT signedJWT = SignedJWT.parse(token);
                    JWSVerifier verifier = new MACVerifier(secret);

                    if (!signedJWT.verify(verifier) || !new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime())) {
                        context.removeCookie("token");
                        context.redirect("login");
                        return;
                    }

                    User user = userDataAccess.getItem(Integer.parseInt(signedJWT.getJWTClaimsSet().getJWTID()));
                    JsonObject json = new JsonObject();
                    json.addProperty("login", true);
                    json.addProperty("username", user.getUserName());
                    context.contentType("application/json");
                    context.result(gson.toJson(json));
                })
                .get("/login", context -> {
                    String path = getServletContext().getRealPath("login.html");
                    String login = String.join("\n", Files.readAllLines(Paths.get(path)));
                    context.html(login);
                })
                .post("/login", context -> {
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
                            byte[] secret = Base64.getDecoder().decode(properties.getProperty("user.secret"));
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
                })
                .get("/public/{item}", context -> {
                    String path = getServletContext().getRealPath(context.pathParam("item"));
                    String file = String.join("\n", Files.readAllLines(Paths.get(path)));
                    context.result(file);
                    String[] split = path.split("\\.");
                    context.contentType("text/" + split[split.length - 1]);
                })
                .javalinServlet();
        super.init(config);
    }

}
