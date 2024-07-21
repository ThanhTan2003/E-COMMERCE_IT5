package org.programmingtechie.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmingtechie.service.ImportHistoryServiceV1;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory/import_history")
@RequiredArgsConstructor
@Slf4j
public class ImportHistoryControllerV1 {
    final ImportHistoryServiceV1 importHistoryServiceV1;
}
