package org.project.karto.domain.common.exceptions;

public class IllegalDomainArgumentException extends DomainException{
    public IllegalDomainArgumentException(String message) {
        super(message);
    }

    public IllegalDomainArgumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalDomainArgumentException(Throwable cause) {
        super(cause);
    }
}
