package com.ryan.data;

import com.ryan.models.Request;
import com.ryan.models.RequestsCounts;

import java.util.List;

public interface RequestDataAccess extends DataAccess<Integer, Request> {

    RequestsCounts getEmployeeRequestCounts(int employeeId);

    List<Request> getEmployeeRequests(int employeeId);

    List<Request> getEmployeeRequests(int employeeId, int start, int limit);

}
