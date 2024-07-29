package org.programmingtechie.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import org.programmingtechie.DisplayFormat.CustomDoubleSerializer;
import org.programmingtechie.dto.request.OrderListDetailDto;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderResponse {
    private String id;

    private String customerId;

    private String customerName;

    private String phoneNumber;

    private String statusHanle;

    private String statusCheckout;

    private String paymentMethod;

    private String note;

    private LocalDateTime date;

    @JsonSerialize(using = CustomDoubleSerializer.class)
    private Double totalAmount;

    @JsonSerialize(using = CustomDoubleSerializer.class)
    private Double discount;

    @JsonSerialize(using = CustomDoubleSerializer.class)
    private Double total;

    private List<OrderDetailResponse> orderDetailResponses;
}
