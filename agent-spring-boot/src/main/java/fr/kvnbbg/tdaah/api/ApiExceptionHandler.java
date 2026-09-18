package fr.kvnbbg.tdaah.api;

import fr.kvnbbg.tdaah.pipeline.NonFiniteMeasurementException;
import fr.kvnbbg.tdaah.pipeline.ZeroDivisionMeasurementException;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static ResponseEntity<Map<String, Object>> reponse(
            HttpStatus statut, String code, String message, HttpServletRequest request) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("error", code);
        corps.put("message", message);
        corps.put("status", statut.value());
        Object correlationId = request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE);
        if (correlationId instanceof String id && !id.isBlank()) {
            corps.put("correlationId", id);
        }
        return ResponseEntity.status(statut).body(corps);
    }

    @ExceptionHandler(ZeroDivisionMeasurementException.class)
    public ResponseEntity<Map<String, Object>> onZeroDivision(ZeroDivisionMeasurementException e) {
        return reponse(HttpStatus.UNPROCESSABLE_ENTITY, "zero_division_measurement", e.getMessage(), request);
    }

    @ExceptionHandler(NonFiniteMeasurementException.class)
    public ResponseEntity<Map<String, Object>> onNonFinite(NonFiniteMeasurementException e) {
        return reponse(HttpStatus.UNPROCESSABLE_ENTITY, "non_finite_measurement", e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> onValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage())
                .reduce((a, b) -> a + " ; " + b)
                .orElse("corps invalide.");
        return reponse(HttpStatus.UNPROCESSABLE_ENTITY, "validation", detail, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> onCorpsIllisible(HttpMessageNotReadableException e, HttpServletRequest request) {
        return reponse(HttpStatus.BAD_REQUEST, "malformed_body", "corps JSON illisible ou absent.", request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> onParametreManquant(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        return reponse(
                HttpStatus.BAD_REQUEST,
                "missing_parameter",
                "paramètre obligatoire manquant : " + e.getParameterName(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> onTypeInvalide(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        return reponse(
                HttpStatus.BAD_REQUEST,
                "invalid_parameter",
                "valeur invalide pour « " + e.getName() + " » : " + e.getValue(), request);
    }
}
