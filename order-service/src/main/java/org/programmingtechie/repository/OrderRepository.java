package org.programmingtechie.repository;

import java.util.List;
import java.util.Optional;

import org.programmingtechie.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByCustomerId(String customerId);

    List<Order> findByPhoneNumber(String phoneNumber);

    List<Order> findByStatusCheckout(String statusCheckout);

    List<Order> findByStatusHandle(String statusHandle);

    List<Order> findByPaymentMethod(String paymentMethod);
}

