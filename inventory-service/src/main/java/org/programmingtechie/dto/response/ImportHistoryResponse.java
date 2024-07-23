package org.programmingtechie.dto.response;

import jakarta.persistence.Entity;
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
public class ImportHistoryResponse
{
    private String id;

    private String productId;

    private String productName;

    private String categoryName;

    private Integer quantity;

    private LocalDateTime date;

    private String note;

    @PrePersist
    private void productName() {
        if (this.productName == null) {
            this.productName = "Chưa xác định";
        }
    }
}
