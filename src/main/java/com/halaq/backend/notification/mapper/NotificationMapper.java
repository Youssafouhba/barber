package com.halaq.backend.notification.mapper;

import com.halaq.backend.notification.dto.NotificationDto;
import com.halaq.backend.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationDto toDto(Notification notification);

    @Mapping(target = "recipient", ignore = true)
    @Mapping(target = "id", ignore = true)
    Notification toEntity(NotificationDto notificationDto);
}
