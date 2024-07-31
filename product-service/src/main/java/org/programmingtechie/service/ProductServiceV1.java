package org.programmingtechie.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


import org.programmingtechie.dto.response.InventoryResponse;
import org.programmingtechie.dto.request.ProductRequest;
import org.programmingtechie.dto.response.ProductResponse;
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


    
    // Tạo mới sản phẩm
    public void createProduct(ProductRequest productRequest) {

        validCheckProductRequest(productRequest);
        if (productRepository.existsByName(productRequest.getName())) {
            throw new IllegalArgumentException("Sản phẩm với tên đã tồn tại vui lòng nhập lại.");
        }
        Product product = Product.builder()
                .name(productRequest.getName())
                .categoryId(productRequest.getCategoryId())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();

        productRepository.save(product);

        log.info("Product {} is saved", product.getId());
    }

    // Kiểm tra hợp lệ của mỗi trường nhập vào
    void validCheckProductRequest(ProductRequest productRequest) {
        if (productRequest.getName() == null || productRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên sản phẩm!");
        }
        if (productRequest.getPrice() <= 0) {
            throw new IllegalArgumentException("Giá tiền sản phẩm phải lớn hơn 0!");
        }
        if (!isNumeric(productRequest.getPrice())) {
            throw new IllegalArgumentException("Giá tiền sản phẩm phải là số!");
        }
    }

    //Kiểm tra số tiền nhập vào là kí tự số
    private boolean isNumeric(Double price) {
        try {
            Double.parseDouble(String.valueOf(price));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Lấy hết thông tin sản phẩm theo mặc định
    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    // Lấy hết thông tin sản phẩm kèm theo tên loại sản phẩm (*)
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductResponse> productResponses = new ArrayList<>();

        for (Product product : products) {
            if (product == null) {
                ProductResponse productResponse = ProductResponse.builder()
                        .isExisting(false).build();
                productResponses.add(productResponse);
            } else {
                Optional<Category> category = categoryRepository.findById(product.getCategoryId());
                String categoryName = category.isEmpty() ? "Không tồn tại" : category.get().getName();

                ProductResponse productResponse = ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .categoryId(product.getCategoryId())
                        .categoryName(categoryName)// (*)
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

    public List<Product> getProductByStatusBusiness(String statusBusiness) {
        List<Product> optionalProduct = productRepository.findByStatusBusiness(statusBusiness);
        if (optionalProduct.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu!");
        }
        return optionalProduct;
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

    public List<Product> getProductByCategoryName(String categoryName) {
        List<Product> products = productRepository.findByCategoryName(categoryName);
        if (products.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy thông tin sản phẩm!");
        }
        return products;
    }

    // Cập nhật sản phẩm
    public void updateProduct(String id, ProductRequest productRequest) {
        validCheckProductRequest(productRequest);
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (productRepository.existsByName(productRequest.getName())) {
            throw new IllegalArgumentException("Sản phẩm với tên đã tồn tại vui lòng nhập lại.");
        }
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
        Product product = optionalProduct.get();

        List<String> productIds = new ArrayList<>();
        productIds.add(product.getId());
        try {
            List<InventoryResponse> inventoryResponses = checkProductInStock(productIds);
            if (!inventoryResponses.isEmpty())
                throw new IllegalArgumentException("Can not delete this product");

            if (optionalProduct.isPresent() && product.getStatusBusiness().equals("Đang kinh doanh"))
                throw new IllegalArgumentException("Can not delete this product");

            productRepository.delete(product);

        } catch (Exception e) {
            log.error("Product with ID {} not found", id);
            throw new IllegalArgumentException("Product with ID " + id + " not found");
        }
    }

    // Kiểm tra sản phẩm có sẵn trong danh mục hay không?
    @Transactional(readOnly = true)
    public ProductResponse isExisting(String id) {
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (optionalProduct.isEmpty()) {
            return ProductResponse.builder()
                    .isExisting(false)
                    .build();
        } else {
            Product product = optionalProduct.get();
            Category category = categoryRepository.findById(product.getCategoryId()).get();
            return ProductResponse.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .categoryId(product.getCategoryId())
                    .categoryName(category.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .statusBusiness(product.getStatusBusiness())
                    .build();
        }
    }

    // Kiểm tra sản phẩm có sẵn trong danh mục hay không?
    @Transactional(readOnly = true)
    public List<ProductResponse> isExisting(List<String> productIds) {
        List<ProductResponse> productResponses = new ArrayList<>();
        for (String id : productIds) {
            Optional<Product> productOptional = productRepository.findById(id);
            if (productOptional.isEmpty()) {
                ProductResponse productResponse = ProductResponse.builder()
                        .id(id)
                        .isExisting(false)
                        .build();
                productResponses.add(productResponse);
            } else {
                Product product = productOptional.get();
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
    // List<Product> products = productRepository.findAllById(id);
    // List<ProductResponse> productResponses = new ArrayList<>();

    // int index = 0;

    // for (ProductResponse productExistingResponse : productResponses) {
    // if (products.get(index) == null) {
    // productExistingResponse.setIsExisting(false);
    // } else {
    // productExistingResponse.setId(products.get(index).getId());
    // productExistingResponse.setName(products.get(index).getName());
    // productExistingResponse.setCategoryId(products.get(index).getCategoryId());

    // Category category =
    // categoryRepository.findById(products.get(index).getCategoryId()).get();

    // productExistingResponse.setCategoryName(category != null ? category.getName()
    // : "Chưa xác định");

    // productExistingResponse.setIsExisting(true);
    // productExistingResponse.setStatusBusiness(products.get(index).getStatusBusiness());

    // index++;

    // }
    // }

    // return productResponses;
    // }
}
