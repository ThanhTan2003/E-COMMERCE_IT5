package org.programmingtechie.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.programmingtechie.DisplayFormat.CustomDoubleDeserializer;
import org.programmingtechie.DisplayFormat.CustomDoubleSerializer;

import java.time.LocalDateTime;
import java.util.List;

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
    @JsonDeserialize(using = CustomDoubleDeserializer.class)
    private Double totalAmount;

    @JsonSerialize(using = CustomDoubleSerializer.class)
    @JsonDeserialize(using = CustomDoubleDeserializer.class)
    private Double discount;

    @JsonSerialize(using = CustomDoubleSerializer.class)
    @JsonDeserialize(using = CustomDoubleDeserializer.class)
    private Double total;

    private List<OrderDetailResponse> orderDetailResponses;
}
