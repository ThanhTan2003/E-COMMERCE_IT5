package org.programmingtechie.dto;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String customerId;
    private String phoneNumber;
    private String statusHandle;
    private String statusCheckout;
    private Double totalAmount;
    private Double discount;
    private Double total;
    private String note;
    private String paymentMethod;
    private List<OrderListDetailDto> orderListDetailDto;
}
