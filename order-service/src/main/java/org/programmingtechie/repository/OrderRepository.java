package org.programmingtechie.repository;

import java.util.List;
import java.util.Optional;

import org.programmingtechie.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByCustomerId(String customerId);
    List<Order> findByStatusCheckout(String statusCheckout);
    List<Order> findByStatusHandle(String statusHandle);
    List<Order> findByPaymentMethod(String paymentMethod);
}

