package ms.recupera_psswd.adapter.out.messaging.event;

import java.io.Serializable;

public class EnviarEmailEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String transactionId;
    private String email;
    private String recoveryToken;
    private String assunto;
    private String mensagem;

    public EnviarEmailEvent() {
    }

    public EnviarEmailEvent(String transactionId, String email, String recoveryToken, String assunto, String mensagem) {
        this.transactionId = transactionId;
        this.email = email;
        this.recoveryToken = recoveryToken;
        this.assunto = assunto;
        this.mensagem = mensagem;
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

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}

