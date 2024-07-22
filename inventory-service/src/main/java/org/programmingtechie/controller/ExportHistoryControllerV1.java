package org.programmingtechie.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmingtechie.dto.response.ExportHistoryResponse;
import org.programmingtechie.service.ExportHistoryServiceV1;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/export_history")
@RequiredArgsConstructor
@Slf4j
public class ExportHistoryControllerV1 {
    final ExportHistoryServiceV1 exportHistoryServiceV1;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ExportHistoryResponse> getAll()
    {
        return exportHistoryServiceV1.getAll();
    }
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ExportHistoryResponse getById(@PathVariable String id)
    {
        return exportHistoryServiceV1.getById(id);
    }
    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    public ExportHistoryResponse getById_Body(@RequestBody String id)
    {
        return exportHistoryServiceV1.getById(id);
    }

    @GetMapping("/product/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<ExportHistoryResponse> getAllByProductId(@PathVariable String product_id)
    {
        return exportHistoryServiceV1.getAllByProductId(product_id);
    }

    @GetMapping("/product")
    @ResponseStatus(HttpStatus.OK)
    public List<ExportHistoryResponse> getAllByProductId_Body(@RequestBody String product_id)
    {
        return exportHistoryServiceV1.getAllByProductId(product_id);
    }
}
