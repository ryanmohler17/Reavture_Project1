package com.ryan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
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
import com.ryan.handlers.LoginHandler;
import com.ryan.models.User;
import io.javalin.Javalin;
import io.javalin.http.Context;
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
import java.text.ParseException;
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

        LoginHandler loginHandler = new LoginHandler(userDataAccess, gson, Base64.getDecoder().decode(properties.getProperty("user.secret")));

        javalinServlet = Javalin.createStandalone()
                .get("/", context -> {
                    int login = checkLogin(properties, context);
                    if (login == -1) {
                        context.redirect("login");
                        return;
                    }

                    User user = userDataAccess.getItem(login);
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
                .post("/login", loginHandler)
                .get("/logout", context -> {
                    context.redirect("/ers/login");
                    context.removeCookie("token");
                })
                .get("/public/{item}", context -> {
                    String path = getServletContext().getRealPath("public/" + context.pathParam("item"));
                    String file = String.join("\n", Files.readAllLines(Paths.get(path)));
                    context.result(file);
                    String[] split = path.split("\\.");
                    context.contentType("text/" + split[split.length - 1]);
                })
                .javalinServlet();
        super.init(config);
    }

    public static int checkLogin(Properties properties, Context context) throws ParseException, JOSEException {
        String token = context.cookie("token");
        if (token == null) {
            return -1;
        }

        byte[] secret = Base64.getDecoder().decode(properties.getProperty("user.secret"));
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(secret);

        if (!signedJWT.verify(verifier) || !new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime())) {
            context.removeCookie("token");
            return -1;
        }

        return Integer.parseInt(signedJWT.getJWTClaimsSet().getJWTID());
    }

}
