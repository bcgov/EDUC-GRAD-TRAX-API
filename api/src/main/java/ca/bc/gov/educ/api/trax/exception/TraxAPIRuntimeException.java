package ca.bc.gov.educ.api.trax.exception;

import java.io.Serial;

public class TraxAPIRuntimeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TraxAPIRuntimeException(String message) {
        super(message);
    }
}
