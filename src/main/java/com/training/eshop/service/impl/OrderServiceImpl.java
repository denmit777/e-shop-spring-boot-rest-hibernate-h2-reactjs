package com.training.eshop.service.impl;

import com.training.eshop.converter.OrderConverter;
import com.training.eshop.dao.GoodDAO;
import com.training.eshop.dao.UserDAO;
import com.training.eshop.dto.GoodBuyerDto;
import com.training.eshop.dto.OrderAdminViewDto;
import com.training.eshop.dto.OrderBuyerDto;
import com.training.eshop.exception.OrderNotPlacedException;
import com.training.eshop.exception.ProductNotFoundException;
import com.training.eshop.exception.ProductNotSelectedException;
import com.training.eshop.mail.service.EmailService;
import com.training.eshop.model.Good;
import com.training.eshop.model.Order;
import com.training.eshop.model.User;
import com.training.eshop.service.GoodService;
import com.training.eshop.service.HistoryService;
import com.training.eshop.service.OrderService;
import com.training.eshop.dao.OrderDAO;
import com.training.eshop.util.comparator.OrderUserComparator;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final Logger LOGGER = LogManager.getLogger(OrderServiceImpl.class.getName());

    private static final Map<String, List<Order>> SORT_MAP = new HashMap<>();

    private static final String ORDER_NOT_PLACED = "Your order not placed yet";
    private static final String PRODUCT_NOT_SELECTED = "You should select the product first";
    private static final String PRODUCT_NOT_FOUND = "Product with title %s and price %s $ is not in the cart";
    private static final String PRODUCT_IS_OVER = "Product with title %s and price %s $ out of stock";

    private final List<Good> goods = new ArrayList<>();

    private final OrderDAO orderDAO;
    private final EmailService emailService;
    private final GoodService goodService;
    private final HistoryService historyService;
    private final OrderConverter orderConverter;
    private final UserDAO userDAO;
    private final GoodDAO goodDAO;

    @Override
    @Transactional
    public Order save(Order order, String login) {
        setOrderParameters(order, login);

        if (!order.getGoods().isEmpty()) {
            orderDAO.save(order);

            historyService.saveHistoryForCreatedOrder(order);

            emailService.sendOrderDetailsMessage(order.getId(), login);

            LOGGER.info("New order: {}", order);

            return order;
        } else {
            throw new OrderNotPlacedException(ORDER_NOT_PLACED);
        }
    }

    @Override
    @Transactional
    public void addGoodToOrder(GoodBuyerDto goodBuyerDto) {
        if (!goodBuyerDto.getTitle().isEmpty()) {
            Good good = goodService.getByTitleAndPrice(goodBuyerDto.getTitle(), String.valueOf(goodBuyerDto.getPrice()));

            Good orderGood = new Good();

            Long last = good.getQuantity() - 1L;
            Long amount = good.getQuantity() - last;

            if (good.getQuantity() >= 1) {
                setOrderGoodParameters(good, orderGood, amount);

                goods.add(orderGood);

                historyService.saveHistoryForAddedGoods(good);
            } else {
                LOGGER.error(String.format(PRODUCT_IS_OVER, goodBuyerDto.getTitle(), goodBuyerDto.getPrice()));

                throw new ProductNotFoundException(String.format(PRODUCT_IS_OVER, goodBuyerDto.getTitle(), goodBuyerDto.getPrice()));
            }

            good.setQuantity(last);

            LOGGER.info("Your goods: {}", goods);
        } else {
            LOGGER.error(PRODUCT_NOT_SELECTED);

            throw new ProductNotSelectedException(PRODUCT_NOT_SELECTED);
        }
    }

    @Override
    @Transactional
    public void deleteGoodFromOrder(GoodBuyerDto goodBuyerDto) {
        if (!goodBuyerDto.getTitle().isEmpty()) {
            Good good = goodService.getByTitleAndPrice(goodBuyerDto.getTitle(), String.valueOf(goodBuyerDto.getPrice()));

            if (isProductPresent(goodBuyerDto)) {
                Good orderGood = new Good();

                Long last = good.getQuantity() + 1L;

                setOrderGoodParameters(good, orderGood, 1L);

                goods.remove(orderGood);

                historyService.saveHistoryForRemovedGoods(good);

                good.setQuantity(last);

                LOGGER.info("Your goods after removing {} : {}", goodBuyerDto.getTitle(), goods);
            } else {
                LOGGER.error(String.format(PRODUCT_NOT_FOUND, goodBuyerDto.getTitle(), goodBuyerDto.getPrice()));

                throw new ProductNotFoundException(String.format(PRODUCT_NOT_FOUND, goodBuyerDto.getTitle(), goodBuyerDto.getPrice()));
            }
        } else {
            LOGGER.error(PRODUCT_NOT_SELECTED);

            throw new ProductNotSelectedException(PRODUCT_NOT_SELECTED);
        }
    }

    @Override
    @Transactional
    public OrderBuyerDto getById(Long id) {
        Order order = orderDAO.getById(id);

        LOGGER.info("Order № {} : {}", id, order);

        return orderConverter.convertToOrderBuyerDto(order);
    }

    @Override
    @Transactional
    public List<OrderAdminViewDto> getAll(String sortField, String sortDirection, int pageSize, int pageNumber) {
        List<Order> orders = getPage(getDirectionSort(orderDAO.getAll(), sortField, sortDirection),
                pageSize, pageNumber);

        LOGGER.info("All orders : {}", orders);

        return orders.stream()
                .map(orderConverter::convertToOrderAdminViewDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<Good> getCartGoods() {
        return goods;
    }

    @Override
    @Transactional
    public void updateDataForCancelledOrder(Order order) {
        for (Good orderGood : goods) {
            for (Good good : goodDAO.getAll()) {
                setGoodQuantityIfOrderIsCancelled(good, orderGood);
            }
        }
        historyService.saveHistoryForCanceledOrder();

        goods.clear();

        order.setGoods(goods);
    }

    @Override
    @Transactional
    public void updateDataAfterPlacingOrder(OrderBuyerDto orderBuyerDto) {
        for (Good good : goodDAO.getAll()) {
            if (good.getQuantity() < 1L) {
                goodDAO.deleteById(good.getId());
            }
        }
        goods.clear();

        orderBuyerDto.setGoods(orderConverter.convertToListGoodDto(goods));
    }

    @Override
    public int getTotalAmount() {
        return orderDAO.getAll().size();
    }

    private String getOrderedGoods(Order order) {
        StringBuilder sb = new StringBuilder();

        int count = 1;

        for (Good good : order.getGoods()) {
            sb.append(count)
                    .append(") ")
                    .append(good.getTitle())
                    .append(" ")
                    .append(good.getPrice())
                    .append(" $\n");

            count++;
        }

        return sb.append("\nTotal: $ ").append(getTotalPrice(order)).toString();
    }

    private BigDecimal getTotalPrice(Order order) {
        BigDecimal count = BigDecimal.valueOf(0);

        for (Good good : order.getGoods()) {
            count = count.add(good.getPrice());
        }

        LOGGER.info("Total price: {}", count);

        return count;
    }

    private List<Order> getSortedOrders(List<Order> orders, String sortField) {
        SORT_MAP.put("id", orders.stream()
                .sorted(Comparator.comparing(Order::getId))
                .collect(Collectors.toList()));
        SORT_MAP.put("totalPrice", orders.stream()
                .sorted(Comparator.comparing(Order::getTotalPrice))
                .collect(Collectors.toList()));
        SORT_MAP.put("user", orders.stream()
                .sorted(Comparator.comparing(Order::getUser, new OrderUserComparator()))
                .collect(Collectors.toList()));
        SORT_MAP.put("default", orders.stream()
                .sorted(Comparator.comparing(Order::getId))
                .collect(Collectors.toList()));

        return SORT_MAP.entrySet().stream()
                .filter(pair -> pair.getKey().equals(sortField))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<Order> getDirectionSort(List<Order> orders, String sortField, String sortDirection) {
        if (sortDirection.equals("desc")) {
            List<Order> reversedOrders = getSortedOrders(orders, sortField);

            Collections.reverse(reversedOrders);

            return reversedOrders;
        }

        return getSortedOrders(orders, sortField);
    }

    private List<Order> getPage(List<Order> orders, int pageSize, int pageNumber) {
        List<List<Order>> pages = new ArrayList<>();

        for (int i = 0; i < orders.size(); i += pageSize) {
            List<Order> page = new ArrayList<>(orders.subList(i, Math.min(orders.size(), i + pageSize)));

            pages.add(page);
        }

        if (!orders.isEmpty() && pageNumber - 1 <= orders.size() / pageSize) {
            return pages.get(pageNumber - 1);
        } else {
            return Collections.emptyList();
        }
    }

    private boolean isProductPresent(GoodBuyerDto goodBuyerDto) {
        String title = goodBuyerDto.getTitle();
        String price = goodService.getPriceFromDropMenu(String.valueOf(goodBuyerDto.getPrice()));

        return goods.stream().anyMatch(good -> title.equals(good.getTitle())
                && price.equals(String.valueOf(good.getPrice())));
    }

    private void setOrderParameters(Order order, String login) {
        User user = userDAO.getByLogin(login);

        order.setUser(user);
        order.setGoods(goods);

        BigDecimal totalPrice = getTotalPrice(order);
        String description = getOrderedGoods(order);

        order.setTotalPrice(totalPrice);
        order.setDescription(description);
    }

    private void setGoodQuantityIfOrderIsCancelled(Good good, Good orderGood) {
        if (Objects.equals(orderGood.getId(), good.getId())) {
            if (good.getQuantity() < 1L) {
                good.setQuantity(orderGood.getQuantity());
            } else {
                good.setQuantity(good.getQuantity() + orderGood.getQuantity());
            }
        }
    }

    private void setOrderGoodParameters(Good good, Good orderGood, Long quantity) {
        orderGood.setId(good.getId());
        orderGood.setTitle(good.getTitle());
        orderGood.setPrice(good.getPrice());
        orderGood.setQuantity(quantity);
        orderGood.setDescription(good.getDescription());
    }
}
