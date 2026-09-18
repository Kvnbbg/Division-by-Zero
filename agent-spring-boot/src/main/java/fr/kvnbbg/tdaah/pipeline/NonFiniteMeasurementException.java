package fr.kvnbbg.tdaah.pipeline;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class NonFiniteMeasurementException extends RuntimeException {

    public NonFiniteMeasurementException(String role, double value) {
        super("Named refusal: non-finite " + role + " (" + value + "). TDAAH does not emit NaN or Infinity.");
    }
}
