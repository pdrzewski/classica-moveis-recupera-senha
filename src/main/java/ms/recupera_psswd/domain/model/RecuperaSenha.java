package ms.recupera_psswd.domain.model;

import ms.recupera_psswd.domain.enums.RecuperaSenhaStatus;
import ms.recupera_psswd.domain.exception.DadoInvalidoException;
import ms.recupera_psswd.domain.exception.TokenExpiradoException;

import java.time.LocalDateTime;

public class RecuperaSenha {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private String transactionId;
    private String email;
    private String recoveryToken;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private RecuperaSenhaStatus status;
    private Integer failedAttempts;

    public RecuperaSenha(String transactionId, String email, String recoveryToken, LocalDateTime expiresAt) {
        this.transactionId = transactionId;
        this.email = email;
        this.recoveryToken = recoveryToken;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.status = RecuperaSenhaStatus.PENDING;
        this.failedAttempts = 0;
    }

    public void verificarToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new DadoInvalidoException("Token é obrigatório");
        }

        if (isTokenExpired()) {
            status = RecuperaSenhaStatus.EXPIRED;
            throw new TokenExpiradoException("Token de recuperação expirado");
        }

        if (status == RecuperaSenhaStatus.CANCELLED) {
            throw new DadoInvalidoException("Solicitação de recuperação de senha cancelada");
        }

        if (status == RecuperaSenhaStatus.COMPLETED) {
            throw new DadoInvalidoException("Token de recuperação já utilizado");
        }

        if (!recoveryToken.equals(token)) {
            incrementFailedAttempts();
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                status = RecuperaSenhaStatus.CANCELLED;
            }
            throw new DadoInvalidoException("Token inválido");
        }

        status = RecuperaSenhaStatus.VERIFIED;
        failedAttempts = 0;
    }

    public void resetarSenha(String token, String novaSenha) {
        verificarToken(token);

        if (novaSenha == null || novaSenha.length() < 8) {
            throw new DadoInvalidoException("Senha deve ter pelo menos 8 caracteres");
        }

        status = RecuperaSenhaStatus.COMPLETED;
        usedAt = LocalDateTime.now();
    }

    public boolean isTokenExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    private void incrementFailedAttempts() {
        failedAttempts++;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRecoveryToken() {
        return recoveryToken;
    }

    public void setRecoveryToken(String recoveryToken) {
        this.recoveryToken = recoveryToken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public RecuperaSenhaStatus getStatus() {
        return status;
    }

    public void setStatus(RecuperaSenhaStatus status) {
        this.status = status;
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
}
