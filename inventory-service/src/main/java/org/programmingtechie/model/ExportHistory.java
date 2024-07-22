package org.programmingtechie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "export_history", indexes = {
        @Index(name = "idx_product_id", columnList = "product_id"),
        @Index(name = "idx_date", columnList = "date")
})
@Entity
public class ExportHistory {
    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 36)
    private String product_id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDateTime date;

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.date == null) {
            // Lấy thời gian hiện tại ở múi giờ UTC+7
            ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Bangkok"));
            this.date = zonedDateTime.toLocalDateTime();
        }
    }

}
