package com.training.eshop.dao.impl;

import com.training.eshop.dao.HistoryDAO;
import com.training.eshop.model.History;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@AllArgsConstructor
public class HistoryDAOImpl implements HistoryDAO {

    private static final String QUERY_SELECT_FROM_HISTORY = "from History";
    private static final String QUERY_SELECT_FROM_HISTORY_BY_ORDER_ID = "from History h where h.order.id =:id";
    private static final String QUERY_SELECT_FROM_HISTORY_BY_ORDER_ID_ORDERED_BY_DATE = "from History h where h.order.id =:id order by date DESC";

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public void save(History history) {
        entityManager.persist(history);
    }

    @Override
    public List<History> getAll() {
        return entityManager.createQuery(QUERY_SELECT_FROM_HISTORY, History.class)
                .getResultList();
    }

    @Override
    public List<History> getLastFiveByOrderId(Long id) {
        return entityManager.createQuery(QUERY_SELECT_FROM_HISTORY_BY_ORDER_ID_ORDERED_BY_DATE, History.class)
                .setParameter("id", id)
                .setMaxResults(5)
                .getResultList();
    }

    @Override
    public List<History> getAllByOrderId(Long id) {
        return entityManager.createQuery(QUERY_SELECT_FROM_HISTORY_BY_ORDER_ID, History.class)
                .setParameter("id", id)
                .getResultList();
    }
}
