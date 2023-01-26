package com.training.eshop.service.impl;

import com.training.eshop.dto.UserLoginDto;
import com.training.eshop.exception.CheckBoxException;
import com.training.eshop.exception.UserIsPresentException;
import com.training.eshop.exception.UserNotFoundException;
import com.training.eshop.model.User;
import com.training.eshop.model.enums.Role;
import com.training.eshop.security.config.jwt.JwtTokenProvider;
import com.training.eshop.service.UserService;
import com.training.eshop.converter.UserConverter;
import com.training.eshop.dao.UserDAO;
import com.training.eshop.dto.UserRegisterDto;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LogManager.getLogger(UserServiceImpl.class.getName());

    private static final String USER_IS_PRESENT = "User with name %s or email %s is already present";
    private static final String USER_NOT_FOUND = "User with login %s has another password. " +
            "Go to register or enter valid credentials";
    private static final String CHECKBOX_IS_NOT_CLICKED = "You should not be here.\n" +
            "Please, agree with the terms of service first\n";

    private final UserDAO userDAO;
    private final UserConverter userConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public User save(UserRegisterDto userDto, String checkBoxValue) {
        checkUserBeforeSave(userDto, checkBoxValue);

        User user = userConverter.fromUserRegisterDto(userDto);

        user.setRole(Role.ROLE_BUYER);

        userDAO.save(user);

        LOGGER.info("New user : {}", user);

        return user;
    }

    @Override
    @Transactional
    public Map<Object, Object> authenticateUser(UserLoginDto userDto) {
        User user = getByLoginAndPassword(userDto.getLogin(), userDto.getPassword());

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole());

        Map<Object, Object> response = new HashMap<>();

        response.put("userName", user.getName());
        response.put("role", user.getRole());
        response.put("token", token);

        return response;
    }

    @Override
    @Transactional
    public User getByLoginAndPassword(String login, String password) {
        User user = userDAO.getByLogin(login);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {

            LOGGER.info("User : {}", user);

            return user;
        }
        LOGGER.error(String.format(USER_NOT_FOUND, login));

        throw new UserNotFoundException(String.format(USER_NOT_FOUND, login));
    }

    @Transactional
    public boolean isUserPresent(UserRegisterDto userDto) {
        String name = userDto.getName();
        String email = userDto.getEmail();

        return userDAO.getAll().stream().anyMatch(user -> name.equals(user.getName())
                || email.equals(user.getEmail()));
    }

    private void checkUserBeforeSave(UserRegisterDto userDto, String checkBoxValue) {
        if (isUserPresent(userDto)) {
            LOGGER.error(String.format(USER_IS_PRESENT, userDto.getName(), userDto.getEmail()));

            throw new UserIsPresentException(String.format(USER_IS_PRESENT, userDto.getName(), userDto.getEmail()));
        }

        if (!checkBoxValue.equals("yes")) {
            LOGGER.error(CHECKBOX_IS_NOT_CLICKED);

            throw new CheckBoxException(CHECKBOX_IS_NOT_CLICKED);
        }
    }
}
