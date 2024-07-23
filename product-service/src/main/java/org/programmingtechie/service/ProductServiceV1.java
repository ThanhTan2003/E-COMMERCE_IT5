package org.programmingtechie.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.programmingtechie.dto.InventoryResponse;
import org.programmingtechie.dto.ProductRequest;
import org.programmingtechie.dto.ProductResponse;
import org.programmingtechie.model.Category;
import org.programmingtechie.model.Product;
import org.programmingtechie.repository.CategoryRepository;
import org.programmingtechie.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceV1 {
    final ProductRepository productRepository;
    final CategoryRepository categoryRepository;

    final WebClient.Builder webClientBuilder;

    public void createProduct(ProductRequest productRequest) {
        validCheckProductRequest(productRequest);
        Product product = Product.builder()
                .name(productRequest.getName())
                .categoryId(productRequest.getCategoryId())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();

        productRepository.save(product);

        log.info("Product {} is saved", product.getId());
    }

    void validCheckProductRequest(ProductRequest productRequest) {
        if (productRequest.getName() == null || productRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên sản phẩm!");
        }
        if (productRequest.getPrice() <= 0) {
            throw new IllegalArgumentException("Giá tiền sản phẩm phải lớn hơn 0!");
        }
    }

    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    List<InventoryResponse> checkProductInStock(List<String> productIds) {
        try {
            InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/inventory/is_in_stock")
                            .queryParam("list_product_id", String.join(",", productIds))
                            // Sử dụng String.join để nối
                            // các ID với dấu phẩy
                            .build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            if (inventoryResponses == null) {
                throw new IllegalStateException("Không thể kiểm tra sản phẩm. Vui lòng thử lại sau.");
            }

            for (InventoryResponse inventoryResponse : inventoryResponses) {
                if (!inventoryResponse.getIsInStock()) {
                    throw new IllegalStateException(
                            String.format("Sản phẩm có mã id %s không tồn tại. Vui lòng kiểm tra lại!",
                                    inventoryResponse.getId()));
                }
            }
            return Arrays.asList(inventoryResponses);
        } catch (WebClientException e) {
            log.info("ERROR - Xảy ra lỗi khi giao tiếp với inventory-service: {}", e.getMessage());

            throw new IllegalArgumentException(
                    "Dịch vụ quản lý sản phẩm (product-service) không khả dụng. Vui lòng kiểm tra hoặc thử lại sau!");
        } catch (Exception e) {
            log.info("ERROR - Xảy ra lỗi khi giao tiếp với inventory-service: {}", e.getMessage());

            throw new IllegalArgumentException("Có lỗi xảy ra khi kiểm tra sản phẩm. Vui lòng thử lại sau!");
        }
    }

    public Product getProductByName(String name) {
        Optional<Product> optionalProduct = productRepository.findByName(name);
        if (optionalProduct.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy thông tin sản phẩm! " + name);
        }
        return optionalProduct.get();
    }

    public Product getProductByStatusBusiness(String statusBusiness) {
        Optional<Product> optionalProduct = productRepository.findByStatusBusiness(statusBusiness);
        if (optionalProduct.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu!");
        }
        return optionalProduct.get();
    }

    public Product getProductById(String id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy thông tin sản phẩm!");
        }
        return optionalProduct.get();
    }

    public List<Product> getProductByCategoryId(String categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        if (products.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy thông tin sản phẩm!");
        }
        return products;
    }

    public void updateProduct(String id, ProductRequest productRequest) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setName(productRequest.getName());
            product.setCategoryId(productRequest.getCategoryId());
            product.setDescription(productRequest.getDescription());
            product.setPrice(productRequest.getPrice());
            product.setStatusBusiness(productRequest.getStatusBusiness());

            productRepository.save(product);

            log.info("Product {} is updated", product.getId());
        } else {
            log.error("Product with ID {} not found", id);
            throw new IllegalArgumentException("Product with ID " + id + " not found");
        }
    }

    public void deleteProduct(String id) {
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (optionalProduct.isPresent()) {
            productRepository.deleteById(id);

            log.info("Product {} is deleted", id);
        } else {
            log.error("Product with ID {} not found", id);
            throw new IllegalArgumentException("Product with ID " + id + " not found");
        }
    }

    @Transactional(readOnly = true)
    public ProductResponse isExisting(String id) {
        Optional<Product> product = productRepository.findById(id);

        if (product.isEmpty()) {
            return ProductResponse.builder()
                    .isExisting(false)
                    .build();
        } else {
            Product prod = product.get();
            Category category = categoryRepository.findById(prod.getCategoryId()).get();
            return ProductResponse.builder()
                    .id(prod.getId())
                    .name(prod.getName())
                    .categoryId(prod.getCategoryId())
                    .categoryName(category.getName())
                    .description(prod.getDescription())
                    .price(prod.getPrice())
                    .statusBusiness(prod.getStatusBusiness())
                    .build();
        }
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> isExisting(List<String> ids) {
        List<Product> products = productRepository.findAllById(ids);

        List <ProductResponse> productResponses = new ArrayList<>();

        for(Product product : products)
        {
            if(product == null)
            {
                ProductResponse productResponse = ProductResponse.builder()
                        .isExisting(false).build();
                productResponses.add(productResponse);
            }
            else {
                Optional<Category> category = categoryRepository.findById(product.getCategoryId());
                String categoryName = category.isEmpty() ? "Không tồn tại" : category.get().getName();

                ProductResponse productResponse = ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .categoryId(product.getCategoryId())
                        .categoryName(categoryName)
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .statusBusiness(product.getStatusBusiness())
                        .isExisting(true)
                        .build();
                productResponses.add(productResponse);
            }

        }
        return productResponses;
    }

    // @Transactional(readOnly = true)
    // public List<ProductResponse> isExisting(List<String> id) {
    //     List<Product> products = productRepository.findAllById(id);
    //     List<ProductResponse> productResponses = new ArrayList<>();

    //     int index = 0;

    //     for (ProductResponse productExistingResponse : productResponses) {
    //         if (products.get(index) == null) {
    //             productExistingResponse.setIsExisting(false);
    //         } else {
    //             productExistingResponse.setId(products.get(index).getId());
    //             productExistingResponse.setName(products.get(index).getName());
    //             productExistingResponse.setCategoryId(products.get(index).getCategoryId());

    //             Category category = categoryRepository.findById(products.get(index).getCategoryId()).get();

    //             productExistingResponse.setCategoryName(category != null ? category.getName()
    //                     : "Chưa xác định");

    //             productExistingResponse.setIsExisting(true);
    //             productExistingResponse.setStatusBusiness(products.get(index).getStatusBusiness());

    //             index++;

    //         }
    //     }

    //     return productResponses;
    // }
}
