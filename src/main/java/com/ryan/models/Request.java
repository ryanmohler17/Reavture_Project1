package com.ryan.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Request {

    private int id;
    private int employee;
    private Date submitted;
    private RequestStatus status;
    private List<RequestPart> parts = new ArrayList<>();
    private Date lastUpdate;

    public Request(int employee, Date submitted, RequestStatus status, Date lastUpdate) {
        this.employee = employee;
        this.submitted = submitted;
        this.status = status;
        this.lastUpdate = lastUpdate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployee() {
        return employee;
    }

    public void setEmployee(int employee) {
        this.employee = employee;
    }

    public Date getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Date submitted) {
        this.submitted = submitted;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public List<RequestPart> getParts() {
        return parts;
    }

    public void setParts(List<RequestPart> parts) {
        this.parts = parts;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
