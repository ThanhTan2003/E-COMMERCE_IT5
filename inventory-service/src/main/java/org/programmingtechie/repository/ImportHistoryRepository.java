package org.programmingtechie.repository;

import org.programmingtechie.model.ImportHistory;
import org.programmingtechie.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImportHistoryRepository extends JpaRepository<ImportHistory, String> {
    Optional<Inventory> findByProductId(String product_id);
}
