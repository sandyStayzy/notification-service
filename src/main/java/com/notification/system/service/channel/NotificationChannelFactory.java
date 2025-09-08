package com.notification.system.service.channel;

import com.notification.system.model.enums.ChannelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class NotificationChannelFactory {
    
    private final List<NotificationChannel> channels;
    
    @Autowired
    public NotificationChannelFactory(List<NotificationChannel> channels) {
        this.channels = channels;
    }
    
    public Optional<NotificationChannel> getChannel(ChannelType channelType) {
        return channels.stream()
                .filter(channel -> channel.supports(channelType))
                .findFirst();
    }
    
    public List<NotificationChannel> getAllChannels() {
        return channels;
    }
    
    public boolean isChannelSupported(ChannelType channelType) {
        return channels.stream()
                .anyMatch(channel -> channel.supports(channelType));
    }
}