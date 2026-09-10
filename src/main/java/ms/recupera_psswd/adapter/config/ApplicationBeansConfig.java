package ms.recupera_psswd.adapter.config;

import ms.recupera_psswd.application.port.out.RecuperaSenhaPortOut;
import ms.recupera_psswd.application.usecase.RecuperaSenhaUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationBeansConfig {

    @Bean
    public RecuperaSenhaUseCase recuperaSenhaUseCase(
            RecuperaSenhaPortOut recuperaSenhaPortOut,
            @Value("${app.api.url:http://localhost:8080}") String apiUrl) {
        return new RecuperaSenhaUseCase(recuperaSenhaPortOut, apiUrl);
    }
}
