package com.training.eshop.dao.impl;

import com.training.eshop.dao.FeedbackDAO;
import com.training.eshop.model.Feedback;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@AllArgsConstructor
public class FeedbackDAOImpl implements FeedbackDAO {

    private static final String ORDER_ID = "orderId";
    private static final String FEEDBACK_ID = "feedbackId";
    private static final String QUERY_SELECT_FROM_FEEDBACK_BY_ORDER_ID = "from Feedback f where f.order.id =:orderId";
    private static final String QUERY_SELECT_FROM_FEEDBACK_BY_ORDER_ID_ORDERED_BY_DATE = "from Feedback f where f.order.id =:orderId order by date DESC";
    private static final String QUERY_SELECT_FROM_FEEDBACK_BY_ORDER_ID_AND_FEEDBACK_ID = "from Feedback f where f.order.id =:orderId and f.id =:feedbackId";

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public void save(Feedback feedBack) {
        entityManager.persist(feedBack);
    }

    @Override
    public Feedback getByFeedbackIdAndOrderId(Long feedbackId, Long orderId) {
        return (Feedback) entityManager.createQuery(QUERY_SELECT_FROM_FEEDBACK_BY_ORDER_ID_AND_FEEDBACK_ID)
                .setParameter(FEEDBACK_ID, feedbackId)
                .setParameter(ORDER_ID, orderId)
                .getSingleResult();
    }

    @Override
    public List<Feedback> getLastFiveByOrderId(Long id) {
        return entityManager.createQuery(QUERY_SELECT_FROM_FEEDBACK_BY_ORDER_ID_ORDERED_BY_DATE, Feedback.class)
                .setParameter(ORDER_ID, id)
                .setMaxResults(5)
                .getResultList();
    }

    @Override
    public List<Feedback> getAllByOrderId(Long id) {
        return entityManager.createQuery(QUERY_SELECT_FROM_FEEDBACK_BY_ORDER_ID, Feedback.class)
                .setParameter(ORDER_ID, id)
                .getResultList();
    }
}
