package com.training.eshop.dao;

import com.training.eshop.model.Feedback;

import java.util.List;

public interface FeedbackDAO {

    void save(Feedback feedBack);

    Feedback getByFeedbackIdAndOrderId(Long feedbackId, Long orderId);

    List<Feedback> getLastFiveByOrderId(Long id);

    List<Feedback> getAllByOrderId(Long id);
}
