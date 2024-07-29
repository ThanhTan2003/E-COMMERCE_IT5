package org.programmingtechie.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.programmingtechie.DisplayFormat.CustomDoubleSerializer;

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

    @JsonSerialize(using = CustomDoubleSerializer.class)
    private Double price;

    private String statusBusiness;

    private Boolean isExisting;
}