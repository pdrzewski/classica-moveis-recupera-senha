package ms.recupera_psswd.application.usecase;

import ms.recupera_psswd.adapter.out.messaging.producer.EmailProducer;
import ms.recupera_psswd.application.model.RecuperaSenha;
import ms.recupera_psswd.application.enums.RecuperaSenhaStatus;
import ms.recupera_psswd.application.port.in.RecuperaSenhaPortIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecuperaSenhaUseCase implements RecuperaSenhaPortIn {

    private static final Integer MAX_FAILED_ATTEMPTS = 5;
    private static final Integer TOKEN_EXPIRATION_HOURS = 24;

    /*
     * Temporary persistence for the recovery flow. This keeps the request
     * available between the POST that sends the email and the click on the
     * link. Replace this map with a database-backed adapter in production.
     */
    private final Map<String, RecuperaSenha> recuperacoes = new ConcurrentHashMap<>();

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

        // Store the request before publishing the email event. The link can
        // only be verified if the request is already available here.
        RecuperaSenha recuperaSenha = new RecuperaSenha(transactionId, email, recoveryToken, expiresAt);
        recuperacoes.put(transactionId, recuperaSenha);

        // Build the recovery URL
        String urlRecuperacao = String.format(
            "%s/api/v1/recupera-senha/%s?token=%s",
            apiUrl,
            transactionId,
            recoveryToken
        );

        // Publish email event to RabbitMQ
        emailProducer.publisharEmailRecuperaSenha(transactionId, email, recoveryToken, urlRecuperacao);

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

        RecuperaSenha recuperaSenha = recuperacoes.get(transactionId);

        if (recuperaSenha == null) {
            throw new IllegalArgumentException("Solicitação de recuperação de senha não encontrada");
        }

        // Check if token is expired
        if (recuperaSenha.isTokenExpired()) {
            recuperaSenha.setStatus(RecuperaSenhaStatus.EXPIRED);
            throw new IllegalArgumentException("Token de recuperação expirado");
        }

        if (recuperaSenha.getStatus() == RecuperaSenhaStatus.CANCELLED) {
            throw new IllegalArgumentException("Solicitação de recuperação de senha cancelada");
        }

        if (recuperaSenha.getStatus() == RecuperaSenhaStatus.COMPLETED) {
            throw new IllegalArgumentException("Token de recuperação já utilizado");
        }

        // Check if token matches
        if (!recuperaSenha.getRecoveryToken().equals(token)) {
            recuperaSenha.incrementFailedAttempts();

            // Lock if too many failed attempts
            if (recuperaSenha.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                recuperaSenha.setStatus(RecuperaSenhaStatus.CANCELLED);
            }

            throw new IllegalArgumentException("Token inválido");
        }

        // Token is valid
        recuperaSenha.setStatus(RecuperaSenhaStatus.VERIFIED);
        recuperaSenha.setFailedAttempts(0); // Reset attempts on success
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

        // TODO: Update the user's password in the user database.

        return recuperaSenha;
    }
}
