package org.project.karto.domain.common.exceptions;

public class IllegalDomainStateException extends DomainException {
    public IllegalDomainStateException(String message) {
        super(message);
    }

    public IllegalDomainStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalDomainStateException(Throwable cause) {
        super(cause);
    }
}
