package com.halaq.backend.core.service;

import com.halaq.backend.core.criteria.BaseCriteria;
import com.halaq.backend.core.entity.BaseEntity;
import com.halaq.backend.core.exception.BusinessRuleException;
import com.halaq.backend.core.exception.EntityNotFoundException;
import com.halaq.backend.core.repository.AbstractRepository;
import com.halaq.backend.core.specification.AbstractSpecification;
import com.halaq.backend.core.util.*;
import jakarta.persistence.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


public abstract class AbstractServiceImpl<T extends BaseEntity, CRITERIA extends BaseCriteria, REPO extends AbstractRepository<T, Long>> extends AbstractServiceImplHelper<T> {
    // Injecter le contexte de Spring pour trouver d'autres services.
    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired
    private ApplicationContext applicationContext;
    protected AbstractSpecification<CRITERIA, T> specification;
    protected Class<? extends AbstractSpecification<CRITERIA, T>> specificationClass;



    protected REPO dao;

    protected Class<T> itemClass;

    @Value("${app.upload.location:uploads/}")
    private String UPLOADED_FOLDER;
    @Value("${app.upload.temp-dir:uploads/tmp/}")
    private String UPLOADED_TEMP_FOLDER;

    public AbstractServiceImpl(REPO dao) {
        this.dao = dao;
        this.configure();
    }


    public void deleteAssociatedLists(Long id) {
    }

    public void deleteAssociatedListsByReferenceEntity(T t) {
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = false)
    public boolean deleteById(Long id) {
        boolean condition = deleteByIdCheckCondition(id);
        if (condition) {
            deleteAssociatedLists(id);
            dao.deleteById(id);
        }
        return condition;
    }

    public boolean deleteByIdCheckCondition(Long id) {
        return true;
    }

