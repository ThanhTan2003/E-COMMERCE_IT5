package org.programmingtechie.dto.request;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    private String name;
    private String categoryId;
    private String description;
    private Double price;
    private String statusBusiness;
    private String statusInStock;
}
