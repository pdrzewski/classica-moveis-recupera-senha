package ms.recupera_psswd.adapter.in.api.rest.dto;

public class RecuperaSenhaResponse {
    private String transactionId;
    private String message;
    private boolean success;

    public RecuperaSenhaResponse() {
    }

    public RecuperaSenhaResponse(String transactionId, String message, boolean success) {
        this.transactionId = transactionId;
        this.message = message;
        this.success = success;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}

