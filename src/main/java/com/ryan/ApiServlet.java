package com.ryan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.ryan.data.DataConnector;
import com.ryan.data.ImageDataAccess;
import com.ryan.data.RequestDataAccess;
import com.ryan.data.SqlImageAccess;
import com.ryan.data.SqlRequestAccess;
import com.ryan.data.SqlUserAccess;
import com.ryan.data.UserDataAccess;
import com.ryan.handlers.GetRequestsHandler;
import com.ryan.handlers.PostRequestsHandler;
import com.ryan.handlers.PostStatusHandler;
import com.ryan.models.Request;
import com.ryan.models.StoredImage;
import com.ryan.models.User;
import com.ryan.models.UserType;
import io.javalin.Javalin;
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
import java.util.Properties;

public class ApiServlet extends HttpServlet {

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
        Logger logger = LogManager.getLogger(ApiServlet.class);
        connector = new DataConnector(properties);
        ImageDataAccess imageDataAccess = new SqlImageAccess(connector, logger);
        UserDataAccess userDataAccess = new SqlUserAccess(connector, imageDataAccess, logger);
        RequestDataAccess requestDataAccess = new SqlRequestAccess(connector, imageDataAccess, logger);
        Gson gson = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy hh:mm:ss aa zzz ").setPrettyPrinting()
                .registerTypeAdapter(StoredImage.class, new JsonSerializer<StoredImage>() {
                    @Override
                    public JsonElement serialize(StoredImage src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.getImage64());
                    }
                }).create();

        GetRequestsHandler getRequestsHandler = new GetRequestsHandler(properties, requestDataAccess, userDataAccess, gson);
        PostRequestsHandler postRequestsHandler = new PostRequestsHandler(properties, requestDataAccess, gson);
        PostStatusHandler postStatusHandler = new PostStatusHandler(properties, requestDataAccess, userDataAccess, gson);

        javalinServlet = Javalin.createStandalone()
                .before(ctx -> {
                    int user = MainServlet.checkLogin(properties, ctx);
                    if (user != -1) {
                        logger.info("User with id " + user + " requested api endpoint " + ctx.path());
                    } else {
                        logger.info("Guest user requested api endpoint " + ctx.path());
                    }
                })
                .get("/api/user", context -> {
                    int user = MainServlet.checkLogin(properties, context);
                    JsonObject returnObj = new JsonObject();
                    int status;
                    if (user == -1) {
                        returnObj.addProperty("error", "User is not logged in");
                        status = 401;
                    } else {
                        User userObj = userDataAccess.getItem(user);
                        JsonObject userJson = gson.toJsonTree(userObj).getAsJsonObject();
                        StoredImage storedImage = userObj.getAvatar();
                        if (storedImage != null) {
                            userJson.addProperty("avatar", userObj.getAvatar().getImage64());
                        }
                        returnObj = userJson;
                        status = 200;
                    }
                    context.status(status);
                    context.contentType("application/json");
                    context.result(gson.toJson(returnObj));
                })
                .patch("/api/user", context -> {

                })
                .get("/api/user/{id}", context -> {
                    int user = MainServlet.checkLogin(properties, context);
                    int id = Integer.parseInt(context.pathParam("id"));
                    JsonObject returnObj = new JsonObject();
                    int status;
                    if (user == -1) {
                        returnObj.addProperty("error", "User is not logged in");
                        status = 401;
                    } else {
                        User userObj = userDataAccess.getItem(id);
                        JsonObject userJson = gson.toJsonTree(userObj).getAsJsonObject();
                        StoredImage storedImage = userObj.getAvatar();
                        if (storedImage != null) {
                            userJson.addProperty("avatar", userObj.getAvatar().getImage64());
                        }
                        returnObj = userJson;
                        status = 200;
                    }
                    context.status(status);
                    context.contentType("application/json");
                    context.result(gson.toJson(returnObj));
                })
                .get("/api/requests", getRequestsHandler)
                .post("/api/requests", postRequestsHandler)
                .get("/api/requests/{id}", context -> {
                    int login = MainServlet.checkLogin(properties, context);
                    JsonObject returnObj = new JsonObject();
                    int status;
                    if (login == -1) {
                        returnObj.addProperty("error", "User is not logged in");
                        status = 401;
                    } else {
                        User user = userDataAccess.getItem(login);
                        int id = Integer.parseInt(context.pathParam("id"));
                        Request request = requestDataAccess.getItem(id);
                        if (!user.getUserType().equals(UserType.MANAGER) && request.getEmployee() != login) {
                            status = 403;
                            returnObj.addProperty("error", "User doesn't have access to this request");
                        } else {
                            status = 200;
                            returnObj = gson.toJsonTree(request).getAsJsonObject();
                        }
                    }
                    context.status(status);
                    context.contentType("application/json");
                    context.result(gson.toJson(returnObj));
                })
                .post("/api/requests/status", postStatusHandler)
                .javalinServlet();
    }
}
