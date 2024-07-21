package org.programmingtechie.controller;

import java.util.List;

import org.programmingtechie.dto.ProductRequest;
import org.programmingtechie.model.Category;
import org.programmingtechie.model.Product;
import org.programmingtechie.service.ProductServiceV1;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductControllerV1 {
    final ProductServiceV1 productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createProduct(@RequestBody ProductRequest productRequest)
    {
        productService.createProduct(productRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Product> getAllProduct()
    {
        return productService.getAllProduct();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Product getCustomerById(@PathVariable String id) {
        return productService.getProductById(id);
    }

    @PostMapping("/name")
    @ResponseStatus(HttpStatus.OK)
    public Product getProductByName(@RequestBody String name) {
        return productService.getProductByName(name);
    }

    @PostMapping("/statusBusiness")
    @ResponseStatus(HttpStatus.OK)
    public Product getCategoryByStatusBusiness(@RequestBody String statusBusiness) {
        return productService.getProductByStatusBusiness(statusBusiness);
    }

    @PostMapping("/statusInStock")
    @ResponseStatus(HttpStatus.OK)
    public Product geProductByStatusInStock(@RequestBody String statusInStock) {
        return productService.getProductByStatusInStock(statusInStock);
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String updateProduct(@PathVariable String id, @RequestBody ProductRequest productRequest) {
        productService.updateProduct(id, productRequest);
        return String.format("Product %s is updated", id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String deleteCustomer(@PathVariable String id) {
        productService.deleteProduct(id);
        return "Xóa thông tin sản phẩm thành công!";
    }
}
