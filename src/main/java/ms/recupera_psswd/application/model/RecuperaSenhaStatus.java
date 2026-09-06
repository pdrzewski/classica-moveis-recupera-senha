package ms.recupera_psswd.application.model;

public enum RecuperaSenhaStatus {
    PENDING,      // Initial state - recovery request created
    VERIFIED,     // User clicked the link/verified the token
    COMPLETED,    // Password reset completed
    EXPIRED,      // Token expired
    CANCELLED     // User cancelled the recovery
}

