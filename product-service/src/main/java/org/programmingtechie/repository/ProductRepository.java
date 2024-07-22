package org.programmingtechie.repository;

import java.util.Optional;

import org.programmingtechie.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByName(String name);
    Optional<Product> findByStatusBusiness(String statusBusiness);
}
