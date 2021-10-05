package com.ryan.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ryan.MainServlet;
import com.ryan.data.RequestDataAccess;
import com.ryan.data.UserDataAccess;
import com.ryan.models.Request;
import com.ryan.models.RequestsCounts;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Properties;

public class GetRequestsHandler implements Handler {

    private Properties properties;
    private UserDataAccess userDataAccess;
    private RequestDataAccess requestDataAccess;
    private Gson gson;
    public GetRequestsHandler(Properties properties, UserDataAccess userDataAccess, RequestDataAccess requestDataAccess, Gson gson) {
        this.properties = properties;
        this.userDataAccess = userDataAccess;
        this.requestDataAccess = requestDataAccess;
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
        String limitS = ctx.queryParam("limit");
        String offsetS = ctx.queryParam("offset");
        JsonElement ret;
        if (limitS != null) {
            int limit = Integer.parseInt(limitS);
            if (offsetS != null) {
                int offset = Integer.parseInt(offsetS);
                ret = handleLimitAndOffset(login, limit, offset);
            } else {
                ret = handleLimitAndOffset(login, limit, 0);
            }
        } else {
            ret = handleCounts(login);
        }
        ctx.status(200);
        ctx.result(gson.toJson(ret));
    }

    private JsonElement handleLimitAndOffset(int employee, int limit, int offset) {
        List<Request> requests = requestDataAccess.getEmployeeRequests(employee, offset, limit);
        return gson.toJsonTree(requests);
    }

    private JsonElement handleCounts(int employee) {
        RequestsCounts requestsCounts = requestDataAccess.getEmployeeRequestCounts(employee);
        return gson.toJsonTree(requestsCounts);
    }

}
