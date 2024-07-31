package org.programmingtechie.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.programmingtechie.dto.request.CategoryRequest;
import org.programmingtechie.dto.response.CategoryListProductsResponse;
import org.programmingtechie.dto.response.CategoryResponse;
import org.programmingtechie.dto.response.ProductResponse;
import org.programmingtechie.model.Category;
import org.programmingtechie.model.Product;
import org.programmingtechie.repository.CategoryRepository;
import org.programmingtechie.repository.ProductRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceV1 {

    final CategoryRepository categoryRepository;
    final ProductRepository productRepository;


    public void createCategory(CategoryRequest categoryRequest) {

        validCheckCategoryRequest(categoryRequest);
        if (categoryRepository.existsByName(categoryRequest.getName())) {
            throw new IllegalArgumentException("Tên loại sản phẩm tồn tại vui lòng nhập lại.");
        }
        Category category = Category.builder()
                .name(categoryRequest.getName())
                .build();

        categoryRepository.save(category);

        log.info("Category {} is saved", category.getId());

    }

    void validCheckCategoryRequest(CategoryRequest categoryRequest) {
        if (categoryRequest.getName() == null || categoryRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên loại sản phẩm!");
        }
    }

    public List<Category> getAllCategory() {
        return categoryRepository.findAll();
    }

    public Category getCategoryByName(String name) {
        Optional<Category> category = categoryRepository.findByName(name);
        if (category.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy thông tin loại sản phẩm! " + name);
        }
        return category.get();
    }

    public List<Category> getCategoryByStatusBusiness(String statusBusiness) {
        List<Category> optionalCategory = categoryRepository.findByStatusBusiness(statusBusiness);
        if (optionalCategory.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu!");
        }
        return optionalCategory;
    }

    public void updateCategory(String id, CategoryRequest categoryRequest) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (categoryRepository.existsByName(categoryRequest.getName())) {
            throw new IllegalArgumentException("Tên loại sản phẩm tồn tại vui lòng nhập lại.");
        }
        if (optionalCategory.isPresent()) {
            Category category = optionalCategory.get();
            category.setName(categoryRequest.getName());
            category.setStatusBusiness(categoryRequest.getStatusBusiness());

            categoryRepository.save(category);

            log.info("Category {} is updated", category.getId());
        } else {
            log.error("Category with ID {} not found", id);
            throw new IllegalArgumentException("Category with ID " + id + " not found");
        }
    }

    public void deleteCategory(String id) {

        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (optionalCategory.isEmpty())
            throw new IllegalArgumentException("Thể loại không hợp lệ!");

        List<Product> product = productRepository.findByCategoryId(id);
        if (!product.isEmpty())
            throw new IllegalArgumentException("Không thể xóa loại phẩm do ràng buộc dữ liệu!");

        try {
            categoryRepository.deleteById(id);

            log.info("Category {} is deleted", id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Không thể xóa loại phẩm!");
        }

    }

    public CategoryListProductsResponse getListProductsById(String id) {
        Optional<Category> category = categoryRepository.findById(id);

        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(category.get().getId())
                .name(category.get().getName())
                .statusBusiness(category.get().getStatusBusiness())
                .build();

        List<Product> productList = productRepository.findByCategoryId(id);

        List<ProductResponse> productResponses = new ArrayList<>();

        for (Product product : productList) {
            ProductResponse productResponse = ProductResponse.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .categoryId(product.getCategoryId())
                    .categoryName(category.get().getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .statusBusiness(product.getStatusBusiness())
                    .isExisting(true)
                    .build();
            productResponses.add(productResponse);
        }

        return CategoryListProductsResponse.builder()
                .category(categoryResponse)
                .quantity(productList.size())
                .productList(productResponses)
                .build();
    }
}
