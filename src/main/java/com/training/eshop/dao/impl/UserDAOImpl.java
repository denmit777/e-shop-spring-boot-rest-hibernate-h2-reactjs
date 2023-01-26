package com.training.eshop.dao.impl;

import com.training.eshop.dao.UserDAO;
import com.training.eshop.exception.UserNotFoundException;
import com.training.eshop.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@AllArgsConstructor
public class UserDAOImpl implements UserDAO {

    private static final String QUERY_SELECT_FROM_USER = "from User";
    private static final String QUERY_SELECT_ALL_ADMINS_FROM_USERS = "from User u where u.role = 'ROLE_ADMIN'";
    private static final String USER_NOT_FOUND = "User with login %s not found";

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public void save(User user) {
        entityManager.persist(user);
    }

    @Override
    public User getByLogin(String login) {
        return getAll().stream()
                .filter(user -> login.equals(user.getEmail()))
                .findAny()
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND, login)));
    }

    @Override
    public List<User> getAll() {
        return entityManager.createQuery(QUERY_SELECT_FROM_USER, User.class)
                .getResultList();
    }

    @Override
    public List<User> getAllAdmins() {
        return entityManager.createQuery(QUERY_SELECT_ALL_ADMINS_FROM_USERS, User.class)
                .getResultList();
    }
}
