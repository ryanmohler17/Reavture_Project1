package com.ryan.models;

public class RequestsCounts {

    private int total;
    private int open;
    private int approved;
    private int denied;

    public RequestsCounts(int total, int open, int approved, int denied) {
        this.total = total;
        this.open = open;
        this.approved = approved;
        this.denied = denied;
    }

    public int getTotal() {
        return total;
    }

    public int getOpen() {
        return open;
    }

    public int getApproved() {
        return approved;
    }

    public int getDenied() {
        return denied;
    }

    public int getClosed() {
        return denied + approved;
    }
}
