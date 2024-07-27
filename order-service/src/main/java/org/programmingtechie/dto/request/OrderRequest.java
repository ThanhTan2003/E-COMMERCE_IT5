package org.programmingtechie.dto.request;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    private String phoneNumber;

    private String statusHandle;

    private String statusCheckout;

    private Double discount;

    private String note;

    private String paymentMethod;

    //-------------------------------------------------

    private String customerId;

    private Double totalAmount;

    private Double total;

    private List<OrderListDetailDto> orderListDetailDto;
}
