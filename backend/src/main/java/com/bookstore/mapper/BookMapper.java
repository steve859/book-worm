package com.bookstore.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import com.bookstore.dto.request.BookCreationRequest;
import com.bookstore.dto.request.BookUpdateRequest;
import com.bookstore.dto.response.BookResponse;
import com.bookstore.entity.Books;
import com.bookstore.service.ParameterService;

@Mapper(componentModel = "spring", uses = { AuthorMapper.class, CategoryMapper.class })
public abstract class BookMapper {
    @Autowired
    protected ParameterService parameterService;

    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "categories", ignore = true)
    public abstract Books toBook(BookCreationRequest request);

    // @Mapping(target = "authors", ignore = true)
    // @Mapping(target = "categories", ignore = true)
    // Books toBook(BookUpdateRequest request);

    @Mapping(target = "sellPrice", expression = "java(calculateSellPrice(book.getImportPrice()))")
    public abstract BookResponse toBookResponse(Books book);

    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "categories", ignore = true)
    public abstract void updateBook(@MappingTarget Books book, BookUpdateRequest request);

    protected BigDecimal calculateSellPrice(BigDecimal importPrice) {
        if (importPrice == null) {
            return BigDecimal.ZERO;
        }
        double sellingPriceRatio = 1.05; // Default value
        try {
            sellingPriceRatio = parameterService.getParamValue("sell_price_ratio");
        } catch (RuntimeException e) {
            // Log or handle the case where the parameter is not found, using default
            System.out.println("Could not find 'sell_price_ratio' parameter, using default value 1.05");
        }
        return importPrice.multiply(BigDecimal.valueOf(sellingPriceRatio)).setScale(2, RoundingMode.HALF_UP);
    }
}
