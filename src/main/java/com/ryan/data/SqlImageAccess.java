package com.ryan.data;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;

public class SqlImageAccess implements ImageDataAccess {

    private DataConnector connector;
    public SqlImageAccess(DataConnector connector) {
        this.connector = connector;
    }

    @Override
    public String loadBase64Img(UUID uuid) {
        try (Connection connection = connector.newConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT image FROM image_store WHERE image_id = ?");
            statement.setObject(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getString("image");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public BufferedImage loadBufferedImg(UUID uuid) {
        String base64 = loadBase64Img(uuid);
        if (base64 == null) {
            return null;
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
        try {
            return ImageIO.read(byteArrayInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UUID saveImg(BufferedImage image) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", outputStream);
            String enc = new String(Base64.getEncoder().encode(outputStream.toByteArray()));
            try (Connection connection = connector.newConnection()) {
                UUID uuid = UUID.randomUUID();
                PreparedStatement statement = connection.prepareStatement("INSERT INTO image_store (image_id, image) VALUES (?, ?)");
                statement.setObject(1, uuid);
                statement.setString(2, enc);
                statement.execute();
                return uuid;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
