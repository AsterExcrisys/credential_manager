package com.asterexcrisys.acm.types.console;

import com.asterexcrisys.acm.services.Utility;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
public enum ShellType {

    NON_INTERACTIVE(Utility.NonInteractiveShell::fromValue),
    INTERACTIVE(Utility.InteractiveShell::fromValue),;

    private final Function<String, Optional<? extends CommandType>> filter;

    ShellType(Function<String, Optional<? extends CommandType>> filter) {
        this.filter = filter;
    }

    public Function<String, Optional<? extends CommandType>> filter() {
        return filter;
    }

}