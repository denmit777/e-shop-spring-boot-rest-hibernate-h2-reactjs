package com.training.eshop.service.impl;

import com.training.eshop.converter.GoodConverter;
import com.training.eshop.dao.UserDAO;
import com.training.eshop.dto.GoodAdminCreationDto;
import com.training.eshop.dto.GoodAdminViewDto;
import com.training.eshop.dto.GoodBuyerDto;
import com.training.eshop.exception.AccessDeniedException;
import com.training.eshop.exception.ProductNotFoundException;
import com.training.eshop.model.Good;
import com.training.eshop.model.User;
import com.training.eshop.model.enums.Role;
import com.training.eshop.service.GoodService;
import com.training.eshop.dao.GoodDAO;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GoodServiceImpl implements GoodService {
    private static final Logger LOGGER = LogManager.getLogger(GoodServiceImpl.class.getName());

    private static final Map<String, List<Good>> SORT_MAP = new HashMap<>();

    private static final String PRODUCT_NOT_FOUND = "Product with title %s and price %s $ not found";
    private static final String ACCESS_DENIED_FOR_BUYER = "Access is allowed only for administrator";
    private static final String DOT = ".";
    private static final String ZERO = "0";
    private static final String TWO_ZEROS_LEFT = ".00";

    private final GoodDAO goodDAO;
    private final UserDAO userDAO;
    private final GoodConverter goodConverter;

    @Override
    @Transactional
    public Good save(GoodAdminCreationDto goodDto, String login) {
        Good good = goodConverter.fromGoodAdminCreationDto(goodDto);

        User user = userDAO.getByLogin(login);

        checkAccess(user);

        good.setUser(user);

        goodDAO.save(good);

        return good;
    }

    @Override
    @Transactional
    public List<GoodBuyerDto> getAllForBuyer() {
        return goodDAO.getAll()
                .stream()
                .map(goodConverter::convertToGoodBuyerDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<GoodAdminViewDto> getAllForAdmin(String searchField, String parameter, String sortField,
                                                 String sortDirection, int pageSize, int pageNumber) {
        List<Good> goods = getPage(getDirectionSort(goodDAO.getAllBySearch(searchField, parameter),
                sortField, sortDirection), pageSize, pageNumber);

        LOGGER.info("All goods : {}", goods);

        return goods.stream()
                .map(goodConverter::convertToGoodAdminViewDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GoodAdminViewDto getById(Long id) {
        Good good = goodDAO.getById(id);

        return goodConverter.convertToGoodAdminViewDto(good);
    }

    @Override
    @Transactional
    public Good getByTitleAndPrice(String title, String price) {
        return goodDAO.getAll().stream()
                .filter(good -> title.equals(good.getTitle())
                        && getPriceFromDropMenu(price).equals(String.valueOf(good.getPrice())))
                .findAny()
                .orElseThrow(() -> new ProductNotFoundException(String.format(PRODUCT_NOT_FOUND, title, price)));
    }

    @Override
    @Transactional
    public Good update(Long id, GoodAdminCreationDto goodDto, String login) {
        Good good = goodConverter.fromGoodAdminCreationDto(goodDto);

        User user = userDAO.getByLogin(login);

        checkAccess(user);

        good.setUser(user);
        good.setId(id);

        goodDAO.update(good);

        if (id > 0) {
            LOGGER.info("Updated good: {}", good);
        } else {
            LOGGER.info("New good: {}", goodDto);
        }

        return good;
    }

    @Override
    @Transactional
    public void deleteById(Long id, String login) {
        User user = userDAO.getByLogin(login);

        checkAccess(user);

        goodDAO.deleteById(id);

        LOGGER.info("Goods after removing good with id = {} : {}", id, getSortedGoods(goodDAO.getAll(), "id"));
    }

    @Override
    @Transactional
    public int getTotalAmount() {
        return goodDAO.getAll().size();
    }

    @Override
    public String getPriceFromDropMenu(String price) {
        if (price.contains(DOT)) {
            return isNumbersAfterDotLengthMoreThanOne(price) ? price : price + ZERO;
        }

        return price + TWO_ZEROS_LEFT;
    }

    private boolean isNumbersAfterDotLengthMoreThanOne(String price) {
        String numbersAfterDot = price.substring(price.indexOf(DOT) + 1);

        return numbersAfterDot.length() > 1;
    }

    private List<Good> getSortedGoods(List<Good> goods, String sortField) {
        SORT_MAP.put("id", goods.stream()
                .sorted(Comparator.comparing(Good::getId))
                .collect(Collectors.toList()));
        SORT_MAP.put("title", goods.stream()
                .sorted(Comparator.comparing(Good::getTitle))
                .collect(Collectors.toList()));
        SORT_MAP.put("price", goods.stream()
                .sorted(Comparator.comparing(Good::getPrice))
                .collect(Collectors.toList()));
        SORT_MAP.put("default", goods.stream()
                .sorted(Comparator.comparing(Good::getId))
                .collect(Collectors.toList()));

        return SORT_MAP.entrySet().stream()
                .filter(pair -> pair.getKey().equals(sortField))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<Good> getDirectionSort(List<Good> goods, String sortField, String sortDirection) {
        if (sortDirection.equals("desc")) {
            List<Good> reversedGoods = getSortedGoods(goods, sortField);

            Collections.reverse(reversedGoods);

            return reversedGoods;
        }

        return getSortedGoods(goods, sortField);
    }

    private List<Good> getPage(List<Good> goods, int pageSize, int pageNumber) {
        List<List<Good>> pages = new ArrayList<>();

        for (int i = 0; i < goods.size(); i += pageSize) {
            List<Good> page = new ArrayList<>(goods.subList(i, Math.min(goods.size(), i + pageSize)));

            pages.add(page);
        }

        if (!goods.isEmpty() && pageNumber - 1 <= goods.size() / pageSize) {
            return pages.get(pageNumber - 1);
        } else {
            return Collections.emptyList();
        }
    }

    private void checkAccess(User user) {
        if (user.getRole().equals(Role.ROLE_BUYER)) {
            throw new AccessDeniedException(ACCESS_DENIED_FOR_BUYER);
        }
    }
}
