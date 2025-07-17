package org.project.karto.domain.companies.value_objects;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record CompanyName(String companyName) {
    private static final String COMPANY_NAME_REGEX = "[\\p{L}\\p{N} .,'&()\\-+/]*";
    private static final Pattern COMPANY_NAME_PATTERN = Pattern.compile(COMPANY_NAME_REGEX);

    public CompanyName {
        if (companyName == null || companyName.isBlank())
            throw new IllegalDomainArgumentException("Company name cannot be null or blank.");
        if (companyName.length() > 255)
            throw new IllegalDomainArgumentException("Company name is too long.");

        Matcher matcher = COMPANY_NAME_PATTERN.matcher(companyName);
        if (!matcher.matches())
            throw new IllegalDomainArgumentException("Company name contains invalid characters.");
    }

    @Override
    public String toString() {
        return companyName;
    }
}