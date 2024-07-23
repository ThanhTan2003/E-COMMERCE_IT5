package org.programmingtechie.dto.response;

import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InventoryResponse
{
    private String id;

    private String productId;

    private String productName;

    private Integer quantity;

    private Boolean isInStock;

    @PrePersist
    private void productName() {
        if (this.productName == null) {
            this.productName = "Chưa xác định";
        }
    }
}
