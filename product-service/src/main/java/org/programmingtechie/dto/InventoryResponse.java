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

    private String productId;

    private String productName;

    private String categoryName;

    private Integer quantity;

    private Boolean isInStock;

    @PrePersist
    private void productName() {
        if (this.productName == null) {
            this.productName = "Chưa xác định";
        }
        isInStock = (quantity > 0);
    }
}
