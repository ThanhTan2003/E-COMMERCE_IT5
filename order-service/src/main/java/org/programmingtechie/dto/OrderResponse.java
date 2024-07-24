package org.programmingtechie.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderResponse {
    private List<OrderListDetailDto> orderListDetailDto;

    private String id;

    private String customerId;

    private String phoneNumber;

    private String statusHanle;

    private String statusCheckout;

    private String paymentMethod;

    private Double totalAmount;

    private Double discount;

    private LocalDateTime date;

    private Double total;

    private String note;
}
