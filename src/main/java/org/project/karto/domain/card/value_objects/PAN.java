package org.project.karto.domain.card.value_objects;

public record PAN(String creditCardNumber) {
    public PAN {
        if (creditCardNumber == null)
            throw new IllegalArgumentException("Credit card number cannot be null.");

        boolean isValidLuhn = luhnAlgorithmValidation(creditCardNumber);
        if (!isValidLuhn)
            throw new IllegalArgumentException("Invalid credit card number");
    }

    private boolean luhnAlgorithmValidation(String creditCardNumber) {
        char[] chars = convertToArrayOfValidChars(creditCardNumber);
        return getSum(chars) % 10 == 0;
    }

    private char[] convertToArrayOfValidChars(String input) {
        String sanitized = input.replaceAll("\\D", "");
        return sanitized.toCharArray();
    }

    private int getSum(char[] chars) {
        int sum = 0;
        for (int i = 0; i < chars.length; i++) {
            int number = getInReverseOrder(chars, i);
            sum += getElementValue(i, number);
        }
        return sum;
    }

    private int getInReverseOrder(char[] chars, int i) {
        int indexInReverseOrder = chars.length - 1 - i;
        char character = chars[indexInReverseOrder];
        return Character.getNumericValue(character);
    }

    private int getElementValue(int i, int number) {
        if (i % 2 != 0) return getOddElementValue(number);
        return number;
    }

    private int getOddElementValue(int element) {
        int value = element * 2;
        if (value <= 9) return value;
        return value - 9;
    }
}
