package com.halaq.backend.core.converter;

import com.halaq.backend.core.dto.BaseDto;
import com.halaq.backend.core.entity.BaseEntity;
import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractConverter<T extends BaseEntity, DTO extends BaseDto> {
    protected Class<T> itemType;
    protected Class<DTO> dtoType;

    protected AbstractConverter(Class<T> itemType, Class<DTO> dtoType) {
        this.itemType = itemType;
        this.dtoType = dtoType;
        this.init(true);
    }

    public abstract T toItem(DTO dto);

    public void copy(DTO dto, T t) {
        if (dto != null && t != null) {
            copyNonNullProperties(dto, t);
        }
    }

    public List<T> copy(List<DTO> dtos) {
        List<T> result = new ArrayList<>();
        if (dtos != null) {
            for (DTO dto : dtos) {
                T instance = null;
                try {
                    instance = itemType.getDeclaredConstructor().newInstance();
                    copy(dto, instance);
                } catch (Exception e) {

                }
                if (instance != null) {
                    result.add(instance);
                }
            }
        }
        return result.isEmpty() ? null : result;
    }


    public List<T> toItem(List<DTO> dtos) {
        List<T> items = new ArrayList<>();
        if (dtos != null && !dtos.isEmpty()) {
            for (DTO DTO : dtos) {
                items.add(toItem(DTO));
            }
        }
        return items;
    }


    public List<DTO> toDto(List<T> items) {
        List<DTO> dtos = new ArrayList<>();
        if (items != null && !items.isEmpty()) {
            for (T item : items) {
                dtos.add(toDto(item));
            }
        }
        return dtos;
    }
    public List<DTO> toDto(List<T> items, boolean simpleMode) {
        List<DTO> dtos = new ArrayList<>();
        if (items != null && !items.isEmpty()) {
            for (T item : items) {
                dtos.add(mapToDtoInternal(item, new CycleAvoidingMappingContext(simpleMode)));
            }
        }
        return dtos;
    }
    public DTO toDto(T item) {
        return mapToDtoInternal(item, new CycleAvoidingMappingContext());
    }

    // --- NOUVELLES METHODES ---

    /**
     * Convertit en DTO "Léger" (Tout simple)
     */
    public DTO toSimpleDto(T item) {
        if (item == null) return null;
        // On crée un contexte en mode "Global Simple" (true)
        return mapToDtoInternal(item, new CycleAvoidingMappingContext(true));
    }

    /**
     * Convertit en DTO "Complet"
     */
    public DTO toFullDto(T item) {
        return toDto(item); // Alias vers toDto standard
    }

    /**
     * Conversion "À la carte" (Ex: Barber en simple)
     */
    public DTO toCustomDto(T item, Class<?>... typesToSimplify) {
        if (item == null) return null;

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        for (Class<?> clazz : typesToSimplify) {
            context.addSimpleType(clazz);
        }

        // CORRECTION CLÉ : On passe le contexte complet ici !
        return mapToDtoInternal(item, context);
    }

    public List<DTO> toCustomDto(List<T> items, Class<?>... typesToSimplify) {
        if (items == null || items.isEmpty()) return new ArrayList<>();

        // On crée le contexte une seule fois pour optimiser
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        for (Class<?> clazz : typesToSimplify) {
            context.addSimpleType(clazz);
        }

        List<DTO> dtos = new ArrayList<>();
        for (T item : items) {
            dtos.add(mapToDtoInternal(item, context));
        }
        return dtos;
    }

    // --- CORRECTION DE LA METHODE ABSTRAITE ---
    // Au lieu de 'boolean simpleMode', on prend le 'context' complet
    protected abstract DTO mapToDtoInternal(T item, CycleAvoidingMappingContext context);
    public void initObject(boolean initialisationObject) {

    }

    public void initList(boolean initialisationList) {

    }

    public void init(boolean initialisation) {
        initObject(initialisation);
        initList(initialisation);
    }


    private void copyNonNullProperties(Object src, Object target) {
        BeanUtils.copyProperties(src, target, AbstractConverterHelper.getNullPropertyNames(src));
    }


}
