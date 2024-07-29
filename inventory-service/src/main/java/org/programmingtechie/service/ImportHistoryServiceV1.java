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

    // Tạo ImportHistoryResponse
    // Xác định tên sản phẩm và thể loại sản phẩm tương ứng
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

    // Lấy danh sách thông tin sản phẩm nhập kho
    public List<ImportHistoryResponse> getAll() {
        // Lay danh sach id san pham co trong danh sach
        List<String> productIds = importHistoryRepository.findDistinctProductIds();

        List<ProductExistingResponse> productExistingResponses = inventoryServiceV1.checkProductExistingWithFallback(productIds);

        List<ImportHistory> importHistories = importHistoryRepository.findAll();

        List<ImportHistoryResponse> importHistoryResponses = new ArrayList<>();

        for (ImportHistory importHistory : importHistories) {
            importHistoryResponses.add(createImportHistoryResponse(importHistory, productExistingResponses));
        }

        // Sắp xếp danh sách theo date giảm dần
        importHistoryResponses.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));

        return importHistoryResponses;
    }

    //
    public ImportHistoryResponse getById(String id) {
        ImportHistory importHistory = importHistoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin xuất kho!"));

        List<String> productIds = new ArrayList<>();
        productIds.add(importHistory.getProductId());
        List<ProductExistingResponse> productExistingResponses = inventoryServiceV1.checkProductExistingWithFallback(productIds);
        return createImportHistoryResponse(importHistory, productExistingResponses);
    }

    //
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
