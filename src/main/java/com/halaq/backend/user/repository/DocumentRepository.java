package com.halaq.backend.user.repository;

import com.halaq.backend.user.entity.Document;
import com.halaq.backend.core.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends AbstractRepository<Document, Long> {
}