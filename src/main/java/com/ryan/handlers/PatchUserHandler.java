package com.ryan.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ryan.MainServlet;
import com.ryan.data.UserDataAccess;
import com.ryan.models.StoredImage;
import com.ryan.models.User;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Properties;

public class PatchUserHandler implements Handler {

    private Properties properties;
    private UserDataAccess userDataAccess;
    private Gson gson;
    public PatchUserHandler(Properties properties, UserDataAccess userDataAccess, Gson gson) {
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
        JsonObject json = JsonParser.parseString(ctx.body()).getAsJsonObject();

        if (json.has("password")) {
            String old = json.get("password").getAsJsonObject().get("old").getAsString();
            if (!user.verifyPassword(old)) {
                ctx.status(403);
                JsonObject error = new JsonObject();
                error.addProperty("error", "Invalid password");
                ctx.result(gson.toJson(error));
                return;
            }
            user.setPassword(json.get("password").getAsJsonObject().get("new").getAsString());
        }

        if (json.has("firstName")) {
            user.setFirstName(json.get("firstName").getAsString());
        }
        if (json.has("lastName")) {
            user.setLastName(json.get("lastName").getAsString());
        }
        if (json.has("img")) {
            String img = json.get("img").getAsString();
            String base64 = img.split(",")[1];
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            StoredImage storedImage = new StoredImage(null, bufferedImage);
            user.setAvatar(storedImage);
        }

        userDataAccess.saveItem(user);

        ctx.status(200);
        JsonObject success = new JsonObject();
        success.addProperty("success", true);
        ctx.result(gson.toJson(success));
    }
}
