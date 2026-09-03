package fr.kvnbbg.tdaah.pipeline;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ZeroDivisionMeasurementException extends RuntimeException {

    public ZeroDivisionMeasurementException(String field) {
        super("Named refusal: division by zero on field '" + field + "'. TDAAH does not emit Infinity.");
    }
}
