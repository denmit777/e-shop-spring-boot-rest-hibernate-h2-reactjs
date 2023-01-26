package com.training.eshop.dao;

import com.training.eshop.model.History;

import java.util.List;

public interface HistoryDAO {

    void save(History history);

    List<History> getAll();

    List<History> getLastFiveByOrderId(Long id);

    List<History> getAllByOrderId(Long id);
}
