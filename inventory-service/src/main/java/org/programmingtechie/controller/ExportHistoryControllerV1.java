package org.programmingtechie.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmingtechie.service.ExportHistoryServiceV1;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory/export_history")
@RequiredArgsConstructor
@Slf4j
public class ExportHistoryControllerV1 {
    final ExportHistoryServiceV1 exportHistoryServiceV1;


}
