package org.programmingtechie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmingtechie.dto.response.ImportHistoryResponse;
import org.programmingtechie.dto.response.ProductExistingResponse;
import org.programmingtechie.model.ImportHistory;
import org.programmingtechie.repository.ImportHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ImportHistoryServiceV1 {
    final ImportHistoryRepository importHistoryRepository;
    final InventoryServiceV1 inventoryServiceV1;

    private ImportHistoryResponse createImportHistoryResponse(ImportHistory importHistory, List<ProductExistingResponse> productExistingResponses)
    {

        ImportHistoryResponse response = new ImportHistoryResponse();
        response.setId(importHistory.getId());
        response.setProductId(importHistory.getProductId());
        response.setProductName("...");
        response.setCategoryName("...");
        for (ProductExistingResponse productExistingResponse : productExistingResponses) {
            if (importHistory.getProductId().equals(productExistingResponse.getId())) {
                response.setProductName(productExistingResponse.getName());
                response.setCategoryName(productExistingResponse.getCategoryName());
            }
        }
        response.setQuantity(importHistory.getQuantity());
        response.setDate(importHistory.getDate());
        response.setNote(importHistory.getNote());
        return response;
    }

//    private String findProductName(String productId, List<ProductExistingResponse> productExistingResponses)
//    {
//        for (ProductExistingResponse productExistingResponse : productExistingResponses) {
//            if (productId.equals(productExistingResponse.getId())) {
//                return productExistingResponse.getName();
//            }
//        }
//        return "Chưa xác định";
//    }


    public List<ImportHistoryResponse> getAll() {

        // Lay danh sach id san pham co trong danh sach
        List<String> productIds = importHistoryRepository.findDistinctProductIds();

        List<ProductExistingResponse> productExistingResponses = inventoryServiceV1.checkProductExistingWithFallback(productIds);

        List<ImportHistory> exportHistories = importHistoryRepository.findAll();

        List<ImportHistoryResponse> exportHistoryResponses = new ArrayList<>();

        for (ImportHistory exportHistory : exportHistories) {
            exportHistoryResponses.add(createImportHistoryResponse(exportHistory, productExistingResponses));
        }

        return exportHistoryResponses;
    }

    public ImportHistoryResponse getById(String id) {
        ImportHistory importHistory = importHistoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin xuất kho!"));

        List<String> productIds = new ArrayList<>();
        productIds.add(importHistory.getProductId());
        List<ProductExistingResponse> productExistingResponses = inventoryServiceV1.checkProductExistingWithFallback(productIds);
        return createImportHistoryResponse(importHistory, productExistingResponses);
    }

    public List<ImportHistoryResponse> getAllByProductId(String product_id) {
        List<ImportHistory> exportHistories = importHistoryRepository.findByProductId(product_id);
        if(exportHistories.isEmpty())
            throw new IllegalArgumentException("Không tìm thấy thông tin xuất kho!");

        List<String> productIds = new ArrayList<>();
        productIds.add(product_id);
        List<ProductExistingResponse> productExistingResponses = inventoryServiceV1.checkProductExistingWithFallback(productIds);
        List<ImportHistoryResponse> importHistoryResponses = new ArrayList<>();
        for (ImportHistory importHistory : exportHistories) {
            importHistoryResponses.add(createImportHistoryResponse(importHistory, productExistingResponses));
        }
        return importHistoryResponses;
    }
}
