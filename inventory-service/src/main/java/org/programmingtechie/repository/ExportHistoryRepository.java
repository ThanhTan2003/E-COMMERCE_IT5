package org.programmingtechie.repository;

import org.programmingtechie.model.ExportHistory;
import org.programmingtechie.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExportHistoryRepository extends JpaRepository<ExportHistory, String> {
    Optional<Inventory> findByProductId(String product_id);
}
