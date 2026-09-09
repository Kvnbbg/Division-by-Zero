package fr.kvnbbg.tdaah.api;

import fr.kvnbbg.tdaah.pipeline.ZeroDivisionMeasurementException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Politique d'erreurs commune aux trois contrôleurs.
 *
 * <p>Aucun d'eux n'en avait : ni {@code @RestControllerAdvice}, ni
 * {@code @ExceptionHandler}. Les erreurs sortaient donc dans le corps par
 * défaut de Spring — {@code timestamp}, {@code status}, {@code error},
 * {@code path} — dont aucun champ ne porte de code stable. Un appelant ne
 * pouvait distinguer « dénominateur nul » de « JSON illisible » qu'en lisant
 * une phrase en anglais susceptible de changer à la prochaine version.
 *
 * <p>{@link ZeroDivisionMeasurementException} portait déjà
 * {@code @ResponseStatus(422)}, et c'était le bon code : la réponse arrivait
 * simplement sans nom lisible par une machine. Cet advice conserve le statut et
 * lui ajoute le champ {@code error}.
 *
 * <h2>422 contre 400, la distinction que le projet porte dans son nom</h2>
 *
 * <p>400 dit « je n'ai pas compris la demande » ; 422 dit « je l'ai comprise,
 * elle est bien formée, et elle n'a pas de réponse ». Diviser par une mesure
 * nulle est une question parfaitement lisible dont le résultat n'existe pas :
 * c'est 422. Un corps illisible ou un paramètre absent sont des demandes
 * fautives : c'est 400.
 *
 * <p>Aucun des deux n'est 500. Un 500 annonce une panne et invite à réessayer —
 * ce qui échouerait indéfiniment de la même façon. C'est le geste qui donne son
 * nom au projet : refuser en le disant, plutôt que d'émettre {@code Infinity}.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Corps commun : un code stable que l'on programme, un message que l'on lit. */
    private static ResponseEntity<Map<String, Object>> reponse(
            HttpStatus statut, String code, String message) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("error", code);
        corps.put("message", message);
        corps.put("status", statut.value());
        return ResponseEntity.status(statut).body(corps);
    }

    /** 422 — la demande est recevable, le résultat n'existe pas. */
    @ExceptionHandler(ZeroDivisionMeasurementException.class)
    public ResponseEntity<Map<String, Object>> onZeroDivision(ZeroDivisionMeasurementException e) {
        return reponse(HttpStatus.UNPROCESSABLE_ENTITY, "zero_division_measurement", e.getMessage());
    }

    /**
     * 422 — le corps est bien formé, mais viole une contrainte.
     *
     * <p>Atteint par {@code @Valid @NotNull} sur DivisionRequest : un corps
     * {@code {}} rendait un 400 sans dire QUEL champ manquait.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> onValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage())
                .reduce((a, b) -> a + " ; " + b)
                .orElse("corps invalide.");
        return reponse(HttpStatus.UNPROCESSABLE_ENTITY, "validation", detail);
    }

    /** 400 — corps JSON illisible ou absent. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> onCorpsIllisible(HttpMessageNotReadableException e) {
        return reponse(HttpStatus.BAD_REQUEST, "malformed_body", "corps JSON illisible ou absent.");
    }

    /** 400 — paramètre de requête obligatoire manquant. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> onParametreManquant(
            MissingServletRequestParameterException e) {
        return reponse(
                HttpStatus.BAD_REQUEST,
                "missing_parameter",
                "paramètre obligatoire manquant : " + e.getParameterName());
    }

    /** 400 — paramètre présent mais du mauvais type, et non 500 pour une faute de frappe. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> onTypeInvalide(MethodArgumentTypeMismatchException e) {
        return reponse(
                HttpStatus.BAD_REQUEST,
                "invalid_parameter",
                "valeur invalide pour « " + e.getName() + " » : " + e.getValue());
    }
}
