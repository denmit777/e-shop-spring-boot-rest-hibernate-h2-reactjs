package com.training.eshop.util;

import com.training.eshop.model.User;
import com.training.eshop.model.enums.Role;
import com.training.eshop.util.comparator.OrderUserComparator;
import org.junit.Assert;
import org.junit.Test;

public class OrderUserComparatorTest {

    private final OrderUserComparator comparator = new OrderUserComparator();

    @Test
    public void compareTest_IfNameOfUserOneLexicographicallyGreaterThanNameOfUserTwo_ThenReturnPositiveNumber() {
        User userOne = createTestUser(1L, "Den", "den_mogilev@yopmail.com", "1234", Role.ROLE_ADMIN);
        User userTwo = createTestUser(2L, "Asya", "asya_mogilev@yopmail.com", "5678", Role.ROLE_BUYER);

        int actual = comparator.compare(userOne, userTwo);

        Assert.assertTrue(actual > 0);
    }

    @Test
    public void compareTest_IfNameOfUserOneLexicographicallyLessThanNameOfUserTwo_ThenReturnNegativeNumber() {
        User userOne = createTestUser(1L, "Den", "den_mogilev@yopmail.com", "1234", Role.ROLE_ADMIN);
        User userTwo = createTestUser(3L, "Peter", "peter_mogilev@yopmail.com", "4567", Role.ROLE_BUYER);

        int actual = comparator.compare(userOne, userTwo);

        Assert.assertTrue(actual < 0);
    }

    @Test
    public void compareTest_IfNameOfUserOneLexicographicallyEqualsNameOfUserTwo_ThenReturnZero() {
        User userOne = createTestUser(3L, "Peter", "peter_mogilev@yopmail.com", "4567", Role.ROLE_BUYER);
        User userTwo = createTestUser(3L, "Peter", "peter_mogilev@yopmail.com", "4567", Role.ROLE_BUYER);

        int actual = comparator.compare(userOne, userTwo);

        Assert.assertEquals(0, actual);
    }

    private User createTestUser(Long id, String name, String email, String password, Role role) {
        User user = new User();

        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);

        return user;
    }
}
