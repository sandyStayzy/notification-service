# SMS Integration Setup

## Overview

The notification system supports two SMS channels:
1. **Console SMS Channel** (default) - Logs SMS content to console
2. **Twilio SMS Channel** - Sends real SMS via Twilio API

## Quick Setup

### 1. Enable Twilio SMS Channel

In `application.yml`, set:
```yaml
notification:
  channels:
    sms:
      twilio:
        enabled: true  # Enable Twilio SMS
        account-sid: ${TWILIO_ACCOUNT_SID:your-account-sid}
        auth-token: ${TWILIO_AUTH_TOKEN:your-auth-token}
        from-number: ${TWILIO_FROM_NUMBER:+1234567890}
      console:
        enabled: false # Disable console (optional)
```

### 2. Set Environment Variables

```bash
export TWILIO_ACCOUNT_SID="ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
export TWILIO_AUTH_TOKEN="your-auth-token"
export TWILIO_FROM_NUMBER="+1234567890"
```

Then start the application:
```bash
mvn spring-boot:run
```

## Twilio Account Setup

### 1. Create Twilio Account
- Go to [twilio.com](https://www.twilio.com)
- Sign up for a free account
- Get $15 free credit for testing

### 2. Get Credentials
- **Account SID**: Found on dashboard (starts with "AC")
- **Auth Token**: Found on dashboard (click "Show" to reveal)

### 3. Get Phone Number
- Go to Console → Phone Numbers → Manage → Buy a number
- Choose a number with SMS capability
- Note the purchased number (e.g., +1234567890)

### 4. Verify Phone Numbers (Trial Account)
- Trial accounts can only send to verified numbers
- Go to Console → Phone Numbers → Manage → Verified Caller IDs
- Add your phone number for testing

## Features

### Smart SMS Formatting
- **Title Integration**: Adds emoji and title to message
- **Content Optimization**: Truncates long messages to fit SMS limits
- **Metadata Filtering**: Includes only key metadata in SMS
- **Priority Handling**: Adds urgency indicators for HIGH priority
- **Character Limits**: Respects 160-character SMS limits

### Phone Number Processing
- **Auto-formatting**: Cleans and formats phone numbers
- **Country Code**: Automatically adds +1 for US numbers
- **Validation**: Validates international phone number format
- **Error Handling**: Clear error messages for invalid numbers

### International Support
- Supports international phone numbers
- Proper country code handling
- Twilio global SMS coverage

## Testing

### 1. Send Test SMS
```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "🚀 SMS Test",
    "content": "This is a test SMS from the notification system. Your verification code is 123456.",
    "channelType": "SMS",
    "priority": "HIGH",
    "metadata": {
      "code": "123456",
      "expires": "10min"
    }
  }'
```

### 2. Check Console Output
You should see:
```
📱 TWILIO SMS SENT
=============================================
To: +1234567890
From: +1555123456
Priority: HIGH
Message SID: SMxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
Status: queued
Character Count: 87
Service: Twilio
=============================================
```

### 3. Check Your Phone
- You should receive the SMS on your phone
- Message will include title, content, key metadata
- HIGH priority messages show ⚠️ URGENT

## Advanced Configuration

### Message Templates
The system automatically formats SMS messages:

```
📱 [Title]

[Content]

key: value
type: urgent

⚠️ URGENT (for HIGH priority)
```

### Phone Number Formats Supported
- `+1234567890` (international format)
- `1234567890` (US format, auto-adds +1)
- `(123) 456-7890` (formatted US, cleaned automatically)
- `123-456-7890` (formatted US, cleaned automatically)

### Character Limit Handling
- Content is truncated at 155 characters
- Adds "..." if truncated
- Prioritizes title and main content
- Limits metadata to 2 key items

## Error Handling

### Common Issues

**1. Invalid Account SID/Auth Token**
```
Twilio SMS failed: Authenticate
```
- Verify credentials in Twilio Console
- Check environment variables are set correctly

**2. Invalid Phone Number**
```
Invalid phone number format for user: 123456
```
- Ensure phone number includes country code
- Use format: +1234567890

**3. Unverified Number (Trial Account)**
```
Twilio SMS failed: The number +1234567890 is unverified
```
- Add phone number to verified caller IDs
- Or upgrade to paid account

**4. Insufficient Balance**
```
Twilio SMS failed: Account not authorized
```
- Add funds to Twilio account
- Check account balance

### Debug Mode
Enable debug logging:
```yaml
logging:
  level:
    com.twilio: DEBUG
    com.notification.system.service.channel.impl.TwilioSmsChannel: DEBUG
```

## Production Considerations

### 1. Upgrade to Paid Account
- Remove phone number verification requirement
- Lower per-message costs
- Higher rate limits

### 2. Webhook Configuration
```yaml
notification:
  channels:
    sms:
      twilio:
        webhook-url: https://yourdomain.com/webhooks/sms-status
```

### 3. Message Templates
Consider creating templates for different message types:
- Verification codes
- Alerts and notifications  
- Marketing messages (with opt-out)

### 4. Rate Limiting
- Implement rate limiting for SMS sending
- Consider costs for high-volume sending
- Monitor usage via Twilio Console

### 5. Compliance
- Follow SMS marketing laws (CAN-SPAM, TCPA)
- Implement opt-out mechanisms
- Include clear sender identification

## Alternative SMS Providers

### AWS SNS SMS
```yaml
notification:
  channels:
    sms:
      aws-sns:
        enabled: true
        access-key: ${AWS_ACCESS_KEY}
        secret-key: ${AWS_SECRET_KEY}
        region: ${AWS_REGION:us-east-1}
```

### Other Providers
- **Nexmo/Vonage**: Global SMS provider
- **MessageBird**: European-focused provider  
- **Plivo**: Developer-friendly SMS API
- **TextMagic**: Business SMS platform

## Monitoring

### Key Metrics to Track
- SMS delivery rate
- Message costs
- Response times
- Error rates by phone number/country

### Twilio Console
- Monitor usage and costs
- View message logs and status
- Set up usage alerts
- Download usage reports

### Application Metrics
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,sms-stats
```