package org.programmingtechie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmingtechie.repository.ExportHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExportHistoryServiceV1 {
    final ExportHistoryRepository exportHistoryRepository;

}
