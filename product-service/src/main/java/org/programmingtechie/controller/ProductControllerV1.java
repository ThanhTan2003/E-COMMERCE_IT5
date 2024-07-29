package org.programmingtechie.controller;

import java.util.List;

import org.programmingtechie.dto.request.ProductRequest;
import org.programmingtechie.dto.response.ProductResponse;
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
    public List<ProductResponse> getAllProduct()
    {
        return productService.getAllProducts();
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

    @GetMapping("/category-id/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<Product> getProductByCategoryId(@PathVariable String id) {
        return productService.getProductByCategoryId(id);
    }

    @PostMapping("/category-id")
    @ResponseStatus(HttpStatus.OK)
    public List<Product> getProductByCategoryId1(@RequestBody String categoryId) {
        return productService.getProductByCategoryId(categoryId);
    }

    @PostMapping("/status-business")
    @ResponseStatus(HttpStatus.OK)
    public List<Product> getCategoryByStatusBusiness(@RequestBody String statusBusiness) {
        return productService.getProductByStatusBusiness(statusBusiness);
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

    @GetMapping("/is-existing")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> isExisting(@RequestParam List<String> list_product_id) {
        return productService.isExisting(list_product_id);
    }

    @GetMapping("/is-existing/single")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse isExisting(@RequestParam String list_product_id) {
        return productService.isExisting(list_product_id);
    }
}
