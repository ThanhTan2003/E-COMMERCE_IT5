package org.programmingtechie.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryResponse {
    private String productId;
    private Integer quantity;
    private Boolean isInStock;
}
