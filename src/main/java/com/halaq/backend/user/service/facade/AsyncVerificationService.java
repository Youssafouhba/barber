package com.halaq.backend.user.service.facade;

import com.halaq.backend.user.dto.IdCardDataDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AsyncVerificationService {

    private final Map<String, CompletableFuture<IdCardDataDto>> taskMap = new ConcurrentHashMap<>();
    private final Map<String, String> taskStatus = new ConcurrentHashMap<>();

    @Autowired
    private VerificationService verificationService;

    @Async
    public CompletableFuture<IdCardDataDto> processIdCardAsync(String taskId, MultipartFile file) {
        taskStatus.put(taskId, "PROCESSING");

        CompletableFuture<IdCardDataDto> future = CompletableFuture.supplyAsync(() -> {
            try {
                IdCardDataDto result = verificationService.extractIdCardData(file);
                taskStatus.put(taskId, "COMPLETED");
                return result;
            } catch (Exception e) {
                taskStatus.put(taskId, "ERROR");
                throw new RuntimeException(e);
            }
        });

        taskMap.put(taskId, future);
        return future;
    }

    public String getTaskStatus(String taskId) {
        return taskStatus.getOrDefault(taskId, "NOT_FOUND");
    }

    public IdCardDataDto getTaskResult(String taskId) {
        CompletableFuture<IdCardDataDto> future = taskMap.get(taskId);
        if (future != null && future.isDone() && !future.isCompletedExceptionally()) {
            try {
                return future.get();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}