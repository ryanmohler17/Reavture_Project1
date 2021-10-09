package com.ryan.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ryan.MainServlet;
import com.ryan.data.RequestDataAccess;
import com.ryan.data.UserDataAccess;
import com.ryan.models.Request;
import com.ryan.models.RequestsCounts;
import com.ryan.models.User;
import com.ryan.models.UserType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Properties;

public class GetRequestsHandler implements Handler {

    private Properties properties;
    private RequestDataAccess requestDataAccess;
    private UserDataAccess userDataAccess;
    private Gson gson;

    public GetRequestsHandler(Properties properties, RequestDataAccess requestDataAccess, UserDataAccess userDataAccess, Gson gson) {
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
        String limitS = ctx.queryParam("limit");
        String offsetS = ctx.queryParam("offset");
        JsonElement ret;
        int status = 200;
        if (user.getUserType().equals(UserType.EMPLOYEE)) {
            if (limitS != null) {
                int limit = Integer.parseInt(limitS);
                int offset = 0;
                if (offsetS != null) {
                    offset = Integer.parseInt(offsetS);
                }
                try {
                    ret = handleEmployeeLimitAndOffset(login, limit, offset);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                ret = handleCounts(login);
            }
        } else {
            if (limitS == null) {
                ret = new JsonObject();
                ((JsonObject) ret).addProperty("error", "Must specify limit");
                status = 400;
            } else {
                int limit = Integer.parseInt(limitS);
                int offset = 0;
                if (offsetS != null) {
                    offset = Integer.parseInt(offsetS);
                }
                ret = handleManagerLimitAndOffset(limit, offset);
            }
        }
        ctx.status(status);
        ctx.result(gson.toJson(ret));
    }

    private JsonElement handleEmployeeLimitAndOffset(int employee, int limit, int offset) {
        List<Request> requests = requestDataAccess.getEmployeeRequests(employee, offset, limit);
        return gson.toJsonTree(requests);
    }

    private JsonElement handleCounts(int employee) {
        RequestsCounts requestsCounts = requestDataAccess.getEmployeeRequestCounts(employee);
        return gson.toJsonTree(requestsCounts);
    }

    private JsonElement handleManagerLimitAndOffset(int limit, int offset) {
        List<Request> requests = requestDataAccess.getRequests(offset, limit);
        return gson.toJsonTree(requests);
    }

}
