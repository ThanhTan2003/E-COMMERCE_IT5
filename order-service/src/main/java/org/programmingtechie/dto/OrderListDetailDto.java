package org.programmingtechie.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListDetailDto {
    private String id;
    private String skuCode;
    private Double price;
    private Integer quantity;
}
