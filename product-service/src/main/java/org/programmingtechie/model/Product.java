package org.programmingtechie.model;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "product")
@Entity
public class Product {
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    private BigDecimal price;

    @Column(nullable = false, length = 100)
    private String statusBusiness;

    @Column(nullable = false, length = 100)
    private String statusInStock;

    @Column(length = 36)
    private String categoryId;

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if(this.statusBusiness==null){
            this.statusBusiness="Đang kinh doanh";
        }
        if(this.statusInStock==null){
            this.statusInStock="Còn hàng";
        }
    }
}

