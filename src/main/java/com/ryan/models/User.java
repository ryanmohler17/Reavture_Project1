package com.ryan.models;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.util.UUID;

public class User {

    private int id = -1;
    private String userName;
    private String encryptPassword;
    private String email;
    private String firstName;
    private String lastName;
    private UserType userType;
    private StoredImage avatar;

    public User() {

    }

    public User(String userName, String encryptPassword, String email, String firstName, String lastName, UserType userType) {
        this.userName = userName;
        this.encryptPassword = encryptPassword;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEncryptPassword(String encryptPassword) {
        this.encryptPassword = encryptPassword;
    }

    public String getEncryptPassword() {
        return encryptPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public StoredImage getAvatar() {
        return avatar;
    }

    public void setAvatar(StoredImage avatar) {
        this.avatar = avatar;
    }

    public boolean verifyPassword(String password) {
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), encryptPassword);
        return result.verified;
    }

    public void setPassword(String password) {
        encryptPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

}
