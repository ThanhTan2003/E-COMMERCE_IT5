package org.programmingtechie.repository;

import org.programmingtechie.model.ExportHistory;
import org.programmingtechie.model.ImportHistory;
import org.programmingtechie.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExportHistoryRepository extends JpaRepository<ExportHistory, String> {
    List<ExportHistory> findByProductId(String product_id);

    @Query("SELECT DISTINCT e.productId FROM ExportHistory e")
    List<String> findDistinctProductIds();
}