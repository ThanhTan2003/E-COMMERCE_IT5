package org.programmingtechie.controller;

import java.util.List;

import org.programmingtechie.dto.CategoryRequest;
import org.programmingtechie.model.Category;
import org.programmingtechie.service.CategoryServiceV1;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import lombok.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/category")
public class CategoryControllerV1 {

    final CategoryServiceV1 categoryService;

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
    public Category getCategoryByStatusBusiness(@RequestBody String statusBusiness) {
        return categoryService.getCategoryByStatusBusiness(statusBusiness);
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String updateCustomer(@PathVariable String id, @RequestBody CategoryRequest categoryRequest) {
        categoryService.updateCategory(id, categoryRequest);
        return "Cập nhật thông tin loại sản phẩm thành công!";
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String deleteCustomer(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return "Xóa thông tin loại sản phẩm thành công!";
    }
}
