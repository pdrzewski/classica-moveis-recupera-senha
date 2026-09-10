package ms.recupera_psswd.adapter.in.api.rest.controller;

import ms.recupera_psswd.adapter.in.api.rest.dto.RecuperaSenhaRequest;
import ms.recupera_psswd.adapter.in.api.rest.dto.RecuperaSenhaResponse;
import ms.recupera_psswd.adapter.in.api.rest.dto.ResetSenhaRequest;
import ms.recupera_psswd.application.model.RecuperaSenha;
import ms.recupera_psswd.application.port.in.RecuperaSenhaPortIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recupera-senha")
public class RecuperaSenhaController {

    @Autowired
    private RecuperaSenhaPortIn recuperaSenhaPortIn;

    @PostMapping
    public ResponseEntity<RecuperaSenhaResponse> solicitarRecuperaSenha(
            @RequestBody RecuperaSenhaRequest request) {
        try {
            RecuperaSenha recuperaSenha = recuperaSenhaPortIn.solicitarRecuperaSenha(request.getEmail());

            RecuperaSenhaResponse response = new RecuperaSenhaResponse(
                    recuperaSenha.getTransactionId(),
                    "Um link de recuperação foi enviado para o seu email",
                    true
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            RecuperaSenhaResponse response = new RecuperaSenhaResponse(
                    null,
                    e.getMessage(),
                    false
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Endpoint used directly by the link sent in the recovery email.
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<RecuperaSenhaResponse> verificarTokenPeloLink(
            @PathVariable String transactionId,
            @RequestParam String token) {
        return verificarToken(transactionId, token);
    }

    @PostMapping("/{transactionId}/verificar")
    public ResponseEntity<RecuperaSenhaResponse> verificarToken(
            @PathVariable String transactionId,
            @RequestParam String token) {
        try {
            RecuperaSenha recuperaSenha = recuperaSenhaPortIn.verificarToken(transactionId, token);

            RecuperaSenhaResponse response = new RecuperaSenhaResponse(
                    transactionId,
                    "Token verificado com sucesso",
                    true
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            RecuperaSenhaResponse response = new RecuperaSenhaResponse(
                    transactionId,
                    e.getMessage(),
                    false
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/{transactionId}/resetar")
    public ResponseEntity<RecuperaSenhaResponse> resetarSenha(
            @PathVariable String transactionId,
            @RequestBody ResetSenhaRequest request) {
        try {
            RecuperaSenha recuperaSenha = recuperaSenhaPortIn.resetarSenha(
                    transactionId,
                    request.getToken(),
                    request.getNovaSenha()
            );

            RecuperaSenhaResponse response = new RecuperaSenhaResponse(
                    transactionId,
                    "Senha resetada com sucesso",
                    true
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            RecuperaSenhaResponse response = new RecuperaSenhaResponse(
                    transactionId,
                    e.getMessage(),
                    false
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
