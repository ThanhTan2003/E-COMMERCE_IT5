package org.programmingtechie.dto;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private List<OrderListDetailDto> orderListDetailDto;
}
