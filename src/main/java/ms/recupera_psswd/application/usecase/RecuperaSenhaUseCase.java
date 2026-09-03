package ms.recupera_psswd.application.usecase;

import ms.recupera_psswd.adapter.out.messaging.producer.EmailProducer;
import ms.recupera_psswd.application.model.RecuperaSenha;
import ms.recupera_psswd.application.model.RecuperaSenhaStatus;
import ms.recupera_psswd.application.port.in.RecuperaSenhaPortIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RecuperaSenhaUseCase implements RecuperaSenhaPortIn {

    private static final Integer MAX_FAILED_ATTEMPTS = 5;
    private static final Integer TOKEN_EXPIRATION_HOURS = 24;

    @Autowired
    private EmailProducer emailProducer;

    @Value("${app.api.url:http://localhost:8080}")
    private String apiUrl;

    @Override
    public RecuperaSenha solicitarRecuperaSenha(String email) {
        // Validate email
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }

        // Generate transaction ID and recovery token
        String transactionId = UUID.randomUUID().toString();
        String recoveryToken = UUID.randomUUID().toString();

        // Create expiration time (24 hours from now)
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS);

        // Create and return the RecuperaSenha object
        RecuperaSenha recuperaSenha = new RecuperaSenha(transactionId, email, recoveryToken, expiresAt);

        // Build the recovery URL
        String urlRecuperacao = String.format(
            "%s/api/v1/recupera-senha/%s?token=%s",
            apiUrl,
            transactionId,
            recoveryToken
        );

        // Publish email event to RabbitMQ
        emailProducer.publisharEmailRecuperaSenha(transactionId, email, recoveryToken, urlRecuperacao);

        // TODO: Implement persistence layer to save recovery request

        return recuperaSenha;
    }

    @Override
    public RecuperaSenha verificarToken(String transactionId, String token) {
        // Validate inputs
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID é obrigatório");
        }
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token é obrigatório");
        }

        // TODO: Fetch from database
        RecuperaSenha recuperaSenha = null; // Placeholder

        if (recuperaSenha == null) {
            throw new IllegalArgumentException("Solicitação de recuperação de senha não encontrada");
        }

        // Check if token is expired
        if (recuperaSenha.isTokenExpired()) {
            recuperaSenha.setStatus(RecuperaSenhaStatus.EXPIRED);
            // TODO: Save to database
            throw new IllegalArgumentException("Token de recuperação expirado");
        }

        // Check if token matches
        if (!recuperaSenha.getRecoveryToken().equals(token)) {
            recuperaSenha.incrementFailedAttempts();

            // Lock if too many failed attempts
            if (recuperaSenha.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                recuperaSenha.setStatus(RecuperaSenhaStatus.CANCELLED);
            }

            // TODO: Save to database
            throw new IllegalArgumentException("Token inválido");
        }

        // Token is valid
        recuperaSenha.setStatus(RecuperaSenhaStatus.VERIFIED);
        recuperaSenha.setFailedAttempts(0); // Reset attempts on success
        // TODO: Save to database

        return recuperaSenha;
    }

    @Override
    public RecuperaSenha resetarSenha(String transactionId, String token, String novaSenha) {
        // Verify token first
        RecuperaSenha recuperaSenha = verificarToken(transactionId, token);

        // Validate new password
        if (novaSenha == null || novaSenha.length() < 8) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 8 caracteres");
        }

        // Mark as completed
        recuperaSenha.setStatus(RecuperaSenhaStatus.COMPLETED);
        recuperaSenha.setUsedAt(LocalDateTime.now());

        // TODO: Update user password in database
        // TODO: Save recuperaSenha to database

        return recuperaSenha;
    }
}
