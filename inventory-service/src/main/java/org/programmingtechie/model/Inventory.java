package org.programmingtechie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "inventory")
@Entity
public class Inventory {
    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(unique = true, nullable = false, length = 36)
    private String product_id;

    @Column(nullable = false)
    private Integer quantity;

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
