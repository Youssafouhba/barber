package com.halaq.backend.core.transverse.emailling.converter;

import com.halaq.backend.core.transverse.emailling.dto.EmailTemplateDto;
import com.halaq.backend.core.transverse.emailling.model.EmailTemplate;
import com.halaq.backend.core.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EmailTemplateConverter {




    public EmailTemplate toItem(EmailTemplateDto dto) {
        if (dto == null) {
            return null;
        } else {
            EmailTemplate item = new EmailTemplate();
            if (StringUtil.isNotEmpty(dto.getId()))
                item.setId(dto.getId());
            if (StringUtil.isNotEmpty(dto.getName()))
                item.setName(dto.getName());
            if (StringUtil.isNotEmpty(dto.getSubject()))
                item.setSubject(dto.getSubject());
            if (StringUtil.isNotEmpty(dto.getContent()))
                item.setContent(dto.getContent());
            if (StringUtil.isNotEmpty(dto.getContentType()))
                item.setContentType(dto.getContentType());
            if (StringUtil.isNotEmpty(dto.getDescription()))
                item.setDescription(dto.getDescription());
            if (dto.getIsActive() != null)
                item.setIsActive(dto.getIsActive());
            if (StringUtil.isNotEmpty(dto.getCreatedBy()))
                item.setCreatedBy(dto.getCreatedBy());
            if (StringUtil.isNotEmpty(dto.getCategory()))
                item.setCategory(dto.getCategory());
            if (StringUtil.isNotEmpty(dto.getUpdatedBy()))
                item.setUpdatedBy(dto.getUpdatedBy());
            if (dto.getCreatedAt() != null)
                item.setCreatedAt(dto.getCreatedAt());
            if (dto.getUpdatedAt() != null)
                item.setUpdatedAt(dto.getUpdatedAt());

            return item;
        }
    }


    public EmailTemplateDto toDto(EmailTemplate item) {
        if (item == null) {
            return null;
        } else {
            EmailTemplateDto dto = new EmailTemplateDto();
            if (item.getId() != null)
                dto.setId(item.getId());
            if (StringUtil.isNotEmpty(item.getCategory()))
                dto.setCategory(item.getCategory());
            if (StringUtil.isNotEmpty(item.getName()))
                dto.setName(item.getName());
            if (StringUtil.isNotEmpty(item.getSubject()))
                dto.setSubject(item.getSubject());
            if (StringUtil.isNotEmpty(item.getContent()))
                dto.setContent(item.getContent());
            if (StringUtil.isNotEmpty(item.getContentType()))
                dto.setContentType(item.getContentType());
            if (StringUtil.isNotEmpty(item.getDescription()))
                dto.setDescription(item.getDescription());
            if (item.getIsActive() != null)
                dto.setIsActive(item.getIsActive());
            if (StringUtil.isNotEmpty(item.getCreatedBy()))
                dto.setCreatedBy(item.getCreatedBy());
            if (StringUtil.isNotEmpty(item.getUpdatedBy()))
                dto.setUpdatedBy(item.getUpdatedBy());
            if (item.getCreatedAt() != null)
                dto.setCreatedAt(item.getCreatedAt());
            if (item.getUpdatedAt() != null)
                dto.setUpdatedAt(item.getUpdatedAt());

            return dto;
        }
    }

    public List<EmailTemplateDto> toDto(List<EmailTemplate> list) {
        List<EmailTemplateDto> dtos = new ArrayList<>();
        if (list != null) {
            for (EmailTemplate item : list) {
                dtos.add(toDto(item));
            }
        }
        return dtos;
    }
}