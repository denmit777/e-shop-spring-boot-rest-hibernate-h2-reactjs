package com.training.eshop.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class GoodAdminCreationDto {

    private static final String TITLE_FIELD_IS_EMPTY = "Title field shouldn't be empty";
    private static final String PRICE_FIELD_IS_EMPTY = "Price field shouldn't be empty";
    private static final String QUANTITY_FIELD_IS_EMPTY = "Quantity field shouldn't be empty";
    private static final String WRONG_SIZE_OF_TITLE = "Title shouldn't be more than 15 symbols";
    private static final String WRONG_SIZE_OF_DESCRIPTION = "Description shouldn't be more than 100 symbols";
    private static final String WRONG_TITLE = "Title should be in latin letters";

    @NotBlank(message = TITLE_FIELD_IS_EMPTY)
    @Size(max = 15, message = WRONG_SIZE_OF_TITLE)
    @Pattern(regexp = "^[^А-Яа-я0-9]*$", message = WRONG_TITLE)
    private String title;

    @NotNull(message = PRICE_FIELD_IS_EMPTY)
    private BigDecimal price;

    @NotNull(message = QUANTITY_FIELD_IS_EMPTY)
    private Long quantity;

    @Size(max = 100, message = WRONG_SIZE_OF_DESCRIPTION)
    private String description;
}
