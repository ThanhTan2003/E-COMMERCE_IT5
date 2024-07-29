package org.programmingtechie.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {
    private String id;
    private String name;
    private String categoryId;
    private String categoryName;
    private String description;
    private Double price;
    private String statusBusiness;
    private Boolean isExisting;
}