package org.programmingtechie.repository;

import org.programmingtechie.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
    
}

