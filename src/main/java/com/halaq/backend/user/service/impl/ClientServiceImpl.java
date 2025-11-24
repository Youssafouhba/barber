package com.halaq.backend.user.service.impl;

import com.halaq.backend.core.transverse.cloud.dto.FileUploadResult;
import com.halaq.backend.core.transverse.cloud.sevice.facade.MinIOService;
import com.halaq.backend.core.util.FileStorageService;
import com.halaq.backend.user.entity.Client;
import com.halaq.backend.user.entity.Client;
import com.halaq.backend.user.criteria.ClientCriteria;
import com.halaq.backend.user.repository.ClientRepository;
import com.halaq.backend.user.service.facade.ClientService;
import com.halaq.backend.user.specification.ClientSpecification;
import com.halaq.backend.core.service.AbstractServiceImpl;
import io.minio.errors.MinioException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static com.halaq.backend.core.util.FileUtils.generateUniqueFileName;

@Service
public class ClientServiceImpl extends AbstractServiceImpl<Client, ClientCriteria, ClientRepository> implements ClientService {
    private final MinIOService minioService;
    private final FileStorageService fileStorageService;
    public ClientServiceImpl(ClientRepository dao, MinIOService minioService, FileStorageService fileStorageService) {
        super(dao);
        this.minioService = minioService;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public void configure() {
        super.configure(Client.class, ClientSpecification.class);
    }

    /**
     * Upload l'avatar d'un barbier
     * Supprime l'ancien avatar s'il existe
     */
    @Override
    public FileUploadResult uploadClientAvatar(
            Long ClientId,
            MultipartFile file) {
        String uniqueName = generateUniqueFileName(ClientId, file.getOriginalFilename());
        // Recuperer le barbier
        Client Client = dao.findById(ClientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        // Supprimer l'ancien avatar s'il existe
        if (Client.getAvatar() != null && !Client.getAvatar().isEmpty()) {
            String oldObjectName = Client.getAvatar();
            minioService.deleteFile(oldObjectName,"client-avatars");
        }

        // Upload le nouveau fichier
        Optional<FileUploadResult> uploadResult = minioService.uploadFile(file, "client-avatars", uniqueName);

        if (!uploadResult.isPresent()) {
            throw new RuntimeException("Erreur lors de l'upload du fichier");
        }

        FileUploadResult result = uploadResult.get();

        // Mettre à jour l'URL de l'avatar dans la base de données
        Client.setAvatar(uniqueName);
        dao.save(Client);
        return result;
    }

    /**
     * Télécharger l'avatar d'un barbier
     */
    @Override
    public byte[] downloadClientAvatar(Long ClientId) throws MinioException {
        Client Client = dao.findById(ClientId)
                .orElseThrow(() -> new RuntimeException("Barbier non trouvé"));

        if (Client.getAvatar() == null || Client.getAvatar().isEmpty()) {
            throw new RuntimeException("Aucun avatar trouvé");
        }

        return fileStorageService.downloadFile("client-avatars", Client.getAvatar());
    }

}
