package com.training.eshop.dao.impl;

import com.training.eshop.dao.OrderDAO;
import com.training.eshop.exception.OrderNotFoundException;
import com.training.eshop.model.Order;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@AllArgsConstructor
public class OrderDAOImpl implements OrderDAO {

    private static final String QUERY_SELECT_FROM_ORDER = "from Order";
    private static final String ORDER_NOT_FOUND = "Order with id %s not found";

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public void save(Order order) {
        entityManager.persist(order);
    }

    @Override
    public Order getById(Long id) {
        return getAll().stream()
                .filter(order -> id.equals(order.getId()))
                .findAny()
                .orElseThrow(() -> new OrderNotFoundException(String.format(ORDER_NOT_FOUND, id)));
    }

    @Override
    public List<Order> getAll() {
        return entityManager.createQuery(QUERY_SELECT_FROM_ORDER, Order.class)
                .getResultList();
    }
}
