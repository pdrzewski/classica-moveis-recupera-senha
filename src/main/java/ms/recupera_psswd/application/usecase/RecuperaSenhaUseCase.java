package ms.recupera_psswd.application.usecase;

import ms.recupera_psswd.application.port.out.RecuperaSenhaPortOut;
import ms.recupera_psswd.application.port.in.RecuperaSenhaPortIn;
import ms.recupera_psswd.domain.exception.DadoInvalidoException;
import ms.recupera_psswd.domain.model.RecuperaSenha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecuperaSenhaUseCase implements RecuperaSenhaPortIn {

    private static final Integer TOKEN_EXPIRATION_HOURS = 24;

    /*
     * Temporary persistence for the recovery flow. This keeps the request
     * available between the POST that sends the email and the click on the
     * link. Replace this map with a database-backed adapter in production.
     */
    private final Map<String, RecuperaSenha> recuperacoes = new ConcurrentHashMap<>();

    @Autowired
    private RecuperaSenhaPortOut recuperaSenhaPortOut;

    @Value("${app.api.url:http://localhost:8080}")
    private String apiUrl;

    @Override
    public RecuperaSenha solicitarRecuperaSenha(String email) {
        // Validate email
        if (email == null || email.trim().isEmpty()) {
            throw new DadoInvalidoException("Email é obrigatório");
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
        recuperaSenhaPortOut.publicarEmailRecuperaSenha(transactionId, email, recoveryToken, urlRecuperacao);

        return recuperaSenha;
    }

    @Override
    public RecuperaSenha verificarToken(String transactionId, String token) {
        RecuperaSenha recuperaSenha = buscarRecuperacao(transactionId);

        recuperaSenha.verificarToken(token);
        return recuperaSenha;
    }

    @Override
    public RecuperaSenha resetarSenha(String transactionId, String token, String novaSenha) {
        RecuperaSenha recuperaSenha = buscarRecuperacao(transactionId);

        recuperaSenha.resetarSenha(token, novaSenha);

        // TODO: Update the user's password in the user database.

        return recuperaSenha;
    }

    private RecuperaSenha buscarRecuperacao(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new DadoInvalidoException("Transaction ID é obrigatório");
        }

        RecuperaSenha recuperaSenha = recuperacoes.get(transactionId);
        if (recuperaSenha == null) {
            throw new DadoInvalidoException("Solicitação de recuperação de senha não encontrada");
        }

        return recuperaSenha;
    }
}
