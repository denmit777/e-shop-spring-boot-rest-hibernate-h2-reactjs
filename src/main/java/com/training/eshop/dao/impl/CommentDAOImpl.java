package com.training.eshop.dao.impl;

import com.training.eshop.dao.CommentDAO;
import com.training.eshop.model.Comment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@AllArgsConstructor
public class CommentDAOImpl implements CommentDAO {

    private static final String QUERY_SELECT_FROM_COMMENT_BY_ORDER_ID = "from Comment c where c.order.id =:id";
    private static final String QUERY_SELECT_FROM_COMMENT_BY_ORDER_ID_ORDERED_BY_DATE = "from Comment c where c.order.id =:id order by date DESC";

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public void save(Comment comment) {
        entityManager.persist(comment);
    }

    @Override
    public List<Comment> getLastFiveByOrderId(Long id) {
        return entityManager.createQuery(QUERY_SELECT_FROM_COMMENT_BY_ORDER_ID_ORDERED_BY_DATE, Comment.class)
                .setParameter("id", id)
                .setMaxResults(5)
                .getResultList();
    }

    @Override
    public List<Comment> getAllByOrderId(Long id) {
        return entityManager.createQuery(QUERY_SELECT_FROM_COMMENT_BY_ORDER_ID, Comment.class)
                .setParameter("id", id)
                .getResultList();
    }
}
