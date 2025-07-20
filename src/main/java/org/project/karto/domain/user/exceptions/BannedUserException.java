package org.project.karto.domain.user.exceptions;

import org.project.karto.domain.common.exceptions.DomainException;

public class BannedUserException extends DomainException {
    public BannedUserException(String msg) {
        super(msg);
    }

    public BannedUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public BannedUserException(Throwable cause) {
        super(cause);
    }
}
