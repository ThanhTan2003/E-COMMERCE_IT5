package org.programmingtechie.repository;

import java.util.Optional;

import org.programmingtechie.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, String> {
    Optional<OrderDetail> findByOrderId(String id);
}