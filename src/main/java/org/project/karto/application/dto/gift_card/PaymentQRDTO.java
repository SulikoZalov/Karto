package org.project.karto.application.dto.gift_card;

import java.math.BigDecimal;

public record PaymentQRDTO(String partnerName, BigDecimal amount) {

    public String toJson() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("{\"partnerName\":\"");
        appendEscaped(sb, partnerName);
        sb.append("\",\"amount\":").append(amount).append("}");
        return sb.toString();
    }

    private static void appendEscaped(StringBuilder sb, String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            String escaped = switch (c) {
                case '"' -> "\\\"";
                case '\\' -> "\\\\";
                case '\b' -> "\\b";
                case '\f' -> "\\f";
                case '\n' -> "\\n";
                case '\r' -> "\\r";
                case '\t' -> "\\t";
                default -> (c < 0x20) ? String.format("\\u%04x", (int)c) : String.valueOf(c);
            };
            sb.append(escaped);
        }
    }
}
