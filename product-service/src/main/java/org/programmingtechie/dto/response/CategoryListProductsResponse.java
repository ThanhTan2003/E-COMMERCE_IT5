package org.programmingtechie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.programmingtechie.model.Category;
import org.programmingtechie.model.Product;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryListProductsResponse {
    private Category category;

    private Integer quantity;

    List<Product> productList;
}
