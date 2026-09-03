package fr.kvnbbg.tdaah.api;

import fr.kvnbbg.tdaah.domain.Units;
import fr.kvnbbg.tdaah.domain.ZeroDivisionMeasurementException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Surface HTTP de l'agent.
 *
 * <p>Le manifeste annonçait cinq outils — convert_measurement,
 * evaluate_expression, list_units, explain_dimension, refuse_zero_division —
 * alors qu'aucun n'était exposé : le contrôleur ne servait que la liste
 * elle-même. Un agent qui appelait l'un d'eux recevait un 404.
 *
 * <p>Les outils annoncés ici sont désormais ceux qui répondent, et uniquement
 * ceux-là. {@code evaluate_expression} — l'évaluateur d'expressions
 * dimensionnées de web/js/convertisseur.js — n'est pas encore porté : il est
 * donc absent de la liste plutôt que promis.
 */
@RestController
@RequestMapping("/v1/agent")
public class AgentController {

    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> tools() {
        return ResponseEntity.ok(Map.of(
                "author", "Kevin Marville",
                "runtime", "spring-boot",
                "llm", false,
                "tools", List.of(
                        Map.of("name", "convert_measurement", "method", "POST", "path", "/v1/agent/convert"),
                        Map.of("name", "list_units", "method", "GET", "path", "/v1/agent/units"),
                        Map.of("name", "explain_dimension", "method", "GET", "path", "/v1/agent/dimension/{unit}"),
                        Map.of("name", "refuse_zero_division", "method", "GET", "path", "/v1/agent/ratio"))));
    }

    /** convert_measurement — conversion entre unités de même dimension. */
    @PostMapping("/convert")
    public ResponseEntity<Map<String, Object>> convert(@RequestBody Map<String, Object> body) {
        double value = asDouble(body.get("value"));
        Units.Converted converted = Units.convert(value, asText(body.get("from")), asText(body.get("to")));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("value", converted.value());
        out.put("unit", converted.unit());
        return ResponseEntity.ok(out);
    }

    /** list_units — symboles reconnus. */
    @GetMapping("/units")
    public ResponseEntity<Map<String, Object>> units() {
        return ResponseEntity.ok(Map.of("units", Units.symbols()));
    }

    /** explain_dimension — dimension et facteurs d'une unité. */
    @GetMapping("/dimension/{unit}")
    public ResponseEntity<Map<String, Object>> dimension(@PathVariable("unit") String unit) {
        return ResponseEntity.ok(Units.explain(unit));
    }

    /**
     * refuse_zero_division — rapport de deux mesures de même dimension.
     *
     * <p>Le seul endroit où l'agent divise, et donc le seul où il refuse.
     */
    @GetMapping("/ratio")
    public ResponseEntity<Map<String, Object>> ratio(
            @RequestParam("value") double value,
            @RequestParam("unit") String unit,
            @RequestParam("per") double per,
            @RequestParam("perUnit") String perUnit) {
        return ResponseEntity.ok(Map.of("ratio", Units.ratio(value, unit, per, perUnit)));
    }

    /**
     * 422 et non 500 : la demande est recevable, le résultat n'existe pas.
     * Un 500 laisserait croire à une panne du service.
     */
    @ExceptionHandler(ZeroDivisionMeasurementException.class)
    public ResponseEntity<Map<String, Object>> onZeroDivision(ZeroDivisionMeasurementException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "zero_division_measurement", "message", e.getMessage()));
    }

    /** Unité inconnue ou dimensions incompatibles : la requête est fautive. */
    @ExceptionHandler(Units.UnitException.class)
    public ResponseEntity<Map<String, Object>> onUnit(Units.UnitException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "unit", "message", e.getMessage()));
    }

    private static double asDouble(Object raw) {
        if (raw instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(raw).trim());
        } catch (NumberFormatException e) {
            throw new Units.UnitException("valeur numérique attendue : " + raw);
        }
    }

    private static String asText(Object raw) {
        return raw == null ? null : String.valueOf(raw);
    }
}
