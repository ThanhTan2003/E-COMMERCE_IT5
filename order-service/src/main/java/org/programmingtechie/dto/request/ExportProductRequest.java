package org.programmingtechie.dto.request;

import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExportProductRequest {

    private String productId;

    private String productName;

    private Integer quantity;

    @PrePersist
    private void productName() {
        if (this.productName == null) {
            this.productName = "Chưa xác định";
        }
    }
}
