package com.halaq.backend.user.service.facade;

import com.halaq.backend.shared.DocumentType;
import com.halaq.backend.user.dto.DocumentDto;
import com.halaq.backend.user.entity.Document;
import com.halaq.backend.user.criteria.DocumentCriteria;
import com.halaq.backend.core.service.IService;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService extends IService<Document, DocumentCriteria> {
    List<Document> uploadMultiple(MultipartFile[] files, DocumentType documentType) throws FileUploadException;

    byte[] downloadFile(Long docId);

    List<Document> uploadPortfolioImage(MultipartFile[] files) throws FileUploadException;
}