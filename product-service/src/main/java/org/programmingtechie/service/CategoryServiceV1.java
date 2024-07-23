package org.programmingtechie.service;

import java.util.List;
import java.util.Optional;

import org.programmingtechie.dto.CategoryRequest;
import org.programmingtechie.model.Category;
import org.programmingtechie.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceV1 {

    final CategoryRepository categoryRepository;

    public void createProduct(CategoryRequest categoryRequest) {
        validCheckCategoryRequest(categoryRequest);
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

    public Category getCategoryByStatusBusiness(String statusBusiness) {
        Optional<Category> optionalCategory = categoryRepository.findByStatusBusiness(statusBusiness);
        if (optionalCategory.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu!");
        }
        return optionalCategory.get();
    }

    public void updateCategory(String id, CategoryRequest categoryRequest) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
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

        if (optionalCategory.isPresent()) {
            categoryRepository.deleteById(id);

            log.info("Category {} is deleted", id);
        } else {
            log.error("Category with ID {} not found", id);
            throw new IllegalArgumentException("Category with ID " + id + " not found");
        }
    }
}
