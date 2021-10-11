package com.ryan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.ryan.data.DataConnector;
import com.ryan.data.ImageDataAccess;
import com.ryan.data.RequestDataAccess;
import com.ryan.data.SqlImageAccess;
import com.ryan.data.SqlRequestAccess;
import com.ryan.data.SqlUserAccess;
import com.ryan.data.UserDataAccess;
import com.ryan.handlers.LoginHandler;
import com.ryan.models.Request;
import com.ryan.models.StoredImage;
import com.ryan.models.User;
import com.ryan.models.UserType;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.JavalinServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
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
        Logger logger = LogManager.getLogger(MainServlet.class);
        connector = new DataConnector(properties);
        ImageDataAccess imageDataAccess = new SqlImageAccess(connector, logger);
        UserDataAccess userDataAccess = new SqlUserAccess(connector, imageDataAccess, logger);
        RequestDataAccess requestDataAccess = new SqlRequestAccess(connector, imageDataAccess, logger);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(StoredImage.class, new JsonSerializer<StoredImage>() {
                    @Override
                    public JsonElement serialize(StoredImage src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.getImage64());
                    }
                }).setPrettyPrinting().create();

        LoginHandler loginHandler = new LoginHandler(userDataAccess, gson, Base64.getDecoder().decode(properties.getProperty("user.secret")));

        javalinServlet = Javalin.createStandalone()
                .before(ctx -> {
                    int user = MainServlet.checkLogin(properties, ctx);
                    logger.info("User with id " + user + " requested normal endpoint " + ctx.path());
                })
                .get("/", context -> {
                    int login = checkLogin(properties, context);
                    if (login == -1) {
                        context.redirect("login");
                        return;
                    }

                    User user = userDataAccess.getItem(login);
                    if (user.getUserType().equals(UserType.EMPLOYEE)) {
                        String path = getServletContext().getRealPath("employee.html");

                        String employee = String.join("\n", Files.readAllLines(Paths.get(path)));
                        context.html(employee);
                    } else {
                        String path = getServletContext().getRealPath("manager.html");

                        String manager = String.join("\n", Files.readAllLines(Paths.get(path)));
                        context.html(manager);
                    }
                })
                .get("/request", context -> {
                    int login = checkLogin(properties, context);
                    if (login == -1) {
                        context.redirect("login");
                        return;
                    }

                    User user = userDataAccess.getItem(login);

                    if (!user.getUserType().equals(UserType.EMPLOYEE)) {
                        context.redirect("/ers/");
                        return;
                    }

                    String path = getServletContext().getRealPath("request.html");

                    String request = String.join("\n", Files.readAllLines(Paths.get(path)));
                    context.html(request);
                })
                .get("/view", context -> {
                    int login = checkLogin(properties, context);
                    if (login == -1) {
                        context.redirect("/ers/login");
                        return;
                    }

                    User user = userDataAccess.getItem(login);
                    int id = Integer.parseInt(context.queryParam("id"));
                    Request request = requestDataAccess.getItem(id);
                    if (!user.getUserType().equals(UserType.MANAGER) && request.getEmployee() != login) {
                        context.status(403);
                        context.result("You don't have access to this");
                        return;
                    }

                    String path = getServletContext().getRealPath("view.html");

                    String view = String.join("\n", Files.readAllLines(Paths.get(path)));
                    context.html(view);
                })
                .get("/requests", context -> {
                    int login = checkLogin(properties, context);
                    if (login == -1) {
                        context.redirect("login");
                        return;
                    }

                    String path = getServletContext().getRealPath("requests.html");

                    String requests = String.join("\n", Files.readAllLines(Paths.get(path)));
                    context.html(requests);
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
        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(token);
        } catch (ParseException e) {
            return -1 ;
        }
        JWSVerifier verifier = new MACVerifier(secret);

        if (!signedJWT.verify(verifier) || !new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime())) {
            context.removeCookie("token");
            return -1;
        }

        return Integer.parseInt(signedJWT.getJWTClaimsSet().getJWTID());
    }

}
