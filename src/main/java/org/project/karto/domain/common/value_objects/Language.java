package org.project.karto.domain.common.value_objects;

import java.util.Locale;
import java.util.Set;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

import static org.project.karto.domain.common.util.Utils.required;

public record Language(String code) {
  private static final Set<String> ISO_LANGUAGES = Set.of(Locale.getISOLanguages());

  public Language {
    required("laguageCode", code);
    if (code.length() != 2)
      throw new IllegalDomainArgumentException("Laguage code must be 2 characters length.");

    if (!ISO_LANGUAGES.contains(code)) {
      throw new IllegalDomainArgumentException("Invalid ISO 639-1 language code: " + code);
    }
  }
}
