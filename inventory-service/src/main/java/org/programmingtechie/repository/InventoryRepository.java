package org.programmingtechie.repository;

import org.programmingtechie.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, String> {
    Optional<Inventory> findByProductId(String product_id);

    List<Inventory> findByProductId(List<String> product_id);
}
