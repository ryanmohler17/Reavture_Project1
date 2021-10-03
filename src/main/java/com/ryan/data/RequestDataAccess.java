package com.ryan.data;

import com.ryan.models.Request;
import com.ryan.models.RequestPart;
import com.ryan.models.RequestsCounts;
import com.ryan.models.StoredImage;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface RequestDataAccess extends DataAccess<Integer, Request> {

    Integer savePart(RequestPart part, int reqId);

    RequestPart loadPart(int id) throws SQLException;

    void saveImage(StoredImage img, int partId);

    RequestsCounts getEmployeeRequestCounts(int employeeId);

    List<Request> getEmployeeRequests(int employeeId);

    List<Request> getEmployeeRequests(int employeeId, int start, int limit);

}
