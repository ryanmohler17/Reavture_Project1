package com.ryan.data;

import org.apache.logging.log4j.Logger;

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
    private Logger logger;
    public SqlImageAccess(DataConnector connector, Logger logger) {
        this.connector = connector;
        this.logger = logger;
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
            logger.warn("Failed to load base64 image", e);
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
            logger.warn("Failed to load buffered image", e);
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
                logger.warn("Failed to save image to database", e);
                return null;
            }
        } catch (IOException e) {
            logger.warn("Failed to convert image to base64 for saving in database", e);
            return null;
        }
    }

}
