package org.programmingtechie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductExistingResponse
{
    private String id;
    private String name;
    private String category_id;
    private String category_name;
    private Boolean isExisting;
    private Double price;
    private String description;
}
