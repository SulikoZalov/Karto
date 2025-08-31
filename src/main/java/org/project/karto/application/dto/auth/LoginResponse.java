package org.project.karto.application.dto.auth;

public sealed interface LoginResponse permits TwoFAMessage, Tokens {}