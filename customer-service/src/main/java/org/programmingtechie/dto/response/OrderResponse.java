package org.programmingtechie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderResponse
{
    private String id;

    private String customerId;

    private LocalDateTime date;

    private String statusHandle;

    private String statusCheckout;

    private String paymentMethod;

    private double totalAmount;

    private double discount;

    private double total;

    private String note;

}
