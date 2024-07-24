package org.programmingtechie.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmingtechie.dto.response.ImportHistoryResponse;
import org.programmingtechie.service.ImportHistoryServiceV1;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/import-history")
@RequiredArgsConstructor
@Slf4j
public class ImportHistoryControllerV1 {
    final ImportHistoryServiceV1 importHistoryServiceV1;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ImportHistoryResponse> getAll()
    {
        return importHistoryServiceV1.getAll();
    }
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ImportHistoryResponse getById(@PathVariable String id)
    {
        return importHistoryServiceV1.getById(id);
    }
    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    public ImportHistoryResponse getById_Body(@RequestBody String id)
    {
        return importHistoryServiceV1.getById(id);
    }

    @GetMapping("/product/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<ImportHistoryResponse> getAllByProductId(@PathVariable String product_id)
    {
        return importHistoryServiceV1.getAllByProductId(product_id);
    }

    @GetMapping("/product")
    @ResponseStatus(HttpStatus.OK)
    public List<ImportHistoryResponse> getAllByProductId_Body(@RequestBody String product_id)
    {
        return importHistoryServiceV1.getAllByProductId(product_id);
    }
}
