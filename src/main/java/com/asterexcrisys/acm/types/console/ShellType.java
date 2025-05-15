package com.asterexcrisys.acm.types.console;

import com.asterexcrisys.acm.utility.ConsoleUtility;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
public enum ShellType {

    NON_INTERACTIVE(ConsoleUtility.NonInteractiveShell::fromCommandName),
    INTERACTIVE(ConsoleUtility.InteractiveShell::fromCommandName),;

    private final Function<String, Optional<? extends CommandType>> filter;

    ShellType(Function<String, Optional<? extends CommandType>> filter) {
        this.filter = filter;
    }

    public Function<String, Optional<? extends CommandType>> filter() {
        return filter;
    }

}