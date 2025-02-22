package org.programmingtechie.repository;

import java.util.List;
import java.util.Optional;

import org.programmingtechie.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface CategoryRepository extends JpaRepository<Category, String> {
    Optional<Category> findByName(String name);

    List<Category> findByStatusBusiness(String statusBusiness);

    boolean existsByName(String name);
}
