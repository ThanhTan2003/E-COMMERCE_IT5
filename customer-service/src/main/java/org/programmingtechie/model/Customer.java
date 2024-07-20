package org.programmingtechie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "customer")
@Entity
public class Customer {
    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(length = 100)
    private String fullName;

    @Column(length = 150)
    private String address;

    @Column(nullable = false, unique = true, length = 11)
    private String phoneNumber;

    @Column(unique = true, length = 50)
    private String email;

    private LocalDate dateOfBirth;

    @Column(length = 4)
    private String gender;

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
