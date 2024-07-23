package org.programmingtechie.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmingtechie.dto.request.ExportProductRequest;
import org.programmingtechie.dto.request.ImportHistoryRequest;
import org.programmingtechie.dto.response.InventoryResponse;
import org.programmingtechie.service.InventoryServiceV1;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryControllerV1
{
    final InventoryServiceV1 inventoryServiceV1;
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> getAll()
    {
        return inventoryServiceV1.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse getInventoryById(@PathVariable String id)
    {
        return inventoryServiceV1.getInventoryById(id);
    }

    @PostMapping("/id")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse getInventoryById_1(@RequestBody String id)
    {
        return inventoryServiceV1.getInventoryById(id);
    }

    @GetMapping("/product/{id}")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse getInventoryByProductId(@PathVariable String id)
    {
        return inventoryServiceV1.getInventoryByProductId(id);
    }

    @PostMapping("/product")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse getInventoryByProductId_1(@RequestBody String id)
    {
        return inventoryServiceV1.getInventoryByProductId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String importStock(@RequestBody List<ImportHistoryRequest> importHistoryRequests)
    {
        inventoryServiceV1.importStock(importHistoryRequests);
        return "Đã nhập kho thành công!";
    }

    @GetMapping("/is_in_stock")
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> product_id) {
        return inventoryServiceV1.isInStock(product_id);
    }

    @GetMapping("/is_in_stock/single")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse isInStock(@RequestParam String product_id) {
        return inventoryServiceV1.isInStock(product_id);
    }

    @PostMapping("/export_product")
    @ResponseStatus(HttpStatus.OK)
    public Boolean exportProduct(@RequestBody List<ExportProductRequest> exportProductRequest)
    {
        return inventoryServiceV1.exportProduct(exportProductRequest);
    }

    @GetMapping("/load")
    public String a()
    {
        return "load";
    }
}
