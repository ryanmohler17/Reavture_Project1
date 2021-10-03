package com.ryan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ryan.data.DataConnector;
import com.ryan.data.ImageDataAccess;
import com.ryan.data.RequestDataAccess;
import com.ryan.data.SqlImageAccess;
import com.ryan.data.SqlRequestAccess;
import com.ryan.data.SqlUserAccess;
import com.ryan.data.UserDataAccess;
import com.ryan.models.StoredImage;
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
        connector = new DataConnector(properties);
        ImageDataAccess imageDataAccess = new SqlImageAccess(connector);
        UserDataAccess userDataAccess = new SqlUserAccess(connector, imageDataAccess);
        RequestDataAccess requestDataAccess = new SqlRequestAccess(connector, imageDataAccess);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        javalinServlet = Javalin.createStandalone()
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
                        returnObj.add("user", userJson);
                        status = 200;
                    }
                    context.status(status);
                    context.contentType("application/json");
                    context.result(gson.toJson(returnObj));
                })
                .javalinServlet();
    }
}
