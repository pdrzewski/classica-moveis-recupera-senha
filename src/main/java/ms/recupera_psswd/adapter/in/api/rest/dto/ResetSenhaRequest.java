package ms.recupera_psswd.adapter.in.api.rest.dto;

public class ResetSenhaRequest {
    private String token;
    private String novaSenha;

    public ResetSenhaRequest() {
    }

    public ResetSenhaRequest(String token, String novaSenha) {
        this.token = token;
        this.novaSenha = novaSenha;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }
}

