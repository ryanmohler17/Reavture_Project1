package com.ryan.data;

import com.ryan.models.StoredImage;
import com.ryan.models.User;
import com.ryan.models.UserType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqlUserAccess implements UserDataAccess {

    private DataConnector connector;
    private ImageDataAccess imageDataAccess;
    public SqlUserAccess(DataConnector connector, ImageDataAccess imageDataAccess) {
        this.connector = connector;
        this.imageDataAccess = imageDataAccess;
    }

    @Override
    public Integer saveItem(User item) {
        if (item.getId() == -1) {
            try (Connection c = connector.newConnection()) {
                PreparedStatement statement = c.prepareStatement("INSERT INTO \"user\" (first_name, last_name, username, email_address, password, user_type) VALUES (?. ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, item.getFirstName());
                statement.setString(2, item.getLastName());
                statement.setString(3, item.getUserName());
                statement.setString(4, item.getEmail());
                statement.setString(5, item.getEncryptPassword());
                statement.setInt(6, item.getUserType().ordinal());
                statement.execute();
                ResultSet resultSet = statement.getGeneratedKeys();
                resultSet.next();
                int id = resultSet.getInt("id");
                item.setId(id);
                if (item.getAvatar() != null && item.getAvatar().getBufferedImage() != null) {
                    UUID uuid = imageDataAccess.saveImg(item.getAvatar().getBufferedImage());
                    item.getAvatar().setId(uuid);
                }
                return id;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try (Connection c = connector.newConnection()) {
                PreparedStatement statement = c.prepareStatement("UPDATE \"user\" SET first_name = ?, last_name = ?, username = ?, email_address = ?, password = ?, user_type = ? WHERE id = ?");
                statement.setString(1, item.getFirstName());
                statement.setString(2, item.getLastName());
                statement.setString(3, item.getUserName());
                statement.setString(4, item.getEmail());
                statement.setString(5, item.getEncryptPassword());
                statement.setInt(6, item.getUserType().ordinal());
                statement.setInt(7, item.getId());
                if (item.getAvatar() != null && item.getAvatar().getBufferedImage() != null) {
                    PreparedStatement iconStatement;
                    UUID uuid = imageDataAccess.saveImg(item.getAvatar().getBufferedImage());
                    if (item.getAvatar().getId() == null) {
                        iconStatement = c.prepareStatement("INSERT INTO user_icon (image_id, user_id) VALUES (?, ?)");
                    } else {
                        iconStatement = c.prepareStatement("UPDATE user_icon SET image_id = ? WHERE user_id = ?;");
                    }
                    iconStatement.setObject(1, uuid);
                    iconStatement.setInt(2, item.getId());
                    iconStatement.execute();
                    item.getAvatar().setId(uuid);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return item.getId();
        }
        return null;
    }

    @Override
    public User getItem(Integer item) {
        try (Connection c = connector.newConnection()) {
            PreparedStatement statement = c.prepareStatement("SELECT id, first_name, last_name, username, email_address, password, user_type FROM \"user\" WHERE id = ?");
            statement.setInt(1, item);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            User user = getUserFromResultSet(resultSet);
            PreparedStatement imgStatement = c.prepareStatement("SELECT image_id FROM user_icon WHERE user_id = ?");
            imgStatement.setInt(1, item);
            ResultSet imgSet = imgStatement.executeQuery();
            if (imgSet.next()) {
                UUID uuid = imgSet.getObject("image_id", UUID.class);
                user.setAvatar(new StoredImage(uuid, imageDataAccess.loadBase64Img(uuid)));
            }
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<User> getAllItems() {
        try (Connection c = connector.newConnection()) {
            Statement statement = c.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id, first_name, last_name, username, email_address, password, user_type FROM \"user\"");
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(getUserFromResultSet(resultSet));
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User getUserByName(String name) {
        try (Connection c = connector.newConnection()) {
            PreparedStatement statement = c.prepareStatement("SELECT id, first_name, last_name, username, email_address, password, user_type FROM \"user\" WHERE username = ?");
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return getUserFromResultSet(resultSet);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private User getUserFromResultSet(ResultSet resultSet) throws SQLException {
        User user = new User(resultSet.getString("username"), resultSet.getString("password"),
                resultSet.getString("email_address"), resultSet.getString("first_name"),
                resultSet.getString("last_name"), UserType.values()[resultSet.getInt("user_type")]);

        user.setId(resultSet.getInt("id"));

        return user;
    }
}
