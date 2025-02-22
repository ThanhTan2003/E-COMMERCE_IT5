package org.programmingtechie.controller;

import java.util.List;

import org.programmingtechie.dto.request.CategoryRequest;
import org.programmingtechie.dto.response.CategoryListProductsResponse;
import org.programmingtechie.model.Category;
import org.programmingtechie.model.Product;
import org.programmingtechie.service.CategoryServiceV1;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import lombok.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/product/category")
public class CategoryControllerV1 {

    final CategoryServiceV1 categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createCategory(@RequestBody CategoryRequest categoryRequest)
    {
        categoryService.createCategory(categoryRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Category> getAllcCategories() {
        return categoryService.getAllCategory();
    }

    @PostMapping("/name")
    @ResponseStatus(HttpStatus.OK)
    public Category getCategoryByName(@RequestBody String name) {
        return categoryService.getCategoryByName(name);
    }

    @PostMapping("/statusBusiness")
    @ResponseStatus(HttpStatus.OK)
    public List<Category> getCategoryByStatusBusiness(@RequestBody String statusBusiness) {
        return categoryService.getCategoryByStatusBusiness(statusBusiness);
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String updateCategory(@PathVariable String id, @RequestBody CategoryRequest categoryRequest) {
        categoryService.updateCategory(id, categoryRequest);
        return "Cập nhật thông tin loại sản phẩm thành công!";
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return "Xóa thông tin loại sản phẩm thành công!";
    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public String deleteCategory1(@RequestBody String id) {
        categoryService.deleteCategory(id);
        return "Xóa thông tin loại sản phẩm thành công!";
    }

    @GetMapping("/list-products/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryListProductsResponse getProductByCategoryId(@PathVariable String id) {
        return categoryService.getListProductsById(id);
    }

    @PostMapping("/list-products")
    @ResponseStatus(HttpStatus.OK)
    public CategoryListProductsResponse getProductByCategoryId1(@RequestBody String categoryId) {
        return categoryService.getListProductsById(categoryId);
    }
}
