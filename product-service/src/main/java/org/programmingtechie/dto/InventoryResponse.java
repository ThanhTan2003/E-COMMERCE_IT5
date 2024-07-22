package org.programmingtechie.dto;

import jakarta.persistence.PrePersist;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InventoryResponse
{
    private String id;

    private String product_id;

    private String product_name;

    private Integer quantity;

    private Boolean isInStock;

    @PrePersist
    private void productName() {
        if (this.product_name == null) {
            this.product_name = "Chưa xác định";
        }
    }
}
