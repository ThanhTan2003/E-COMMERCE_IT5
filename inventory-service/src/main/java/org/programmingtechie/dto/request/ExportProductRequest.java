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
    private String product_id;

    private String product_name;

    private Integer quantity;

    @PrePersist
    private void productName() {
        if (this.product_name == null) {
            this.product_name = "Chưa xác định";
        }
    }
}
