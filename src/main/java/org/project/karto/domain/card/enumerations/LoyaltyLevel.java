package org.project.karto.domain.card.enumerations;

import org.project.karto.domain.card.value_objects.UserActivitySnapshot;

import java.math.BigDecimal;

public enum LoyaltyLevel {
    NONE(BigDecimal.ZERO, BigDecimal.ZERO, "Beginner"),
    BRONZE(BigDecimal.valueOf(100), BigDecimal.valueOf(0.005), "Bronze Member"),
    SILVER(BigDecimal.valueOf(250), BigDecimal.valueOf(0.010), "Silver Member"),
    GOLD(BigDecimal.valueOf(500), BigDecimal.valueOf(0.015), "Gold Member"),
    PLATINUM(BigDecimal.valueOf(1000), BigDecimal.valueOf(0.020), "Platinum Member"),
    DIAMOND(BigDecimal.valueOf(2000), BigDecimal.valueOf(0.025), "Diamond Member");

    private static final LoyaltyLevel[] LEVELS = values();
    private final BigDecimal minDecaySpent;
    private final BigDecimal cashbackBonus;
    private final String displayName;

    LoyaltyLevel(BigDecimal minDecaySpent, BigDecimal cashbackBonus, String displayName) {
        this.minDecaySpent = minDecaySpent;
        this.cashbackBonus = cashbackBonus;
        this.displayName = displayName;
    }

    public BigDecimal cashbackBonus() {
        return cashbackBonus;
    }

    public String displayName() {
        return displayName;
    }

    public static LoyaltyLevel determineLevel(UserActivitySnapshot snapshot) {
        for (int i = values().length - 1; i >= 0; i--) {
            LoyaltyLevel level = LEVELS[i];

            boolean reachedLevel = snapshot.decaySpent().compareTo(level.minDecaySpent) >= 0;
            if (reachedLevel) return level;
        }
        return NONE;
    }
}