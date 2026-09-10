package ms.recupera_psswd.application.port.out;

public interface RecuperaSenhaPortOut {

    void publicarEmailRecuperaSenha(String transactionId, String email,
                                    String recoveryToken, String urlRecuperacao);
}
