package ms.recupera_psswd.adapter.out.messaging.consumer;

import ms.recupera_psswd.adapter.out.email.EmailService;
import ms.recupera_psswd.adapter.out.messaging.config.RabbitMQConfig;
import ms.recupera_psswd.adapter.out.messaging.event.EnviarEmailEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailConsumer {

    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void consumirMensagemEmail(EnviarEmailEvent event) {
        try {
            System.out.println("Recebido evento de email para: " + event.getEmail());
            System.out.println("Transaction ID: " + event.getTransactionId());

            // Enviar o email
            emailService.enviarEmail(
                event.getEmail(),
                event.getAssunto(),
                event.getMensagem()
            );

            System.out.println("Email processado com sucesso para: " + event.getEmail());
        } catch (Exception e) {
            System.err.println("Erro ao processar email para " + event.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
            // Aqui você pode adicionar lógica de retry
            throw new RuntimeException("Erro ao processar mensagem de email", e);
        }
    }
}

