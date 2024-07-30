package org.programmingtechie.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryListProductsResponse {
    private CategoryResponse category;

    private Integer quantity;

    List<ProductResponse> productList;
}
