package com.training.eshop.dao;

import com.training.eshop.model.Comment;

import java.util.List;

public interface CommentDAO {

    void save(Comment comment);

    List<Comment> getLastFiveByOrderId(Long id);

    List<Comment> getAllByOrderId(Long id);
}
