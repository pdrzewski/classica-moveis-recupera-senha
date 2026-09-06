# 🚀 Quick Start Guide - RabbitMQ Email Integration

## 1️⃣ Start the Infrastructure (Docker)

### Prerequisites
- Docker and Docker Compose installed

### Command
```bash
docker-compose up -d
```

### Verify Services
```bash
# Check if containers are running
docker ps | grep classica-

# Output should show:
# - classica-rabbitmq
# - classica-maildev
```

### Access Dashboards
- **RabbitMQ Management:** http://localhost:15672 (guest/guest)
- **MailDev Email Testing:** http://localhost:1080

## 2️⃣ Run the Application

### Option A: IntelliJ IDE
1. Open project in IntelliJ
2. Edit Configurations → Add Spring Boot configuration
3. Set Active Profiles: `dev`
4. Run the application

### Option B: Maven Command
```bash
# Terminal in project root
mvn clean spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Option C: JAR
```bash
mvn clean package
java -jar target/recupera-psswd-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

## 3️⃣ Test the Complete Flow

### Step 1: Request Password Recovery
```bash
curl -X POST http://localhost:8080/api/v1/recupera-senha \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```

Expected Response:
```json
{
  "transactionId": "abc-123-def",
  "message": "Um link de recuperação foi enviado para o seu email",
  "success": true
}
```

### Step 2: Check RabbitMQ Console
- Go to http://localhost:15672
- Click on "Queues"
- Check `recupera-senha-email-queue`
- Should show 0 or low message count (consumer processing them)

### Step 3: Check Email
- Go to http://localhost:1080 (MailDev)
- Should see new email from `noreply@classica-moveis.com`
- Subject: "Recuperação de Senha - Clássica Móveis"
- Email contains the recovery link with transaction ID and token

### Step 4: Extract Recovery Data
From the email, note:
- Transaction ID: `{transactionId}`
- Recovery Token: `{token}`

### Step 5: Verify Token
```bash
curl -X POST "http://localhost:8080/api/v1/recupera-senha/{transactionId}/verificar?token={token}"
```

Expected Response:
```json
{
  "transactionId": "abc-123-def",
  "message": "Token verificado com sucesso",
  "success": true
}
```

### Step 6: Reset Password
```bash
curl -X POST "http://localhost:8080/api/v1/recupera-senha/{transactionId}/resetar" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "{token}",
    "novaSenha": "NovaS3nh@F0rt3"
  }'
```

Expected Response:
```json
{
  "transactionId": "abc-123-def",
  "message": "Senha resetada com sucesso",
  "success": true
}
```

## 4️⃣ Monitor RabbitMQ Messages

### Live Monitoring
```bash
# Watch message flow in real-time
docker logs -f classica-rabbitmq
```

### RabbitMQ Console Details
1. Go to http://localhost:15672
2. Login: guest / guest
3. Sections:
   - **Queues** - View queue status
   - **Connections** - See connected clients
   - **Channels** - Monitor message channels

## 5️⃣ View Application Logs

### Console Output
```bash
# Shows when emails are sent/received
[INFO] Recebido evento de email para: test@example.com
[INFO] Email enviado com sucesso para: test@example.com
```

### Log Levels (application-dev.properties)
```properties
logging.level.ms.recupera_psswd=DEBUG          # Application logs
logging.level.org.springframework.amqp=DEBUG   # RabbitMQ logs
logging.level.org.springframework.mail=DEBUG   # Email logs
```

## 6️⃣ Stop Services

### Stop Infrastructure
```bash
docker-compose down
```

### Remove Volumes (Reset Data)
```bash
docker-compose down -v
```

## 🔧 Troubleshooting

### RabbitMQ Connection Failed
```bash
# Check if container is running
docker ps | grep rabbitmq

# View logs
docker logs classica-rabbitmq

# Restart
docker-compose restart rabbitmq
```

### Email Not Received
1. Check MailDev: http://localhost:1080
2. Check application logs for errors
3. Verify `spring.mail.*` properties in application-dev.properties
4. Check RabbitMQ consumer is running

### Message Stuck in Queue
```bash
# Go to RabbitMQ Console
# Queue tab → recupera-senha-email-queue
# Click "Purge messages" to clear
```

## 📊 Key Metrics

| Component | Status | Access |
|-----------|--------|--------|
| RabbitMQ | Running | :5672 |
| RabbitMQ UI | Available | http://localhost:15672 |
| MailDev SMTP | Running | localhost:1025 |
| MailDev Web | Available | http://localhost:1080 |
| App | Running | http://localhost:8080 |

## 🎯 What Happens Behind the Scenes

```
1. User calls POST /api/v1/recupera-senha
        ↓
2. RecuperaSenhaUseCase.solicitarRecuperaSenha()
        ↓
3. EmailProducer publishes EnviarEmailEvent to RabbitMQ
        ↓
4. Message arrives in recupera-senha-email-queue
        ↓
5. EmailConsumer listens and receives the event
        ↓
6. EmailService sends email via SMTP (MailDev in dev)
        ↓
7. Email appears in MailDev Web UI
        ↓
8. User receives recovery link with token
```

## 📝 Production Checklist

- [ ] Replace MailDev with real SMTP (Gmail, SendGrid, etc.)
- [ ] Update `application-prod.properties`
- [ ] Use environment variables for secrets
- [ ] Enable RabbitMQ SSL/TLS
- [ ] Configure message persistence
- [ ] Set up Dead Letter Queue
- [ ] Add retry policies
- [ ] Implement rate limiting
- [ ] Add monitoring/alerting
- [ ] Test failover scenarios

## 🆘 Need Help?

1. Check logs: `docker logs classica-rabbitmq`
2. Review setup guide: See `RABBITMQ_SETUP.md`
3. Common issues: Check troubleshooting section above
4. RabbitMQ docs: https://www.rabbitmq.com/documentation.html

