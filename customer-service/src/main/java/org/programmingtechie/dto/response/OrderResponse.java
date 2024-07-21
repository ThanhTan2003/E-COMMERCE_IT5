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

    private String customer_id;

    private LocalDateTime date;

    private String status_handle;

    private String status_checkout;

    private String payment_Method;

    private double totalAmount;

    private double discount;

    private double total;

    private String note;

}
