package com.training.eshop.util.comparator;

import com.training.eshop.model.User;

import java.util.Comparator;

public class OrderUserComparator implements Comparator<User> {

    @Override
    public int compare(User user1, User user2) {
        return user1.getName().compareTo(user2.getName());
    }
}
