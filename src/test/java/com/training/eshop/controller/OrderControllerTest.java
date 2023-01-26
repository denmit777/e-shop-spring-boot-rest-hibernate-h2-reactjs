package com.training.eshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.eshop.dto.GoodBuyerDto;
import com.training.eshop.dto.OrderAdminViewDto;
import com.training.eshop.model.Good;
import com.training.eshop.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<Good> goods;

    private static final String[] CLEAN_TABLES_SQL = {
            "delete from comment",
            "delete from history",
    };

    @AfterEach
    public void resetDb() {
        for (String query : CLEAN_TABLES_SQL) {
            jdbcTemplate.execute(query);
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = {"BUYER", "ADMIN"})
    void givenGoodsList_whenAddValidProduct_thenStatus200andGoodsReturned() throws Exception {

        GoodBuyerDto goodBuyerDto = new GoodBuyerDto();

        goodBuyerDto.setTitle("Juice");
        goodBuyerDto.setPrice(BigDecimal.valueOf(2));

        goods = Arrays.asList(new Good(3L, "Juice", BigDecimal.valueOf(2), 1L, "This is a juice"));

        mockMvc.perform(
                        post("http://localhost:8081/orders?buttonValue=Add Goods")
                                .content(objectMapper.writeValueAsString(goodBuyerDto))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)

                )
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(goods)));

        orderService.deleteGoodFromOrder(goodBuyerDto);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"BUYER", "ADMIN"})
    void givenError_whenAddedProductIsOver_thenStatus404NotFound() throws Exception {
        GoodBuyerDto goodBuyerDto = new GoodBuyerDto();

        createProductCart("Phone", BigDecimal.valueOf(100), goodBuyerDto);

        goodBuyerDto.setTitle("Phone");
        goodBuyerDto.setPrice(BigDecimal.valueOf(100));

        String error = String.format("Product with title %s and price %s $ out of stock",
                goodBuyerDto.getTitle(), goodBuyerDto.getPrice());

        mockMvc.perform(
                        post("http://localhost:8081/orders?buttonValue=Add Goods")
                                .content(objectMapper.writeValueAsString(goodBuyerDto))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.info").value(error));

        orderService.deleteGoodFromOrder(goodBuyerDto);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"BUYER", "ADMIN"})
    void givenGoodsList_whenRemoveValidProduct_thenStatus200andGoodsReturned() throws Exception {
        GoodBuyerDto goodBuyerDto1 = new GoodBuyerDto();
        GoodBuyerDto goodBuyerDto2 = new GoodBuyerDto();

        createProductCart("Juice", BigDecimal.valueOf(2), goodBuyerDto1);
        createProductCart("Book", BigDecimal.valueOf(5.5), goodBuyerDto2);


        goods = Arrays.asList(new Good(1L, "Book", BigDecimal.valueOf(5.5), 1L, "This is a book"));

        mockMvc.perform(
                        post("http://localhost:8081/orders?buttonValue=Remove Goods")
                                .content(objectMapper.writeValueAsString(goodBuyerDto1))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)

                )
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(goods)));

        orderService.deleteGoodFromOrder(goodBuyerDto2);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"BUYER", "ADMIN"})
    void givenError_whenRemovedProductIsAbsentInCart_thenStatus404NotFound() throws Exception {
        GoodBuyerDto goodBuyerDto = new GoodBuyerDto();

        goodBuyerDto.setTitle("Phone");
        goodBuyerDto.setPrice(BigDecimal.valueOf(200));

        String error = String.format("Product with title %s and price %s $ is not in the cart",
                goodBuyerDto.getTitle(), goodBuyerDto.getPrice());

        mockMvc.perform(
                        post("http://localhost:8081/orders?buttonValue=Remove Goods")
                                .content(objectMapper.writeValueAsString(goodBuyerDto))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.info").value(error));

    }

    @Test
    @WithMockUser(username = "asya_mogilev@yopmail.com", roles = {"BUYER", "ADMIN"})
    void givenOrder_whenCreateValidOrder_thenStatus201andOrderReturned() throws Exception {
        GoodBuyerDto goodBuyerDto1 = new GoodBuyerDto();
        GoodBuyerDto goodBuyerDto2 = new GoodBuyerDto();

        createProductCart("Juice", BigDecimal.valueOf(2), goodBuyerDto1);
        createProductCart("Book", BigDecimal.valueOf(5.5), goodBuyerDto2);

        mockMvc.perform(
                        post("http://localhost:8081/orders?buttonValue=Submit")
                                .content(objectMapper.writeValueAsString(goodBuyerDto2))
                                .contentType(MediaType.APPLICATION_JSON)

                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.totalPrice").value(7.50))
                .andExpect(jsonPath("$.description").value("1) Juice 2.00 $\n2) Book 5.50 $\n\nTotal: $ 7.50"))
                .andExpect(jsonPath("$.goods[0].id").value(3))
                .andExpect(jsonPath("$.goods[0].title").value("Juice"))
                .andExpect(jsonPath("$.goods[0].price").value(2.0))
                .andExpect(jsonPath("$.goods[0].description").value("This is a juice"))
                .andExpect(jsonPath("$.goods[1].id").value(1))
                .andExpect(jsonPath("$.goods[1].title").value("Book"))
                .andExpect(jsonPath("$.goods[1].price").value(5.5))
                .andExpect(jsonPath("$.goods[1].description").value("This is a book"));

        orderService.deleteGoodFromOrder(goodBuyerDto2);
        orderService.deleteGoodFromOrder(goodBuyerDto1);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"BUYER", "ADMIN"})
    void givenError_whenOrderIsEmpty_thenStatus400BadRequest() throws Exception {
        GoodBuyerDto goodBuyerDto = new GoodBuyerDto();

        String error = "Your order not placed yet";

        mockMvc.perform(
                        post("http://localhost:8081/orders?buttonValue=Submit")
                                .content(objectMapper.writeValueAsString(goodBuyerDto))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.info").value(error));
    }

    @Test
    @WithMockUser(username = "asya_mogilev@yopmail.com", roles = {"BUYER", "ADMIN"})
    void givenOrder_whenGetExistingOrder_thenStatus200andOrderReturned() throws Exception {
        givenOrder_whenCreateValidOrder_thenStatus201andOrderReturned();

        long id = orderService.getTotalAmount();

        mockMvc.perform(
                        get("http://localhost:8081/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(7.50))
                .andExpect(jsonPath("$.description").value("1) Juice 2.00 $\n2) Book 5.50 $\n\nTotal: $ 7.50"))
                .andExpect(jsonPath("$.goods[0].title").value("Juice"))
                .andExpect(jsonPath("$.goods[0].price").value(2.0))
                .andExpect(jsonPath("$.goods[1].title").value("Book"))
                .andExpect(jsonPath("$.goods[1].price").value(5.5));
    }

    @Test
    @WithMockUser(username = "den_mogilev@yopmail.com", roles = {"BUYER", "ADMIN"})
    void givenError_whenGetNotExistingOrder_thenStatus404NotFound() throws Exception {
        long id = orderService.getTotalAmount();

        String error = "Order with id " + (id + 1) + " not found";

        mockMvc.perform(
                        get("http://localhost:8081/orders/{id}", id + 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.info").value(error));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void givenAllOrders_whenIAmAdmin_thenStatus200() throws Exception {
        List<OrderAdminViewDto> orders = orderService.getAll("default",
                "", 25, 1);

        mockMvc.perform(
                        get("http://localhost:8081/orders"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(orders)));
    }

    private void createProductCart(String title, BigDecimal price, GoodBuyerDto goodBuyerDto) {
        goodBuyerDto.setTitle(title);
        goodBuyerDto.setPrice(price);

        orderService.addGoodToOrder(goodBuyerDto);
    }
}