package com.hamrochalchitraghar.system.service;

import com.hamrochalchitraghar.system.model.ErrorLog;
import com.hamrochalchitraghar.system.repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ErrorLogService {

    private final ErrorLogRepository errorLogRepository;

    public void logError(String source, String message, String stackTrace) {
        ErrorLog log = ErrorLog.builder()
                .source(source)
                .message(message)
                .stackTrace(stackTrace != null ? stackTrace.substring(0, Math.min(1000, stackTrace.length())) : null)
                .timestamp(LocalDateTime.now())
                .build();
        errorLogRepository.save(log);
    }
}
