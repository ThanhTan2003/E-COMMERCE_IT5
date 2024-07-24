package org.programmingtechie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmingtechie.dto.response.ExportHistoryResponse;
import org.programmingtechie.dto.response.ProductExistingResponse;
import org.programmingtechie.model.ExportHistory;
import org.programmingtechie.repository.ExportHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExportHistoryServiceV1 {
    final ExportHistoryRepository exportHistoryRepository;
    final InventoryServiceV1 inventoryServiceV1;

    private ExportHistoryResponse createExportHistoryResponse(ExportHistory exportHistory, List<ProductExistingResponse> productExistingResponses)
    {
        ExportHistoryResponse response = new ExportHistoryResponse();
        response.setId(exportHistory.getId());
        response.setProductId(exportHistory.getProductId());
        response.setProductName("...");
        response.setCategoryName("...");
        for (ProductExistingResponse productExistingResponse : productExistingResponses) {
            if (exportHistory.getProductId().equals(productExistingResponse.getId())) {
                response.setProductName(productExistingResponse.getName());
                response.setCategoryName(productExistingResponse.getCategoryName());
            }
        }
        response.setQuantity(exportHistory.getQuantity());
        response.setDate(exportHistory.getDate());
        return response;
    }

    private String findProductName(String productId, List<ProductExistingResponse> productExistingResponses)
    {
        for (ProductExistingResponse productExistingResponse : productExistingResponses) {
            if (productId.equals(productExistingResponse.getId())) {
                return productExistingResponse.getName();
            }
        }
        return "Chưa xác định";
    }

    public List<ExportHistoryResponse> getAll() {
        List<String> productIds = exportHistoryRepository.findDistinctProductIds();
        List<ProductExistingResponse> productExistingResponses = inventoryServiceV1.checkProductExistingWithFallback(productIds);
        List<ExportHistory> exportHistories = exportHistoryRepository.findAll();

        List<ExportHistoryResponse> exportHistoryResponses = new ArrayList<>();

        for (ExportHistory exportHistory : exportHistories) {
            exportHistoryResponses.add(createExportHistoryResponse(exportHistory, productExistingResponses));
        }

        return exportHistoryResponses;
    }

    public ExportHistoryResponse getById(String id) {
        ExportHistory exportHistory = exportHistoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin xuất kho!"));

        List<String> productIds = new ArrayList<>();
        productIds.add(exportHistory.getProductId());
        List<ProductExistingResponse> productExistingResponses = inventoryServiceV1.checkProductExistingWithFallback(productIds);
        return createExportHistoryResponse(exportHistory, productExistingResponses);
    }

    public List<ExportHistoryResponse> getAllByProductId(String product_id) {
        List<ExportHistory> exportHistories = exportHistoryRepository.findByProductId(product_id);
        if(exportHistories.isEmpty())
            throw new IllegalArgumentException("Không tìm thấy thông tin xuất kho!");

        List<String> productIds = new ArrayList<>();
        productIds.add(product_id);
        List<ProductExistingResponse> productExistingResponses = inventoryServiceV1.checkProductExistingWithFallback(productIds);
        List<ExportHistoryResponse> exportHistoryResponses = new ArrayList<>();
        for (ExportHistory exportHistory : exportHistories) {
            exportHistoryResponses.add(createExportHistoryResponse(exportHistory, productExistingResponses));
        }
        return exportHistoryResponses;
    }
}
