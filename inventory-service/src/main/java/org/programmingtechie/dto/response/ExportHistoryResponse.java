package org.programmingtechie.dto.response;

import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExportHistoryResponse {
    private String id;

    private String productId;

    private String productName;

    private Integer quantity;

    private LocalDateTime date;

    @PrePersist
    private void productName() {
        if (this.productName == null) {
            this.productName = "Chưa xác định";
        }
    }
}
