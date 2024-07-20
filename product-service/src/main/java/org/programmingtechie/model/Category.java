package org.programmingtechie.model;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "category")
@Entity
public class Category {
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String statusBusiness;

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if(this.statusBusiness==null){
            this.statusBusiness="Đang kinh doanh";
        }
    }
}