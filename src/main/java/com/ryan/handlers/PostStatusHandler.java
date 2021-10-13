package com.ryan.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ryan.MainServlet;
import com.ryan.data.RequestDataAccess;
import com.ryan.data.UserDataAccess;
import com.ryan.models.Request;
import com.ryan.models.RequestStatus;
import com.ryan.models.User;
import com.ryan.models.UserType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

public class PostStatusHandler implements Handler {

    private Properties properties;
    private RequestDataAccess requestDataAccess;
    private UserDataAccess userDataAccess;
    private Gson gson;
    public PostStatusHandler(Properties properties, RequestDataAccess requestDataAccess, UserDataAccess userDataAccess, Gson gson) {
        this.properties = properties;
        this.requestDataAccess = requestDataAccess;
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
        int reqId = json.get("req").getAsInt();
        Request request = requestDataAccess.getItem(reqId);
        if (request == null) {
            ctx.status(400);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Invalid request");
            ctx.result(gson.toJson(error));
            return;
        }

        String statusS = json.get("status").getAsString();
        RequestStatus status = Arrays.stream(RequestStatus.values()).filter(requestStatus -> requestStatus.name().equalsIgnoreCase(statusS)).findFirst().get();
        request.setStatus(status);
        if (!status.equals(RequestStatus.OPEN)) {
            request.setResolvedBy(login);
        } else {
            request.setResolvedBy(-1);
        }
        request.setLastUpdate(new Date());
        requestDataAccess.saveItem(request);

        ctx.status(200);
        JsonObject success = new JsonObject();
        success.addProperty("success", true);
        ctx.result(gson.toJson(success));
    }
}
