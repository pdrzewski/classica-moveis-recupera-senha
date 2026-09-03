package ms.recupera_psswd.adapter.in.api.rest.dto;

public class RecuperaSenhaRequest {
    private String email;

    public RecuperaSenhaRequest() {
    }

    public RecuperaSenhaRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
