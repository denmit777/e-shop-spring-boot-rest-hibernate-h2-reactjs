package com.training.eshop.dao.impl;

import com.training.eshop.dao.GoodDAO;
import com.training.eshop.exception.ProductNotFoundException;
import com.training.eshop.model.Good;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class GoodDAOImpl implements GoodDAO {

    private static final Map<String, List<Good>> SEARCH_MAP = new HashMap<>();
    private static final String QUERY_SELECT_FROM_GOOD = "from Good";
    private static final String QUERY_SELECT_FROM_GOOD_FOR_BUYER = "from Good order by title";
    private static final String QUERY_SELECT_FROM_GOOD_SEARCHED_BY_ID = "from Good g where str(g.id) like concat('%',?0,'%')";
    private static final String QUERY_SELECT_FROM_GOOD_SEARCHED_BY_TITLE = "from Good g where lower(g.title) like lower(concat('%',?0,'%'))";
    private static final String QUERY_SELECT_FROM_GOOD_SEARCHED_BY_PRICE = "from Good g where str(g.price) like concat('%',?0,'%')";
    private static final String QUERY_SELECT_FROM_GOOD_SEARCHED_BY_DESCRIPTION = "from Good g where lower(g.description) like lower(concat('%',?0,'%'))";
    private static final String QUERY_DELETE_FROM_GOOD_BY_GOOD_ID = "delete from Good g where g.id =:id";
    private static final String PRODUCT_NOT_FOUND = "Good with id %s not found";

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public void save(Good good) {
        entityManager.persist(good);
    }

    @Override
    public List<Good> getAll() {
        return entityManager.createQuery(QUERY_SELECT_FROM_GOOD_FOR_BUYER, Good.class)
                .getResultList();
    }

    @Override
    public List<Good> getAllBySearch(String searchField, String searchParameter) {

        SEARCH_MAP.put("id", createQueryWithSearchedParameter(QUERY_SELECT_FROM_GOOD_SEARCHED_BY_ID, searchParameter));
        SEARCH_MAP.put("title", createQueryWithSearchedParameter(QUERY_SELECT_FROM_GOOD_SEARCHED_BY_TITLE, searchParameter));
        SEARCH_MAP.put("price", createQueryWithSearchedParameter(QUERY_SELECT_FROM_GOOD_SEARCHED_BY_PRICE, searchParameter));
        SEARCH_MAP.put("description", createQueryWithSearchedParameter(QUERY_SELECT_FROM_GOOD_SEARCHED_BY_DESCRIPTION, searchParameter));
        SEARCH_MAP.put("default", createQueryWithSearchedParameter(QUERY_SELECT_FROM_GOOD, ""));

        return SEARCH_MAP.entrySet().stream()
                .filter(pair -> pair.getKey().equals(searchField))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Good getById(Long id) {
        return getAll().stream()
                .filter(good -> id.equals(good.getId()))
                .findAny()
                .orElseThrow(() -> new ProductNotFoundException(String.format(PRODUCT_NOT_FOUND, id)));
    }

    @Override
    public void update(Good good) {
        entityManager.merge(good);
    }

    @Override
    public void deleteById(Long id) {
        entityManager.createQuery(QUERY_DELETE_FROM_GOOD_BY_GOOD_ID)
                .setParameter("id", id)
                .executeUpdate();
    }

    private List<Good> createQueryWithSearchedParameter(String searchQuery, String searchParameter) {
        Query query = entityManager.createQuery(searchQuery);

        if (!(searchQuery.equals(QUERY_SELECT_FROM_GOOD))) {
            query.setParameter(0, searchParameter);
        }

        return query.getResultList();
    }
}
