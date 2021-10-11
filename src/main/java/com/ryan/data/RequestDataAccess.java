package com.ryan.data;

import com.ryan.models.Request;
import com.ryan.models.RequestPart;
import com.ryan.models.RequestsCounts;
import com.ryan.models.StoredImage;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface RequestDataAccess extends DataAccess<Integer, Request> {

    /**
     * Saves a request part
     *
     * @param part The request part
     * @param reqId The id of the request the part is a part of
     * @return The generated if of the part
     */
    Integer savePart(RequestPart part, int reqId);

    /**
     * Loads a request part from a given id
     *
     * @param id The id of the part
     * @return The request part that was loaded
     */
    RequestPart loadPart(int id);

    /**
     * Saves a part image
     *
     * @param img The image to save
     * @param partId The id of the part the image is on
     */
    void saveImage(StoredImage img, int partId);

    /**
     * Get the requests counts for the specified employee
     *
     * @param employeeId The id of the employee to get counts for
     * @return The requests counts of the employee
     */
    RequestsCounts getEmployeeRequestCounts(int employeeId);

    /**
     * Get all requests for a specified employee
     *
     * @param employeeId The id of the employee to use
     * @return The list of requests they have
     */
    List<Request> getEmployeeRequests(int employeeId);

    /**
     * Get a certain amount of requests for an employee
     *
     * @param employeeId The id of the employee
     * @param start The offset to use
     * @param limit The amount of requests to get
     * @return The list of request an employee has limited.
     */
    List<Request> getEmployeeRequests(int employeeId, int start, int limit);

    /**
     * Get a certain amount of requests
     *
     * @param start The offset to use
     * @param limit The amount of requests to get
     * @return A limited list of requests
     */
    List<Request> getRequests(int start, int limit);

}
