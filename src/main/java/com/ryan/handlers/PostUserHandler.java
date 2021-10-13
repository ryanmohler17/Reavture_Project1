package com.ryan.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ryan.MainServlet;
import com.ryan.data.UserDataAccess;
import com.ryan.models.User;
import com.ryan.models.UserType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class PostUserHandler implements Handler {

    private Properties properties;
    private UserDataAccess userDataAccess;
    private Gson gson;
    public PostUserHandler(Properties properties, UserDataAccess userDataAccess, Gson gson) {
        this.properties = properties;
        this.userDataAccess = userDataAccess;
        this.gson = gson;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        int login = MainServlet.checkLogin(properties, ctx);
        ctx.contentType("application/json");
        if (login == -1) {
            ctx.status(401);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Not logged in");
            ctx.result(gson.toJson(error));
            return;
        }
        User user = userDataAccess.getItem(login);
        if (!user.getUserType().equals(UserType.MANAGER)) {
            ctx.status(403);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Not allowed to do this");
            ctx.result(gson.toJson(error));
            return;
        }
        JsonObject json = JsonParser.parseString(ctx.body()).getAsJsonObject();
        User toCreate = new User(json.get("userName").getAsString(), "", json.get("email").getAsString(),
                json.get("firstName").getAsString(), json.get("lastName").getAsString(), UserType.EMPLOYEE);

        toCreate.setPassword(json.get("password").getAsString());

        userDataAccess.saveItem(toCreate);

        ctx.status(200);
        JsonObject success = new JsonObject();
        success.addProperty("success", true);
        ctx.result(gson.toJson(success));
    }
}
