package com.asterexcrisys.cman.types;

@SuppressWarnings("unused")
public enum PasswordStrength {

    EXCELLENT(5),
    GOOD(4),
    DECENT(3),
    BAD(2),
    TERRIBLE(1);

    private final int score;

    PasswordStrength(int score) {
        this.score = score;
    }

    public int score() {
        return score;
    }

}