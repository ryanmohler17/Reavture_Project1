package com.ryan.data;

import com.ryan.models.User;

public interface UserDataAccess extends DataAccess<Integer, User> {

    /**
     * Gets a user with a given username
     *
     * @param name The username to look up
     * @return The user with the given username
     */
    User getUserByName(String name);

}
