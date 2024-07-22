package org.programmingtechie.repository;

import org.programmingtechie.model.ImportHistory;
import org.programmingtechie.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ImportHistoryRepository extends JpaRepository<ImportHistory, String> {
    Optional<ImportHistory> findByProductId(String product_id);

    @Query("SELECT DISTINCT e.product_id FROM ImportHistory e")
    List<String> findDistinctProductIds();
}
