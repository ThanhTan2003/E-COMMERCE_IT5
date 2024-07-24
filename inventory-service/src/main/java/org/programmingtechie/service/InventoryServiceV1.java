package org.programmingtechie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmingtechie.dto.request.ExportProductRequest;
import org.programmingtechie.dto.request.ImportHistoryRequest;
import org.programmingtechie.dto.response.InventoryResponse;
import org.programmingtechie.dto.response.ProductExistingResponse;
import org.programmingtechie.model.ExportHistory;
import org.programmingtechie.model.ImportHistory;
import org.programmingtechie.model.Inventory;
import org.programmingtechie.repository.ExportHistoryRepository;
import org.programmingtechie.repository.ImportHistoryRepository;
import org.programmingtechie.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryServiceV1 {
    final InventoryRepository inventoryRepository;
    final ImportHistoryRepository importHistoryRepository;
    final ExportHistoryRepository exportHistoryRepository;

    final WebClient.Builder webClientBuilder;

    // Kiểm tra danh sách productIds với fallback nếu product-service không hoạt động
    public List<ProductExistingResponse> checkProductExistingWithFallback(List<String> productIds)
    {
        try {
            return checkProductExisting(productIds);
        } catch (Exception e) {
            return productIds.stream()
                    .map(productId -> new ProductExistingResponse(productId, "...","...", "...", false, null, null, null))
                    .toList();
        }
    }

    // Kiểm tra hợp lệ thông tin khi nhập kho
    void validCheckInventoryRequest(ImportHistoryRequest importHistoryRequest)
    {
        if(importHistoryRequest.getProductId() == null)
            throw new IllegalArgumentException("Vui lòng nhập thông tin sản phẩm nhập kho!");

        if(importHistoryRequest.getQuantity() == null)
            throw new IllegalArgumentException("Vui lòng nhập số lượng sản phẩm nhập kho!");

        if(importHistoryRequest.getQuantity() <= 0)
            throw new IllegalArgumentException("Số lượng không hợp lệ!");

    }

    // Kiểm tra danh sách productIds có tồn tại trong product-service không?
    List<ProductExistingResponse> checkProductExisting(List<String> productIds)
    {
        try
        {
            ProductExistingResponse[] productResponses = webClientBuilder.build().get()
                    .uri("http://product-service/api/v1/product/is-existing",
                            uriBuilder -> uriBuilder.queryParam("list_product_id",productIds).build())
                    .retrieve()
                    .bodyToMono(ProductExistingResponse[].class)
                    .block();

            if (productResponses == null) {
                throw new IllegalStateException("Không thể kiểm tra sản phẩm. Vui lòng thử lại sau.");
            }

            for (ProductExistingResponse productResponse : productResponses) {
                if (!productResponse.getIsExisting()) {
                    throw new IllegalStateException(
                            String.format("Sản phẩm có mã id %s không tồn tại. Vui lòng kiểm tra lại!",
                                    productResponse.getId()));
                }
            }
            return Arrays.asList(productResponses);
        }
        catch (WebClientException e) {
            log.info("ERROR - Xảy ra lỗi khi giao tiếp với product-service: {}", e.getMessage());

            throw new IllegalArgumentException("Dịch vụ quản lý sản phẩm (product-service) không khả dụng. Vui lòng kiểm tra hoặc thử lại sau!");
        } catch (Exception e) {
            log.info("ERROR - Xảy ra lỗi khi giao tiếp với product-service: {}", e.getMessage());

            throw new IllegalArgumentException("Có lỗi xảy ra khi kiểm tra sản phẩm. Vui lòng thử lại sau!");
        }
    }

    // Nhập kho
    public void importStock(List<ImportHistoryRequest> importHistoryRequests)
    {
        List<String> productIds = importHistoryRequests.stream()
                .map(ImportHistoryRequest::getProductId)
                .toList();

        // Kiểm tra sự tồn tại của các sản phẩm
        List<ProductExistingResponse> pProductExistingResponse = checkProductExisting(productIds);

        for (ImportHistoryRequest importHistoryRequest : importHistoryRequests) {
            validCheckInventoryRequest(importHistoryRequest);

            Optional<Inventory> inventoryOptional = inventoryRepository.findByProductId(importHistoryRequest.getProductId());

            Inventory inventory;
            if (inventoryOptional.isEmpty()) {
                inventory = Inventory.builder()
                        .productId(importHistoryRequest.getProductId())
                        .quantity(importHistoryRequest.getQuantity())
                        .build();
            } else {
                inventory = inventoryOptional.get();
                Integer quantity = inventory.getQuantity() + importHistoryRequest.getQuantity();
                inventory.setQuantity(quantity);
            }
            inventoryRepository.save(inventory);
            ImportHistory importHistory = ImportHistory.builder()
                    .productId(importHistoryRequest.getProductId())
                    .quantity(importHistoryRequest.getQuantity())
                    .note(importHistoryRequest.getNote())
                    .build();
            importHistoryRepository.save(importHistory);
        }
    }

    // Cập nhật số lượng tồn kho của một sản phẩm
    void updateInventory(String product_id, Integer quantity)
    {
        Optional<Inventory> inventory = inventoryRepository.findByProductId(product_id);

        if(inventory.isEmpty())
            throw new IllegalArgumentException("sản phẩm có id " + product_id + " không có sẵn trong kho!");

        Inventory inventory1 = inventory.get();
        inventory1.setQuantity(quantity);
        try
        {
            inventoryRepository.save(inventory1);
        }
        catch (Exception ignored)
        {

        }
    }

    // Ghi lịch sử xuất kho (ExportHistory) cho sản phẩm
    public void createExportHistory(List<ExportProductRequest> exportProductRequest)
    {
        for (ExportProductRequest exportProductRequest1 : exportProductRequest)
        {
            ExportHistory exportHistory = ExportHistory.builder()
                    .productId(exportProductRequest1.getProductId())
                    .quantity(exportProductRequest1.getQuantity())
                    .build();
            exportHistoryRepository.save(exportHistory);
        }
    }

    // Kiểm tra tồn kho 1 sản phẩm
    @Transactional(readOnly = true)
    public InventoryResponse isInStock(String product_id)
    {

        Optional<Inventory> inventory = inventoryRepository.findByProductId(product_id);

        if (inventory.isEmpty()) {
            return InventoryResponse.builder()
                    .productId(product_id)
                    .isInStock(false)
                    .quantity(0)
                    .build();
        }

        Inventory inv = inventory.get();
        Boolean inStock = inv.getQuantity() > 0;
        return InventoryResponse.builder()
                .productId(inv.getProductId())
                .isInStock(inStock)
                .quantity(inv.getQuantity())
                .build();
    }

    // Kiểm tra tồn kho nhiều sản phẩm
    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> product_id)
    {

        List<Inventory> inventories = inventoryRepository.findByProductIdIn(product_id);

        return inventories.stream()
                .map(inventory -> {
                    Boolean inStock = inventory.getQuantity() > 0;
                    return InventoryResponse.builder()
                            .productId(inventory.getProductId())
                            .isInStock(inStock)
                            .quantity(inventory.getQuantity())
                            .build();
                })
                .toList();
    }

    // lấy danh sách thông tin sản phẩm tồn kho
    public List<InventoryResponse> getAll() {
        return inventoryRepository.findAll().stream().map(inventory -> {
            List<String> productIds = new ArrayList<>();
            productIds.add(inventory.getProductId());
            List<ProductExistingResponse> productExistingResponse = checkProductExistingWithFallback(productIds);

            String productName = "...";
            String categoryName = "...";
            if (!productExistingResponse.isEmpty() && productExistingResponse.get(0) != null) {
                productName = productExistingResponse.get(0).getName().isEmpty() ? "..." : productExistingResponse.get(0).getName();
                categoryName = productExistingResponse.get(0).getCategoryName().isEmpty() ? "..." : productExistingResponse.get(0).getCategoryName();
            }

            return InventoryResponse.builder()
                    .id(inventory.getId())
                    .productId(inventory.getProductId())
                    .productName(productName)
                    .categoryName(categoryName)
                    .quantity(inventory.getQuantity())
                    .isInStock(inventory.getQuantity() > 0)
                    .build();
        }).toList();
    }


    // Lấy thông tin tồn kho theo ID
    public InventoryResponse getInventoryById(String id)
    {
        Optional<Inventory> inventory = inventoryRepository.findById(id);
        if(inventory.isEmpty())
        {
            throw new IllegalArgumentException("Không tìm thấy thông tin tồn kho!");
        }
        List<String> productIds = new ArrayList<>();
        productIds.add(inventory.get().getProductId());
        List<ProductExistingResponse> productExistingResponse = checkProductExistingWithFallback(productIds);

        String productName = "...";
        String categoryName = "...";
        if (!productExistingResponse.isEmpty() && productExistingResponse.get(0) != null) {
            productName = productExistingResponse.get(0).getName().isEmpty() ? "..." : productExistingResponse.get(0).getName();
            categoryName = productExistingResponse.get(0).getCategoryName().isEmpty() ? "..." : productExistingResponse.get(0).getCategoryName();
        }
        return InventoryResponse.builder()
                .id(inventory.get().getId())
                .productId(inventory.get().getProductId())
                .productName(productName)
                .categoryName(categoryName)
                .quantity(inventory.get().getQuantity())
                .isInStock(inventory.get().getQuantity() > 0)
                .build();
    }

    // Lấy thông tin tồn kho theo product_id
    public InventoryResponse getInventoryByProductId(String id)
    {
        Optional<Inventory> inventoryOptional = inventoryRepository.findByProductId(id);

        if (inventoryOptional.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy thông tin tồn kho!");
        }

        Inventory inventory = inventoryOptional.get();
        List<String> productIds = new ArrayList<>();
        productIds.add(inventory.getProductId());
        List<ProductExistingResponse> productExistingResponse = checkProductExistingWithFallback(productIds);

        String productName = productExistingResponse.get(0).getName().isEmpty() ? "Chưa xác định" : productExistingResponse.get(0).getName();

        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .productName(productName)
                .quantity(inventory.getQuantity())
                .build();
    }

    // Xuất kho sản phẩm khi order-service gọi đến
    public Boolean exportProduct(List<ExportProductRequest> exportProductRequest)
    {
        int index = 0;

        List<String> productIds = exportProductRequest.stream().map(
                ExportProductRequest::getProductId
        ).toList();

        List<InventoryResponse> inventoryResponses = isInStock(productIds);
        for(InventoryResponse inventoryResponse : inventoryResponses)
        {
            if(!inventoryResponse.getIsInStock())
                throw new IllegalArgumentException("sản phẩm " + inventoryResponse.getProductId() + " - " + exportProductRequest.get(index).getProductName() + " không có sẵn trong kho!");

            if(inventoryResponse.getQuantity() < exportProductRequest.get(index).getQuantity())
                throw new IllegalArgumentException("sản phẩm " + inventoryResponse.getProductId() + " - " + exportProductRequest.get(index).getProductName() + " không đủ số lượng trong kho!");
            index++;
        }

        index = 0;

        for(ExportProductRequest export : exportProductRequest)
        {
            Integer quantity = inventoryResponses.get(0).getQuantity() - export.getQuantity();
            updateInventory(export.getProductId(), quantity);
        }

        createExportHistory(exportProductRequest);

        return true;
    }
}
