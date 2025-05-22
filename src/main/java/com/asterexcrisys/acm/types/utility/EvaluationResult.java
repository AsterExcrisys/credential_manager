package com.asterexcrisys.acm.types.utility;

import java.util.logging.Level;

@SuppressWarnings("unused")
public enum EvaluationResult {

    SUCCESS(Level.INFO),
    FAILURE(Level.WARNING);

    private final Level level;

    EvaluationResult(Level level) {
        this.level = level;
    }

    public Level level() {
        return level;
    }

}