    public void deleteByIdIn(List<Long> ids) {
        //dao.deleteByIdIn(ids);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = false)
    public T create(T t) {
        T loaded = findByReferenceEntity(t);
        if (loaded == null) {
            // --- CORRECTION ---
            // On s'assure que les objets associés (comme la catégorie d'un service)
            // sont bien des entités gérées par JPA avant de sauvegarder.
            findOrSaveAssociatedObject(t);
            T saved = dao.save(t);
            return saved;
        } else {
            return loaded;
        }
    }
    /*

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = false)
    public DTO create(DTO dto) {
        T t = converter.toItem(dto);
        T saved = dao.save(t);
        dto.setId(saved.getId());
        return dto;
    }
    */

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = false)
    public List<T> create(List<T> ts) {
        List<T> result = new ArrayList<>();
        if (ts != null) {
            for (T t : ts) {
                if (t.getId() == null || findById(t.getId()) == null) {
                    dao.save(t);
                } else {
                    result.add(t);
                }
            }
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = false)
    public List<T> update(List<T> ts, boolean createIfNotExist) {
        List<T> result = new ArrayList<>();
        if (ts != null) {
            for (T t : ts) {
                if (t.getId() == null) {
                    dao.save(t);
                } else {
                    T loadedItem = dao.findById(t.getId()).orElse(null);
                    if (createIfNotExist && (t.getId() == null || loadedItem == null)) {
                        dao.save(t);
                    } else if (t.getId() != null && loadedItem != null) {
                        dao.save(t);
                    } else {
                        result.add(t);
                    }
                }
            }
        }
        return result;
    }

    /* @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = false)
    public List<DTO> create(List<DTO> dtos) {
        if (dtos != null) {
            for (DTO dto : dtos) {
                create(dto);
            }
        }
        return dtos;
    }
    */



    /**
     * Surcharge de la méthode update pour gérer correctement les listes associées.
     * C'EST LA NOUVELLE MÉTHODE À UTILISER.
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = false)
    public T update(T t) {
        if (t == null || t.getId() == null) {
            throw new IllegalArgumentException("L'entité à mettre à jour ne peut pas être nulle ou n'a pas d'ID.");
        }

        // 1. Charger l'entité managée AVEC ses collections (en utilisant votre méthode existante)
        T loadedItem = this.findWithAssociatedLists(t.getId());
        if (loadedItem == null) {
            throw new EntityNotFoundException("errors.notFound", new String[]{itemClass.getSimpleName(), t.getId().toString()});
        }

        // 2. Mettre à jour les listes associées
        //    Cette méthode va fusionner les listes de 't' (nouvel état) dans 'loadedItem' (état managé)
        updateWithAssociatedLists(loadedItem, t);

        // 3. Copier les propriétés simples (non-collection) de 't' vers 'loadedItem'
        //    Nous devons le faire manuellement pour éviter d'écraser les collections fusionnées.
        BeanWrapper src = new BeanWrapperImpl(t);
        BeanWrapper dest = new BeanWrapperImpl(loadedItem);

        for (java.beans.PropertyDescriptor pd : src.getPropertyDescriptors()) {
            String propName = pd.getName();
            // On ne copie QUE si ce n'est pas une collection et si la source n'est pas nulle
            if (src.isReadableProperty(propName) && dest.isWritableProperty(propName) &&
                    !Collection.class.isAssignableFrom(pd.getPropertyType()) && // IGNORE LES COLLECTIONS
                    !propName.equals("class")) {

                Object srcValue = src.getPropertyValue(propName);
                if (srcValue != null) {
                    dest.setPropertyValue(propName, srcValue);
                }
            }
        }

        // 4. Sauvegarder l'entité managée (loadedItem)
        return dao.save(loadedItem);
    }

    /**
     * FIXED: Avoid merging child entities to prevent OptimisticLockException
     *
     * Strategy:
     * 1. For new children (id == null): Add directly to collection
     * 2. For existing children: Load them fresh by ID, update their fields,
     *    then add to managed collection
     * 3. For deleted children: Remove from collection (orphanRemoval handles deletion)
     *
     * KEY: Never call entityManager.merge() on children - it causes version conflicts
     */
    public void updateWithAssociatedLists(T managed, T incoming) {
        try {
            for (Field field : itemClass.getDeclaredFields()) {
                if (Collection.class.isAssignableFrom(field.getType()) &&
                        (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class))) {

                    // Get cascade config
                    OneToMany oneToMany = field.getAnnotation(OneToMany.class);
                    boolean hasOrphanRemoval = oneToMany != null && oneToMany.orphanRemoval();

                    field.setAccessible(true);
                    Collection<BaseEntity> managedCollection = (Collection<BaseEntity>) field.get(managed);
                    Collection<BaseEntity> incomingCollection = (Collection<BaseEntity>) field.get(incoming);

                    if (incomingCollection == null) continue;
                    if (managedCollection == null) {
                        managedCollection = (field.getType().equals(List.class) ? new ArrayList<>() : new HashSet<>());
                        field.set(managed, managedCollection);
                    }

                    // Convert to lists for comparison
                    List<BaseEntity> oldList = new ArrayList<>(managedCollection);
                    List<BaseEntity> newList = new ArrayList<>(incomingCollection);

                    // Find differences
                    List<List<BaseEntity>> diffs = (List) getToBeSavedAndToBeDeleted(
                            (List<T>) oldList, (List<T>) newList
                    );
                    List<BaseEntity> toSaveOrUpdate = diffs.get(0);
                    List<BaseEntity> toDelete = diffs.get(1);

                    // 1. Remove deleted items
                    managedCollection.removeAll(toDelete);

                    // 2. Process items to save/update
                    for (BaseEntity incomingEntity : toSaveOrUpdate) {
                        if (incomingEntity.getId() == null) {
                            // NEW ENTITY: Just add it, cascade will handle INSERT
                            managedCollection.add(incomingEntity);
                        } else {
                            // EXISTING ENTITY: This is where OptimisticLockException happens
                            // DON'T merge - instead:

                            // Check if already in managed collection
                            boolean alreadyInCollection = managedCollection.stream()
                                    .anyMatch(e -> e.getId() != null && e.getId().equals(incomingEntity.getId()));

                            if (alreadyInCollection) {
                                // Already in collection - Hibernate will auto-update it via dirty checking
                                // Find the managed instance and update its fields
                                BaseEntity managedEntity = managedCollection.stream()
                                        .filter(e -> e.getId() != null && e.getId().equals(incomingEntity.getId()))
                                        .findFirst()
                                        .orElse(null);

                                if (managedEntity != null) {
                                    // Copy fields from incoming to managed
                                    copySimpleFields(incomingEntity, managedEntity);
                                }
                            } else {
                                // Not in collection yet - need to load it fresh
                                // Get the appropriate DAO to fetch fresh managed instance
                                BaseEntity freshManagedEntity = loadEntityById(incomingEntity.getId());
                                if (freshManagedEntity != null) {
                                    // Update the fresh managed entity with incoming values
                                    copySimpleFields(incomingEntity, freshManagedEntity);
                                    // Add to collection
                                    managedCollection.add(freshManagedEntity);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Reflection error during associated list merging", e);
        }
    }

    private BaseEntity loadEntityById(Long id) {
        // This is a generic solution - you may need to customize based on entity type
        try {
            // Option 1: Use EntityManager directly (preferred for performance)
            return entityManager.find(this.itemClass, id);

            // Option 2: Use the DAO (if you have it accessible)
            // return (BaseEntity) dao.findById(id).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Copy only simple (non-collection) fields from source to destination
     * This avoids version conflicts and respects managed entity state
     */
    private void copySimpleFields(BaseEntity source, BaseEntity destination) {
        BeanWrapper src = new BeanWrapperImpl(source);
        BeanWrapper dest = new BeanWrapperImpl(destination);

        for (java.beans.PropertyDescriptor pd : src.getPropertyDescriptors()) {
            String propName = pd.getName();

            // Skip collections, version fields, id, and class
            if (src.isReadableProperty(propName) && dest.isWritableProperty(propName) &&
                    !Collection.class.isAssignableFrom(pd.getPropertyType()) &&
                    !propName.equals("id") &&
                    !propName.equals("class") &&
                    !propName.equals("version")) { // Skip version to avoid conflicts

                Object srcValue = src.getPropertyValue(propName);
                if (srcValue != null) {
                    dest.setPropertyValue(propName, srcValue);
                }
            }
        }
    }

    public T findById(Long id) {
        if (id == null) {
            return null;
        }
        T t = dao.findById(id).orElse(null);
        if (t == null) {
            throw new EntityNotFoundException("errors.notFound", new String[]{itemClass.getSimpleName(), id.toString()});
        }
        return t;
    }

    public T findByReferenceEntity(T t) {
        return t.getId() == null ? null : findById(t.getId());
    }

    public T findOrSave(T t) {
        if (t != null) {
            findOrSaveAssociatedObject(t);
            T result = findByReferenceEntity(t);
            if (result == null) {
                return create(t);
            } else {
                return result;
            }
        }
        return null;
    }

    /**
     * Cette méthode est maintenant GENÉRIQUE. Elle utilise la réflexion pour trouver
     * les champs annotés avec @ResolveAssociation et les traite automatiquement.
     * PLUS BESOIN DE LA SURCHARGER dans chaque service !
     */
    public void findOrSaveAssociatedObject(T t) {
        // Parcourt tous les champs déclarés dans la classe de l'entité.
        for (Field field : t.getClass().getDeclaredFields()) {
            // Vérifie si le champ a notre annotation.
            if (field.isAnnotationPresent(ResolveAssociation.class)) {
                try {
                    // Rend le champ accessible même s'il est 'private'.
                    field.setAccessible(true);
                    // Récupère la valeur du champ (ex: l'objet ServiceCategory détaché).
                    BaseEntity associatedObject = (BaseEntity) field.get(t);

                    if (associatedObject != null && associatedObject.getId() != null) {
                        // 1. Déterminer le nom du service à appeler.
                        // Convention: ServiceCategory -> serviceCategoryService
                        String serviceBeanName = field.getType().getSimpleName(); // "ServiceCategory"
                        serviceBeanName = serviceBeanName.substring(0, 1).toLowerCase() + serviceBeanName.substring(1) + "ServiceImpl"; // "serviceCategoryServiceImpl"

                        // 2. Récupérer le bean de service depuis le contexte Spring.
                        IService service = (IService) applicationContext.getBean(serviceBeanName);

                        // 3. Appeler la méthode findById (ou findOrSave) sur ce service.
                        BaseEntity managedObject = (BaseEntity) service.findById(associatedObject.getId());

                        // 4. Remplacer l'objet détaché par l'objet managé.
                        field.set(t, managedObject);
                    }
                } catch (Exception e) {
                    // Gérer l'exception, par exemple en la loggant.
                    // Il est crucial d'avoir une bonne gestion d'erreur ici.
                    throw new RuntimeException("Could not resolve association for field: " + field.getName(), e);
                }
            }
        }
    }


    public List<T> importerData(List<T> items) {
        List<T> list = new ArrayList<>();
        for (T t : items) {
            T founded = findByReferenceEntity(t);
            if (founded == null) {
                findOrSaveAssociatedObject(t);
                dao.save(t);
            } else {
                list.add(founded);
            }
        }
        return list;
    }

    /**
     * Récupère une entité par son ID en chargeant dynamiquement toutes ses collections
     * associées (annotées avec @OneToMany ou @ManyToMany) en une seule requête optimisée (JOIN FETCH).
     * Utilise la réflexion et un EntityGraph dynamique pour éviter le problème N+1.
     *
     * @param id L'ID de l'entité à rechercher.
     * @return L'entité avec ses listes associées initialisées, ou null si non trouvée.
     */
    public T findWithAssociatedLists(Long id) {
        // 1. Créer un EntityGraph pour notre type d'entité (T)
        EntityGraph<T> entityGraph = entityManager.createEntityGraph(itemClass);

        // 2. Utiliser la réflexion pour trouver les champs de type Collection
        for (Field field : itemClass.getDeclaredFields()) {
            // On ne s'intéresse qu'aux champs qui sont des collections (List, Set...)
            // ET qui sont des relations JPA (@OneToMany ou @ManyToMany)
            if (Collection.class.isAssignableFrom(field.getType()) &&
                    (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class))) {
                // 3. Ajouter le nom du champ à l'EntityGraph.
                // Cela dit à JPA : "Charge-moi ce champ en même temps que l'entité principale".
                entityGraph.addAttributeNodes(field.getName());
            }
        }

        // 4. Préparer les "hints" (indices) pour la requête JPA
        Map<String, Object> properties = new HashMap<>();
        // La propriété "jakarta.persistence.fetchgraph" force JPA à utiliser notre graphe
        // pour charger UNIQUEMENT les champs spécifiés (et l'ID).
        properties.put("jakarta.persistence.fetchgraph", entityGraph);

        // 5. Exécuter la requête find() avec l'EntityManager et les hints
        // C'est ici que la magie opère : JPA va générer une requête SQL avec des LEFT JOINs.
        return entityManager.find(itemClass, id, properties);
    }

    public void deleteWithAssociatedLists(T t) {
        deleteAssociatedLists(t.getId());
        delete(t);
    }

    public void updateWithAssociatedLists(T t) {

    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = false)
    public void delete(T t) {
        if (t != null) {
            deleteAssociatedLists(t.getId());
            dao.deleteById(t.getId()); // il fait find by id apres delete !!!
            //constructAndSaveHistory(dto, ACTION_TYPE.DELETE); TO DO
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, readOnly = false)
    public void delete(List<T> list) {
        if (list != null) {
            for (T t : list) {
                deleteAssociatedLists(t.getId());
                dao.deleteById(t.getId()); // il fait find by id apres delete !!!
                //constructAndSaveHistory(dto, ACTION_TYPE.DELETE); TO DO
            }
        }
    }


    public List<T> findByCriteria(CRITERIA criteria) {
        List<T> content = null;
        if (criteria != null) {
            AbstractSpecification<CRITERIA, T> mySpecification = constructSpecification(criteria);
            if (criteria.isPeagable()) {
                Pageable pageable = PageRequest.of(0, criteria.getMaxResults());
                content = dao.findAll(mySpecification, pageable).getContent();
            } else {
                content = dao.findAll(mySpecification);
            }
        } else {
            content = dao.findAll();
        }
        return content;

    }


    private AbstractSpecification<CRITERIA, T> constructSpecification(CRITERIA criteria) {
        AbstractSpecification<CRITERIA, T> mySpecification = RefelexivityUtil.constructObjectUsingOneParam(specificationClass, criteria);
        return mySpecification;
    }


    public List<T> findPaginatedByCriteria(CRITERIA criteria, int page, int pageSize, String order, String sortField) {
        AbstractSpecification<CRITERIA, T> mySpecification = constructSpecification(criteria);
        order = (order != null && !order.isEmpty()) ? order : "desc";
        sortField = (sortField != null && !sortField.isEmpty()) ? sortField : "id";
        Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.fromString(order), sortField);
        return dao.findAll(mySpecification, pageable).getContent();
    }

    public int getDataSize(CRITERIA criteria) {
        AbstractSpecification<CRITERIA, T> mySpecification = constructSpecification(criteria);
        mySpecification.setDistinct(true);
        return ((Long) dao.count(mySpecification)).intValue();
    }


    public List<T> findAll() {
        return dao.findAll();
    }

    public List<T> findAllOptimized() {
        return dao.findAll();
    }


    //****************************** HISTORY

    /*
    public void saveAuditData(DTO dto, ACTION_TYPE action){
    DTO old = abstractConverter.toDto(findById(dto.getId()));
    try {
        if (Utils.compareObjectsDiff(dto, old)) {
            constructAndSaveHistory(dto, action);
        }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public void constructAndSaveHistory(DTO dto, ACTION_TYPE action) {
        User currentUser = getCurrentUser();
        H history = RefelexivityUtil.constructObjectUsingDefaultConstr(historyClass);
        history.setActionType(action.name());
        history.setObjectName(itemClass.getSimpleName());
        history.setObjectId(dto.getId());
        history.setUserId(currentUser.getId());
        history.setUsername(currentUser.getUsername());
        String dtoAsJson = null;
        try {
            dtoAsJson = new ObjectMapper().writeValueAsString(dto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        history.setData(dtoAsJson);
        history.setDate(LocalDateTime.now());
        history.save(history);
    }

    */




    public void configure(Class<T> itemClass, Class<? extends AbstractSpecification<CRITERIA, T>> specificationClass) {
        this.itemClass = itemClass;
        this.specificationClass = specificationClass;
    }

    public abstract void configure();


    public String uploadFile(String checksumOld, String tempUpladedFile,String destinationFilePath) throws Exception {
        String crName = null;
        if (FileUtils.isFileExist(UPLOADED_TEMP_FOLDER, tempUpladedFile)) {
            String filePath = destinationFilePath;
            if (!FileUtils.isFileExist(UPLOADED_TEMP_FOLDER, tempUpladedFile))
                return crName;

            String checksum = MD5Checksum.getMD5Checksum(UPLOADED_TEMP_FOLDER + tempUpladedFile);
            if (!checksum.equals(checksumOld)) {
                throw new BusinessRuleException("errors.file.checksum", new String[]{tempUpladedFile});
            }

            crName = FileUtils.saveFile(UPLOADED_TEMP_FOLDER, UPLOADED_FOLDER, tempUpladedFile, filePath, "");

            if (FileUtils.isFileExist(UPLOADED_FOLDER, crName)) {
                checksum = MD5Checksum.getMD5Checksum(UPLOADED_FOLDER + crName);
                if (!checksum.equals(checksumOld)) {
                    throw new BusinessRuleException("errors.file.checksum", new String[]{""});
                }
            } else {
                throw new BusinessRuleException("errors.file.data.creation", new String[]{""});
            }
        }
        return crName;
    }

    public List<T> importExcel(MultipartFile file) {
        if (isValidExcelFile(file)) {
            try {
                List<T> items = read(file.getInputStream(), getAttributes());
                this.dao.saveAll(items);
                return items;
            } catch (IOException e) {
                throw new IllegalArgumentException("The file is not a valid excel file");
            }
        }
        return null;
    }

    protected List<com.halaq.backend.core.service.Attribute> getAttributes() {
        return new ArrayList<>();
    }

    public boolean isValidExcelFile(MultipartFile file) {
        return Objects.equals(file.getContentType(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    // create a methode that reade the file and take an inputStream as object and return a liste of commandes
    private List<T> read(InputStream inputStream, List<Attribute> attributes) {
        List<T> items = new ArrayList<>();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowIndex;
            int lastRowIndex = sheet.getLastRowNum();
            for (rowIndex = 1; rowIndex <= lastRowIndex; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                int cellIndex = 0;
                T item = itemClass.getDeclaredConstructor().newInstance();
                BeanWrapper beanWrapper = new BeanWrapperImpl(item);
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    String attributeName = attributes.get(cellIndex).getName();
                    String type = attributes.get(cellIndex).getType();
                    Class complexType = attributes.get(cellIndex).getComplexeType();
                    Object value = null;
                    if (cell.getCellType() != CellType.BLANK) {
                        if (type.equals("String")) {
                            value = cell.getStringCellValue();
                        } else if (type.equals("BigDecimal")) {
                            value = BigDecimal.valueOf(cell.getNumericCellValue());
                        } else if (type.equals("Long")) {
                            value = Long.valueOf((long) cell.getNumericCellValue());
                        } else if (type.equals("Boolean")) {
                            if (cell.getCellType() == CellType.NUMERIC) {
                                double numericValue = cell.getNumericCellValue();
                                value = numericValue == 1.0;
                            } else if (cell.getCellType() == CellType.STRING) {
                                value = Boolean.parseBoolean(cell.getStringCellValue());
                            }
                        } else if (type.equals("LocalDateTime")) {
                            if (cell.getCellType() == CellType.NUMERIC) {
                                Date dateValue = cell.getDateCellValue();
                                Instant instant = dateValue.toInstant();
                                value = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
                            } else if (cell.getCellType() == CellType.STRING) {
                                String dateStr = cell.getStringCellValue();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss");
                                value = LocalDateTime.parse(dateStr, formatter);
                            }
                        }
                    }
                    if (complexType != null && value != null) {
                        beanWrapper.setPropertyValue(attributeName.split("\\.")[0], complexType.getDeclaredConstructor().newInstance());
                        beanWrapper.setPropertyValue(attributeName, value);
                    } else if (complexType == null) {
                        beanWrapper.setPropertyValue(attributes.get(cellIndex).getName(), value);
                    }
                    cellIndex++;
                }
                items.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    //************************************************** UPDATE ***********************************
    public List<List<T>> getToBeSavedAndToBeDeleted(List<T> oldList, List<T> newList) {
        List<List<T>> result = new ArrayList<>();
        List<T> resultDelete = new ArrayList<>();
        List<T> resultUpdateOrSave = new ArrayList<>();
        if (ListUtil.isEmpty(oldList) && ListUtil.isNotEmpty(newList)) {
            resultUpdateOrSave.addAll(newList);
        } else if (ListUtil.isEmpty(newList) && ListUtil.isNotEmpty(oldList)) {
            resultDelete.addAll(oldList);
        } else if (ListUtil.isNotEmpty(newList) && ListUtil.isNotEmpty(oldList)) {
            for (int i = 0; i < oldList.size(); i++) {
                T myOld = oldList.get(i);
                T t = newList.stream().filter(e -> myOld.equals(e)).findFirst().orElse(null);
                if (t != null) {
                    resultUpdateOrSave.add(t);
                } else {
                    resultDelete.add(myOld);
                }
            }
            for (int i = 0; i < newList.size(); i++) {
                T myNew = newList.get(i);
                T t = oldList.stream().filter(e -> myNew.equals(e)).findFirst().orElse(null);
                if (t == null) {
                    resultUpdateOrSave.add(myNew);
                }
            }
        }
        result.add(resultUpdateOrSave);
        result.add(resultDelete);
        return result;
    }
}
