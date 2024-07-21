package org.programmingtechie.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CustomerOrderList
{
    private CustomerResponse customerResponse;

    private List<OrderResponse> orderResponses;
}
