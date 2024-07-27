package org.programmingtechie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
