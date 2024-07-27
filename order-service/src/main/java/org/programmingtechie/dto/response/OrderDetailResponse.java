package org.programmingtechie.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.programmingtechie.DisplayFormat.CustomDoubleSerializer;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderDetailResponse {
    private String id;

    private String productId;

    private String productName;

    @JsonSerialize(using = CustomDoubleSerializer.class)
    private Double price;

    private Integer quantity;

    @JsonSerialize(using = CustomDoubleSerializer.class)
    private Double totalAmount;
}
