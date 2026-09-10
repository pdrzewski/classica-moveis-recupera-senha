package ms.recupera_psswd.domain.exception;

public class TokenExpiradoException extends IllegalArgumentException {

    public TokenExpiradoException(String message) {
        super(message);
    }
}
