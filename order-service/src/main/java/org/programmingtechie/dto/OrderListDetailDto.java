package org.programmingtechie.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListDetailDto {

    private String id;
    private String orderId;
    private String productId;
    private String productName;
    private Double price;
    private Integer quantity;
    private Double totalAmount;
}
