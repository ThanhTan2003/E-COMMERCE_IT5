package org.programmingtechie.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_list")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 36)
    private String customerId;

    @Column(nullable = false, length = 100)
    private String statusHandle;

    @Column(nullable = false, length = 100)
    private String statusCheckout;

    @Column(length = 100)
    private String capacityCheckout;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private Double discount;

    @Column(nullable = false)
    private Double total;

    @Column(length = 100)
    private String note;

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.date == null) {
            ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            this.date = zonedDateTime.toLocalDateTime();
        }
        if (this.statusCheckout == null) {
            this.statusCheckout = "Chưa thanh toán";
        }
        if (this.statusHandle == null) {
            this.statusHandle = "Đã tiếp nhận";
        }
    }

    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderDetail> orderList;
}