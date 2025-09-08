# SMTP Email Configuration

## Overview

The notification system supports two email channels:
1. **Console Email Channel** (default) - Logs email content to console
2. **SMTP Email Channel** - Sends real emails via SMTP server

## Quick Setup

### 1. Enable SMTP Email Channel

In `application.yml`, set:
```yaml
notification:
  channels:
    email:
      smtp:
        enabled: true  # Enable SMTP
      console:
        enabled: false # Disable console (optional)
```

### 2. Configure SMTP Settings

The system supports environment variables for secure credential management:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${SMTP_USERNAME:your-email@gmail.com}
    password: ${SMTP_PASSWORD:your-app-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
```

### 3. Set Environment Variables

```bash
export SMTP_USERNAME="your-email@gmail.com"
export SMTP_PASSWORD="your-app-password"
```

Then start the application:
```bash
mvn spring-boot:run
```

## Gmail Setup

### 1. Enable 2-Factor Authentication
- Go to Google Account settings
- Enable 2-Factor Authentication

### 2. Generate App Password
- Go to Google Account → Security → App passwords
- Generate password for "Mail"
- Use this password as `SMTP_PASSWORD`

### 3. Alternative: Less Secure Apps (Not Recommended)
- Go to Google Account → Security
- Enable "Less secure app access"
- Use your regular password

## Other Email Providers

### Outlook/Hotmail
```yaml
spring:
  mail:
    host: smtp.office365.com
    port: 587
```

### Yahoo
```yaml
spring:
  mail:
    host: smtp.mail.yahoo.com
    port: 587
```

### Custom SMTP
```yaml
spring:
  mail:
    host: your-smtp-server.com
    port: 587 # or 465 for SSL
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true # Use false for port 465
```

## Features

### Rich HTML Emails
The SMTP channel sends beautiful HTML emails with:
- Professional styling and layout
- Responsive design for mobile devices
- Embedded metadata information
- Branded footer with timestamp

### Fallback Behavior
- If SMTP fails, the system logs the error
- Console channel can run alongside as backup
- Priority system ensures SMTP is tried first when enabled

### Configuration Priority
1. **SMTP Email Channel** (`@Order(1)`) - When `smtp.enabled=true`
2. **Console Email Channel** (`@Order(2)`) - When `console.enabled=true`

## Testing

### 1. Send Test Email
```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "SMTP Test Email",
    "content": "This is a test of the SMTP email system with rich HTML formatting!",
    "channelType": "EMAIL",
    "priority": "HIGH",
    "metadata": {
      "test": "smtp_integration",
      "version": "2.0"
    }
  }'
```

### 2. Check Console Output
You should see:
```
📧 SMTP EMAIL SENT
=============================================
To: user@example.com
From: noreply@notificationservice.com
Subject: SMTP Test Email
Priority: HIGH
Email Format: Rich HTML with styling
=============================================
```

### 3. Check Your Email
- Look for the email in your inbox
- Note the professional HTML formatting
- Verify metadata is displayed properly

## Troubleshooting

### Common Issues

**1. Authentication Failed**
- Verify SMTP credentials
- Check if 2FA is enabled (use app password)
- Ensure less secure apps are enabled (if not using app password)

**2. Connection Timeout**
- Check firewall settings
- Verify SMTP server and port
- Try different ports (587, 465, 25)

**3. SSL/TLS Issues**
- For port 465: Set `starttls.enable=false` and use SSL
- For port 587: Keep `starttls.enable=true`

**4. Rate Limiting**
- Gmail: 500 emails/day for free accounts
- Consider using professional email service for production

### Debug Mode
Enable debug logging:
```yaml
logging:
  level:
    org.springframework.mail: DEBUG
    com.notification.system: DEBUG
```

## Production Recommendations

### 1. Use Professional Email Service
- AWS SES
- SendGrid
- Mailgun
- Postmark

### 2. Environment-Specific Configuration
```yaml
# application-prod.yml
spring:
  mail:
    host: ${SMTP_HOST}
    port: ${SMTP_PORT:587}
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}
```

### 3. Error Handling
- Implement retry mechanisms
- Set up monitoring for email failures
- Configure dead letter queues for failed emails

### 4. Security
- Never commit SMTP credentials to version control
- Use encrypted environment variables
- Rotate credentials regularly
- Monitor for suspicious activity