package ms.recupera_psswd.application.model;

import java.time.LocalDateTime;

public class RecuperaSenha {

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

    public boolean isTokenExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void incrementFailedAttempts() {
        this.failedAttempts++;
    }
}
