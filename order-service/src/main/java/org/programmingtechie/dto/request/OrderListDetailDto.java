package org.programmingtechie.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListDetailDto {

    private String productId;

    private Integer quantity;

    //---------------------------------------------------

    private String id;

    private String orderId;

    private String productName;

    private Double price;

    private Double totalAmount;
}
