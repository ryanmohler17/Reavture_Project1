package com.ryan.data;

import com.ryan.models.Request;
import com.ryan.models.RequestPart;
import com.ryan.models.RequestStatus;
import com.ryan.models.RequestType;
import com.ryan.models.RequestsCounts;
import com.ryan.models.StoredImage;

import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SqlRequestAccess implements RequestDataAccess {

    private DataConnector connector;
    private ImageDataAccess imageDataAccess;
    public SqlRequestAccess(DataConnector connector, ImageDataAccess imageDataAccess) {
        this.connector = connector;
        this.imageDataAccess = imageDataAccess;
    }

    @Override
    public Integer saveItem(Request item) {
        int id = item.getId();
        if (id == -1) {
            try (Connection connection = connector.newConnection()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO reimbursement_request (employee, submitted, status, last_update, resolved_by) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                statement.setInt(1, item.getEmployee());
                statement.setTimestamp(2, new Timestamp(item.getSubmitted().getTime()));
                statement.setInt(3, item.getStatus().ordinal());
                statement.setTimestamp(4, new Timestamp(item.getLastUpdate().getTime()));
                if (item.getResolvedBy() > 0) {
                    statement.setInt(5, item.getResolvedBy());
                } else {
                    statement.setObject(5, null);
                }
                statement.execute();
                ResultSet resultSet = statement.getGeneratedKeys();
                resultSet.next();
                id = resultSet.getInt("id");
                item.setId(id);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            try (Connection connection = connector.newConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE reimbursement_request SET employee = ?, submitted = ?, status = ?, last_update = ?, resolved_by = ? WHERE id = ?");
                statement.setInt(1, item.getEmployee());
                statement.setDate(2, new Date(item.getSubmitted().getTime()));
                statement.setInt(3, item.getStatus().ordinal());
                statement.setDate(4, new Date(item.getLastUpdate().getTime()));
                if (item.getResolvedBy() != 0) {
                    statement.setInt(5, item.getResolvedBy());
                } else {
                    statement.setObject(5, null);
                }
                statement.setInt(6, id);
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        for (RequestPart requestPart : item.getParts()) {
            savePart(requestPart, id);
        }
        return id;
    }

    @Override
    public Request getItem(Integer item) {
        try (Connection connection = connector.newConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT id, employee, submitted, status, last_update, resolved_by FROM reimbursement_request WHERE id = ?");
            statement.setInt(1, item);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return requestFromResultSet(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Request> getAllItems() {
        try (Connection connection = connector.newConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id, employee, submitted, status, last_update, resolved_by FROM reimbursement_request");
            List<Request> requests = new ArrayList<>();
            while (resultSet.next()) {
                requests.add(requestFromResultSet(resultSet));
            }
            return requests;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Integer savePart(RequestPart part, int reqId) {
        int id = part.getId();
        if (id == -1) {
            try (Connection connection = connector.newConnection()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO reimbursement_part (amount, description, type, rate) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                statement.setDouble(1, part.getAmount());
                statement.setString(2, part.getDescription());
                statement.setInt(3, part.getType().ordinal());
                statement.setDouble(4, part.getRate());
                statement.execute();
                ResultSet resultSet = statement.getGeneratedKeys();
                resultSet.next();
                id = resultSet.getInt("id");
                part.setId(id);
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO part_request_link (request_id, part_id) VALUES (?, ?)");
                preparedStatement.setInt(1, reqId);
                preparedStatement.setInt(2, id);
                preparedStatement.execute();
                for (StoredImage image : part.getImages()) {
                    saveImage(image, id);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            try (Connection connection = connector.newConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE reimbursement_part SET amount = ?, description = ?, type = ?, rate = ? WHERE id = ?");
                statement.setDouble(1, part.getAmount());
                statement.setString(2, part.getDescription());
                statement.setInt(3, part.getType().ordinal());
                statement.setDouble(4, part.getRate());
                statement.setInt(5, id);
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        return id;
    }

    @Override
    public RequestPart loadPart(int id) throws SQLException {
        Connection connection = connector.newConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT id, amount, description, type, rate FROM reimbursement_part WHERE id = ?");
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        RequestPart requestPart = new RequestPart(resultSet.getDouble("amount"), resultSet.getString("description"),
                RequestType.values()[resultSet.getInt("type")]);

        requestPart.setId(id);
        double rate = resultSet.getDouble("rate");
        if (!resultSet.wasNull()) {
            requestPart.setRate(rate);
        }

        PreparedStatement imgStatement = connection.prepareStatement("SELECT image_id FROM part_img WHERE part_id = ?");
        imgStatement.setInt(1, id);
        ResultSet imgSet = imgStatement.executeQuery();
        while (imgSet.next()) {
            UUID uuid = UUID.fromString(imgSet.getString("image_id"));
            StoredImage storedImage = new StoredImage(uuid, imageDataAccess.loadBase64Img(uuid));
            requestPart.getImages().add(storedImage);
        }
        return requestPart;
    }

    @Override
    public void saveImage(StoredImage img, int partId) {
        BufferedImage image = img.getBufferedImage();
        if (image == null) {
            return;
        }
        UUID uuid = imageDataAccess.saveImg(image);
        img.setId(uuid);

        try (Connection connection = connector.newConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO part_img (part_id, image_id) VALUES (?, ?)");
            statement.setInt(1, partId);
            statement.setObject(2, uuid);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public RequestsCounts getEmployeeRequestCounts(int employeeId) {
        try (Connection connection = connector.newConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT count(id) as allreq, " +
                    "count(case status when 0 then 1 else null end) as open," +
                    "count(case status when 1 then 1 else null end) as approved," +
                    "count(case status when 2 then 1 else null end) as denied" +
                    " FROM reimbursement_request WHERE employee = ?");

            statement.setInt(1, employeeId);
            ResultSet resultSet = statement.executeQuery();

            resultSet.next();

            return new RequestsCounts(resultSet.getInt("allreq"), resultSet.getInt("open"),
                    resultSet.getInt("approved"), resultSet.getInt("denied"));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Request> getEmployeeRequests(int employeeId) {
        try (Connection connection = connector.newConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT id, employee, submitted, status, last_update, resolved_by FROM reimbursement_request WHERE employee = ?");
            statement.setInt(1, employeeId);
            ResultSet resultSet = statement.executeQuery();
            List<Request> requests = new ArrayList<>();
            while (resultSet.next()) {
                requests.add(requestFromResultSet(resultSet));
            }
            return requests;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Request> getEmployeeRequests(int employeeId, int start, int limit) {
        try (Connection connection = connector.newConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT id, employee, submitted, status, last_update, resolved_by FROM reimbursement_request WHERE employee = ? LIMIT ? OFFSET ?");
            statement.setInt(1, employeeId);
            statement.setInt(2, limit);
            statement.setInt(3, start);
            ResultSet resultSet = statement.executeQuery();
            List<Request> requests = new ArrayList<>();
            while (resultSet.next()) {
                requests.add(requestFromResultSet(resultSet));
            }
            return requests;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Request> getRequests(int start, int limit) {
        try (Connection connection = connector.newConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT id, employee, submitted, status, last_update, resolved_by FROM reimbursement_request LIMIT ? OFFSET ?");
            statement.setInt(1, limit);
            statement.setInt(2, start);
            ResultSet resultSet = statement.executeQuery();
            List<Request> requests = new ArrayList<>();
            while (resultSet.next()) {
                requests.add(requestFromResultSet(resultSet));
            }
            return requests;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Request requestFromResultSet(ResultSet resultSet) throws SQLException {
        Request request = new Request(resultSet.getInt("employee"), resultSet.getTimestamp("submitted"),
                RequestStatus.values()[resultSet.getInt("status")], resultSet.getTimestamp("last_update"));

        int resolved = resultSet.getInt("resolved_by");
        if (!resultSet.wasNull()) {
            request.setResolvedBy(resolved);
        }

        int id = resultSet.getInt("id");
        request.setId(id);

        Connection connection = connector.newConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT part_id FROM part_request_link WHERE request_id = ?");
        statement.setInt(1, id);
        ResultSet partResults = statement.executeQuery();
        while (partResults.next()) {
            int partId = partResults.getInt("part_id");
            request.getParts().add(loadPart(partId));
        }

        return request;
    }

}
