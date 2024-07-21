package org.programmingtechie.service;

import java.util.List;
import java.util.Optional;

import org.programmingtechie.controller.ProductControllerV1;
import org.programmingtechie.dto.ProductRequest;
import org.programmingtechie.dto.ProductResponse;
import org.programmingtechie.model.Category;
import org.programmingtechie.model.Product;
import org.programmingtechie.repository.ProductRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceV1 {
    final ProductRepository productRepository;

    public void createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();

        productRepository.save(product);

        log.info("Product {} is saved", product.getId());
    }

    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    public Product getProductByName(String name)
    {
        Optional<Product> optionalProduct = productRepository.findByName(name);
        if(optionalProduct.isEmpty())
        {
            throw new IllegalArgumentException("Không tìm thấy thông tin sản phẩm! " + name);
        }
        return optionalProduct.get();
    }

    public Product getProductByStatusBusiness(String statusBusiness)
    {
        Optional<Product> optionalProduct = productRepository.findByStatusBusiness(statusBusiness);
        if(optionalProduct.isEmpty())
        {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu!");
        }
        return optionalProduct.get();
    }

    public Product getProductByStatusInStock(String statusInStock)
    {
        Optional<Product> optionalProduct = productRepository.findByStatusInStock(statusInStock);
        if(optionalProduct.isEmpty())
        {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu!");
        }
        return optionalProduct.get();
    }

    public Product getProductById(String id)
    {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if(optionalProduct.isEmpty())
        {
            throw new IllegalArgumentException("Không tìm thấy thông tin sản phẩm!");
        }
        return optionalProduct.get();
    }

    public void updateProduct(String id, ProductRequest productRequest)
    {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setName(productRequest.getName());
            product.setCategoryId(productRequest.getCategoryId());
            product.setDescription(productRequest.getDescription());
            product.setPrice(productRequest.getPrice());
            product.setStatusBusiness(productRequest.getStatusBusiness());
            product.setStatusInStock(productRequest.getStatusInStock());

            productRepository.save(product);

            log.info("Product {} is updated", product.getId());
        } else {
            log.error("Product with ID {} not found", id);
            throw new IllegalArgumentException("Product with ID " + id + " not found");
        }
    }

    public void deleteProduct(String id)
    {
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (optionalProduct.isPresent()) {
            productRepository.deleteById(id);

            log.info("Product {} is deleted", id);
        } else {
            log.error("Product with ID {} not found", id);
            throw new IllegalArgumentException("Product with ID " + id + " not found");
        }
    }

}
