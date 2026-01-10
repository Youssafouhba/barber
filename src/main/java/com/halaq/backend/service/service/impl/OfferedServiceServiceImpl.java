package com.halaq.backend.service.service.impl;

import com.halaq.backend.service.entity.OfferedService;
import com.halaq.backend.service.criteria.OfferedServiceCriteria;
import com.halaq.backend.service.entity.ServiceCategory;
import com.halaq.backend.service.repository.OfferedServiceRepository;
import com.halaq.backend.service.service.facade.OfferedServiceService;
import com.halaq.backend.service.service.facade.ServiceCategoryService;
import com.halaq.backend.service.specification.OfferedServiceSpecification;
import com.halaq.backend.core.service.AbstractServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfferedServiceServiceImpl extends AbstractServiceImpl<OfferedService, OfferedServiceCriteria, OfferedServiceRepository> implements OfferedServiceService {
    // Utilisation de @Lazy pour éviter les dépendances circulaires si ServiceCategoryService
    // a aussi une dépendance vers OfferedServiceService.
    @Autowired
    @Lazy
    private ServiceCategoryService serviceCategoryService;

    public OfferedServiceServiceImpl(OfferedServiceRepository dao) {
        super(dao);
    }

    /**
     * Cette méthode est appelée par la méthode 'create' de la classe abstraite.
     * Son rôle est de s'assurer que les objets liés, comme 'category', sont
     * bien des entités gérées par JPA avant la sauvegarde.
     */
    @Override
    public void findOrSaveAssociatedObject(OfferedService service) {
        // On vérifie si le service a une catégorie avec un ID
        if (service != null && service.getCategory() != null) {
            ServiceCategory managedCategory = serviceCategoryService.findOrSave(service.getCategory());
            service.setCategory(managedCategory);
        }
    }

    @Override
    public List<OfferedService> findByCategoryId(Long categoryId) {
        OfferedServiceCriteria criteria = new OfferedServiceCriteria();
        criteria.setCategoryId(categoryId);
        return findByCriteria(criteria);
    }



    @Override
    public void configure() {
        super.configure(OfferedService.class, OfferedServiceSpecification.class);
    }
}