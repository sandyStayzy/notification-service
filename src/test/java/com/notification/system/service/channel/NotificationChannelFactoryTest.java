package com.notification.system.service.channel;

import com.notification.system.model.enums.ChannelType;
import com.notification.system.service.channel.impl.EmailChannel;
import com.notification.system.service.channel.impl.PushChannel;
import com.notification.system.service.channel.impl.SmsChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NotificationChannelFactoryTest {
    
    private NotificationChannelFactory factory;
    
    @BeforeEach
    void setUp() {
        List<NotificationChannel> channels = List.of(
                new EmailChannel(),
                new SmsChannel(), 
                new PushChannel()
        );
        factory = new NotificationChannelFactory(channels);
    }
    
    @Test
    void testGetEmailChannel() {
        Optional<NotificationChannel> channel = factory.getChannel(ChannelType.EMAIL);
        
        assertTrue(channel.isPresent());
        assertEquals(ChannelType.EMAIL, channel.get().getChannelType());
        assertTrue(channel.get() instanceof EmailChannel);
    }
    
    @Test
    void testGetSmsChannel() {
        Optional<NotificationChannel> channel = factory.getChannel(ChannelType.SMS);
        
        assertTrue(channel.isPresent());
        assertEquals(ChannelType.SMS, channel.get().getChannelType());
        assertTrue(channel.get() instanceof SmsChannel);
    }
    
    @Test
    void testGetPushChannel() {
        Optional<NotificationChannel> channel = factory.getChannel(ChannelType.PUSH);
        
        assertTrue(channel.isPresent());
        assertEquals(ChannelType.PUSH, channel.get().getChannelType());
        assertTrue(channel.get() instanceof PushChannel);
    }
    
    @Test
    void testGetUnsupportedChannel() {
        Optional<NotificationChannel> channel = factory.getChannel(ChannelType.SLACK);
        
        assertFalse(channel.isPresent());
    }
    
    @Test
    void testIsChannelSupported() {
        assertTrue(factory.isChannelSupported(ChannelType.EMAIL));
        assertTrue(factory.isChannelSupported(ChannelType.SMS));
        assertTrue(factory.isChannelSupported(ChannelType.PUSH));
        assertFalse(factory.isChannelSupported(ChannelType.SLACK));
        assertFalse(factory.isChannelSupported(ChannelType.WHATSAPP));
    }
    
    @Test
    void testGetAllChannels() {
        List<NotificationChannel> channels = factory.getAllChannels();
        
        assertEquals(3, channels.size());
        assertTrue(channels.stream().anyMatch(c -> c instanceof EmailChannel));
        assertTrue(channels.stream().anyMatch(c -> c instanceof SmsChannel));
        assertTrue(channels.stream().anyMatch(c -> c instanceof PushChannel));
    }
}