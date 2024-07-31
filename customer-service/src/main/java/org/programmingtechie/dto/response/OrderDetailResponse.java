package org.programmingtechie.dto.response;

import org.programmingtechie.displayformat.CustomDoubleDeserializer;
import org.programmingtechie.displayformat.CustomDoubleSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderDetailResponse {
    private String id;

    private String productId;

    private String productName;

    @JsonSerialize(using = CustomDoubleSerializer.class)
    @JsonDeserialize(using = CustomDoubleDeserializer.class)
    private Double price;

    private Integer quantity;

    @JsonSerialize(using = CustomDoubleSerializer.class)
    @JsonDeserialize(using = CustomDoubleDeserializer.class)
    private Double totalAmount;
}
