package ms.recupera_psswd.application.usecase;

import ms.recupera_psswd.application.port.out.RecuperaSenhaPortOut;
import ms.recupera_psswd.application.port.in.RecuperaSenhaPortIn;
import ms.recupera_psswd.domain.exception.DadoInvalidoException;
import ms.recupera_psswd.domain.model.RecuperaSenha;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RecuperaSenhaUseCase implements RecuperaSenhaPortIn {

    private static final Integer TOKEN_EXPIRATION_HOURS = 24;

    private final Map<String, RecuperaSenha> recuperacoes = new ConcurrentHashMap<>();

    private final RecuperaSenhaPortOut recuperaSenhaPortOut;

    private final String apiUrl;

    public RecuperaSenhaUseCase(RecuperaSenhaPortOut recuperaSenhaPortOut, String apiUrl) {
        this.recuperaSenhaPortOut = recuperaSenhaPortOut;
        this.apiUrl = apiUrl;
    }

    @Override
    public RecuperaSenha solicitarRecuperaSenha(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new DadoInvalidoException("Email é obrigatório");
        }

        String transactionId = UUID.randomUUID().toString();
        String recoveryToken = UUID.randomUUID().toString();

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS);


        RecuperaSenha recuperaSenha = new RecuperaSenha(transactionId, email, recoveryToken, expiresAt);
        recuperacoes.put(transactionId, recuperaSenha);

        String urlRecuperacao = String.format(
            "%s/api/v1/recupera-senha/%s?token=%s",
            apiUrl,
            transactionId,
            recoveryToken
        );

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
