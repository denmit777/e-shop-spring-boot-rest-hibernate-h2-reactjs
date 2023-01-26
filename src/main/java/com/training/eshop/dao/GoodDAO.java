package com.training.eshop.dao;

import com.training.eshop.model.Good;

import java.util.List;

public interface GoodDAO {

    void save(Good good);

    List<Good> getAll();

    List<Good> getAllBySearch(String searchField, String searchParameter);

    Good getById(Long id);

    void update(Good good);

    void deleteById(Long id);
}
