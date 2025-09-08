package com.notification.system.service.channel;

import com.notification.system.model.dto.response.NotificationResult;
import com.notification.system.model.entity.Notification;
import com.notification.system.model.enums.ChannelType;

public interface NotificationChannel {
    
    NotificationResult send(Notification notification);
    
    boolean supports(ChannelType channelType);
    
    ChannelType getChannelType();
    
    String getChannelName();
}