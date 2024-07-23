package org.programmingtechie.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductExistingResponse
{
    private String id;
    private String name;
    private String categoryId;
    private String categoryName;
    private Boolean isExisting;
    private String statusBusiness;
    private Double price;
    private String description;
}
