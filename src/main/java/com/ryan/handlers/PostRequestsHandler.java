package com.ryan.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ryan.MainServlet;
import com.ryan.data.RequestDataAccess;
import com.ryan.models.Request;
import com.ryan.models.RequestPart;
import com.ryan.models.RequestStatus;
import com.ryan.models.RequestType;
import com.ryan.models.StoredImage;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

public class PostRequestsHandler implements Handler {

    private Properties properties;
    private RequestDataAccess requestDataAccess;
    private Gson gson;
    public PostRequestsHandler(Properties properties, RequestDataAccess requestDataAccess, Gson gson) {
        this.properties = properties;
        this.requestDataAccess = requestDataAccess;
        this.gson = gson;
    }

    private double rate = .65;

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

        JsonArray jsonArray = JsonParser.parseString(ctx.body()).getAsJsonArray();
        Date submitted = new Date();
        Request request = new Request(login, submitted, RequestStatus.OPEN, submitted);
        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            RequestPart requestPart = new RequestPart(jsonObject.get("amount").getAsDouble(),
                    jsonObject.get("description").getAsString(),
                    RequestType.getById(jsonObject.get("type").getAsString()));

            if (requestPart.getType().equals(RequestType.TRAVEL_MILES)) {
                requestPart.setRate(jsonObject.get("rate").getAsDouble());
            }

            if (jsonObject.has("images")) {
                JsonArray images = jsonObject.get("images").getAsJsonArray();
                for (JsonElement jsonElement : images) {
                    String base64 = jsonElement.getAsString();
                    String[] split = base64.split(",");
                    byte[] data = Base64.getDecoder().decode(split[1]);
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                    BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
                    StoredImage storedImage = new StoredImage(null, bufferedImage);
                    requestPart.getImages().add(storedImage);
                }
            }

            request.getParts().add(requestPart);
        }

        requestDataAccess.saveItem(request);
        ctx.status(200);
        JsonObject result = new JsonObject();
        result.addProperty("success", true);
        ctx.result(gson.toJson(result));
    }
}
