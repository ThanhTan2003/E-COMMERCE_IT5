package org.programmingtechie.dto;

import java.math.BigDecimal;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    private String name;
    private String categoryId;
    private String description;
    private BigDecimal price;
    private String statusBusiness;
    private String statusInStock;
}
