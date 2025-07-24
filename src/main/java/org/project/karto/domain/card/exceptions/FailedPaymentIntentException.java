package org.project.karto.domain.card.exceptions;

import org.project.karto.domain.common.exceptions.DomainException;

public class FailedPaymentIntentException extends DomainException {
    public FailedPaymentIntentException(String message) {
        super(message);
    }
}
