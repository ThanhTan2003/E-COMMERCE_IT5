package org.programmingtechie.repository;

import java.util.*;

import org.programmingtechie.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ProductRepository extends JpaRepository<Product, String> {
    // Tìm sản phẩm theo tên
    Optional<Product> findByName(String name);

    // Liệt kê sản phẩm theo mã loại sản phẩm
    List<Product> findByCategoryId(String categoryId);

    // Liệt kê sản phẩm theo tên loại sản phẩm
    List<Product> findByCategoryName(String categoryName);

    // Liệt kê sản phẩm theo trạng thái kinh doanh
    List<Product> findByStatusBusiness(String statusBusiness);

    // Kiểm tra tên sản phẩm bị trùng
    boolean existsByName(String name);
}
