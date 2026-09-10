package ms.recupera_psswd.application.port.in;

import ms.recupera_psswd.domain.model.RecuperaSenha;

public interface RecuperaSenhaPortIn {
    RecuperaSenha solicitarRecuperaSenha(String email);
    RecuperaSenha verificarToken(String transactionId, String token);
    RecuperaSenha resetarSenha(String transactionId, String token, String novaSenha);
}
