package com.bookstore.dto.response;

import java.math.BigDecimal;
import java.util.Set;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookResponse {
    Integer bookId;
    String name;
    Integer publishedYear;
    BigDecimal importPrice;
    BigDecimal sellPrice;
    int quantity;
    Set<AuthorResponse> authors;
    Set<CategoryResponse> categories;
}
