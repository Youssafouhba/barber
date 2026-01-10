package com.halaq.backend.user.service.facade;

import com.halaq.backend.core.transverse.cloud.dto.FileUploadResult;
import com.halaq.backend.user.entity.Client;
import com.halaq.backend.user.criteria.ClientCriteria;
import com.halaq.backend.core.service.IService;
import io.minio.errors.MinioException;
import org.springframework.web.multipart.MultipartFile;

public interface ClientService extends IService<Client, ClientCriteria> {
    byte[] downloadClientAvatar(Long clientId) throws MinioException;

    FileUploadResult uploadClientAvatar(
            Long clientId,
            MultipartFile file);

    // Methods specific to clients can be added here
}