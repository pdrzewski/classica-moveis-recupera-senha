## RabbitMQ Email Integration Setup Guide

### Overview
This microservice now uses RabbitMQ to asynchronously send password recovery emails. When a user requests a password reset, the request is published to a RabbitMQ queue, and a consumer processes it to send the email.

### Architecture
```
RecuperaSenhaUseCase 
    ↓ (publishes event)
EmailProducer 
    ↓ (sends to RabbitMQ)
RabbitMQ Exchange/Queue
    ↓ (routes to)
EmailConsumer 
    ↓ (calls)
EmailService 
    ↓ (sends via SMTP)
User Email
```

### Prerequisites
1. **RabbitMQ Server** - Install and running
2. **Email Service** - Gmail, SendGrid, or any SMTP server
3. **Java 17+** - Already configured
4. **Maven** - For dependency management

### Installation & Setup

#### 1. Install RabbitMQ (Docker - Recommended)
```bash
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3.13-management
```

**Access RabbitMQ Management Console:**
- URL: http://localhost:15672
- Username: guest
- Password: guest

#### 2. Configure Email (Gmail Example)
1. Enable 2-Step Verification on your Google Account
2. Generate an App Password:
   - Go to https://myaccount.google.com/apppasswords
   - Select Mail and Windows Computer
   - Copy the generated 16-character password

#### 3. Update application.properties
```properties
# RabbitMQ Configuration
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.virtual-host=/

# Email Configuration (Gmail Example)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=seu-email@gmail.com
spring.mail.password=xxxx-xxxx-xxxx-xxxx  # 16-char app password
spring.mail.from=seu-email@gmail.com
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

# Application Configuration
app.api.url=http://localhost:8080
```

#### 4. Run the Application
```bash
mvn clean spring-boot:run
```

### Testing the Flow

#### Step 1: Request Password Recovery
```bash
curl -X POST http://localhost:8080/api/v1/recupera-senha \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com"
  }'
```

**Response:**
```json
{
  "transactionId": "uuid-here",
  "message": "Um link de recuperação foi enviado para o seu email",
  "success": true
}
```

#### Step 2: Monitor RabbitMQ
- Check the RabbitMQ console: http://localhost:15672
- Look for queue: `recupera-senha-email-queue`
- Verify messages are being published and consumed

#### Step 3: Check Email
- Check the user's email for recovery link
- Link format: `http://localhost:8080/api/v1/recupera-senha/{transactionId}?token={token}`

#### Step 4: Verify Token (Optional)
```bash
curl -X POST http://localhost:8080/api/v1/recupera-senha/{transactionId}/verificar?token={token}
```

#### Step 5: Reset Password
```bash
curl -X POST http://localhost:8080/api/v1/recupera-senha/{transactionId}/resetar \
  -H "Content-Type: application/json" \
  -d '{
    "token": "token-aqui",
    "novaSenha": "NovaS3nh@Forte"
  }'
```

### File Structure
```
src/main/java/ms/recupera_psswd/
├── adapter/
│   ├── out/
│   │   ├── messaging/
│   │   │   ├── config/
│   │   │   │   └── RabbitMQConfig.java          # Queue & Exchange setup
│   │   │   ├── event/
│   │   │   │   └── EnviarEmailEvent.java        # Message payload
│   │   │   ├── producer/
│   │   │   │   └── EmailProducer.java           # Publishes to RabbitMQ
│   │   │   └── consumer/
│   │   │       └── EmailConsumer.java           # Consumes from RabbitMQ
│   │   └── email/
│   │       └── EmailService.java                # SMTP email sending
├── application/
│   ├── usecase/
│   │   └── RecuperaSenhaUseCase.java           # Updated with email publishing
```

### RabbitMQ Configuration Details

**Queue:** `recupera-senha-email-queue`
- Durable: Yes (survives broker restart)
- Auto-delete: No
- Exclusive: No

**Exchange:** `recupera-senha-exchange`
- Type: Direct
- Durable: Yes

**Routing Key:** `recupera-senha.email`

### Error Handling

If email fails to send:
1. **Check RabbitMQ Connection:** Verify `spring.rabbitmq.*` properties
2. **Check Email Credentials:** Test SMTP settings
3. **Review Logs:** Look for stack traces in console
4. **Dead Letter Queue:** Consider adding DLQ for failed messages

### Performance Tips

1. **Batch Processing:** Configure consumer thread pool in `application.properties`:
```properties
spring.rabbitmq.listener.simple.concurrency=5
spring.rabbitmq.listener.simple.max-concurrency=10
```

2. **Message Persistence:** Already enabled in `RabbitMQConfig`

3. **Retry Policy:** Consider adding:
```properties
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.initial-interval=1000
spring.rabbitmq.listener.simple.retry.max-attempts=3
```

### Security Considerations

1. **Store Credentials:** Use environment variables for email passwords
```bash
export SPRING_MAIL_PASSWORD=your-app-password
export SPRING_RABBITMQ_PASSWORD=rabbitmq-password
```

2. **Use SSL for RabbitMQ:**
```properties
spring.rabbitmq.ssl.enabled=true
spring.rabbitmq.ssl.key-store=path/to/keystore.p12
spring.rabbitmq.ssl.key-store-password=password
```

3. **Token Expiration:** Default is 24 hours (configurable in `RecuperaSenhaUseCase`)

4. **Rate Limiting:** Consider adding to prevent email bombing

### Troubleshooting

| Issue | Solution |
|-------|----------|
| "Cannot connect to RabbitMQ" | Check if RabbitMQ is running on localhost:5672 |
| "Authentication failed" | Verify username/password in properties |
| "Email not received" | Check app password, enable less secure apps for Gmail |
| "Message stuck in queue" | Check consumer logs, verify email config |
| "Connection timeout" | Increase timeout values in properties |

### Future Enhancements

- [ ] Add message retry logic with exponential backoff
- [ ] Implement Dead Letter Queue (DLQ) for failed messages
- [ ] Add email templates (HTML emails)
- [ ] Implement message tracking/logging
- [ ] Add SMS as alternative notification channel
- [ ] Create admin dashboard for recovery requests
- [ ] Add rate limiting for email requests

