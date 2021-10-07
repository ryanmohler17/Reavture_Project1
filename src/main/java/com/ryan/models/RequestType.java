package com.ryan.models;

public enum RequestType {
    TRAVEL_MILES("miles"),
    TRAVEL_PRICE("gas"),
    PLANE("plane"),
    EQUIPMENT("equip"),
    OTHER("other");

    private String id;
    RequestType(String id) {
        this.id = id;
    }

    public static RequestType getById(String id) {
        for (RequestType requestType : values()) {
            if (requestType.id.equals(id)) {
                return requestType;
            }
        }
        return null;
    }
}
