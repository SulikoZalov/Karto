package org.project.karto.application.dto.gift_card;

import java.math.BigDecimal;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

public record TransactionDTO(
    long orderID,
    BigDecimal amount,
    String language,
    String successURL,
    String cancelURL,
    String declineURL) {

  public TransactionDTO {
    if (orderID < 0)
      throw new IllegalDomainArgumentException("OrderID cannot be below 0.");
 
    if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
      throw new IllegalDomainArgumentException("amount must be a positive number.");

    if (language == null || language.length() != 2)
      throw new IllegalDomainArgumentException("language must be not null and have 2 characters.");

    if (successURL == null || successURL.isBlank())
      throw new IllegalDomainArgumentException("successURL must not be blank.");

    if (cancelURL == null || cancelURL.isBlank())
      throw new IllegalDomainArgumentException("cancelURL must not be blank.");
        
    if (declineURL == null || declineURL.isBlank())
      throw new IllegalDomainArgumentException("declineURL must not be blank.");
  }
}
