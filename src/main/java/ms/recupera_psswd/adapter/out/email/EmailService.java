package ms.recupera_psswd.adapter.out.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@classica-moveis.com}")
    private String emailFrom;

    public void enviarEmail(String destinatario, String assunto, String mensagem) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(destinatario);
            message.setSubject(assunto);
            message.setText(mensagem);

            mailSender.send(message);

            System.out.println("Email enviado com sucesso para: " + destinatario);
        } catch (Exception e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }
}

