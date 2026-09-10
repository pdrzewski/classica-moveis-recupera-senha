package ms.recupera_psswd.adapter.out.messaging.producer;

import ms.recupera_psswd.adapter.out.messaging.config.RabbitMQConfig;
import ms.recupera_psswd.adapter.out.messaging.event.EnviarEmailEvent;
import ms.recupera_psswd.application.port.out.RecuperaSenhaPortOut;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailProducer implements RecuperaSenhaPortOut {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void publicarEmailRecuperaSenha(String transactionId, String email,
                                           String recoveryToken, String urlRecuperacao) {

        String assunto = "Recuperação de Senha - Clássica Móveis";
        String mensagem = String.format(
            "Olá,\n\n" +
            "Recebemos uma solicitação para recuperar sua senha.\n\n" +
            "Clique no link abaixo para redefinir sua senha:\n" +
            "%s\n\n" +
            "Este link expira em 24 horas.\n\n" +
            "Se você não solicitou isso, ignore este email.\n\n" +
            "Atenciosamente,\n" +
            "Clássica Móveis",
            urlRecuperacao
        );

        EnviarEmailEvent event = new EnviarEmailEvent(transactionId, email, recoveryToken, assunto, mensagem);

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.ROUTING_KEY,
            event
        );
    }
}
