package com.ryan.data;

import com.ryan.models.User;

public interface UserDataAccess extends DataAccess<Integer, User> {

    User getUserByName(String name);

}
