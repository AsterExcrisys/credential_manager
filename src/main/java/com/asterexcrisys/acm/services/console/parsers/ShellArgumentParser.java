package com.asterexcrisys.acm.services.console.parsers;

import com.asterexcrisys.acm.services.console.validators.Validator;
import com.asterexcrisys.acm.types.console.CommandType;
import com.asterexcrisys.acm.types.utility.Outcome;
import com.asterexcrisys.acm.types.utility.Result;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ShellArgumentParser {

    private final Function<String, Optional<? extends CommandType>> filter;

    public ShellArgumentParser(Function<String, Optional<? extends CommandType>> filter) {
        this.filter = filter;
    }

    public Result<? extends CommandType, String> parseOld(String[] arguments) {
        if (arguments.length < 1) {
            return Result.failure("No command specified");
        }
        Optional<? extends CommandType> type = filter.apply(arguments[0]);
        if (type.isEmpty()) {
            return Result.failure("Unknown command: " + arguments[0]);
        }
        if (arguments.length != type.get().argumentCount() + 1) {
            return Result.failure("Invalid number of arguments for command: " + type.get().longName());
        }
        Validator[] validators = type.get().argumentValidators();
        for (int i = 0; i < validators.length; i++) {
            if (!validators[i].validate(arguments[i + 1])) {
                return Result.failure("Invalid argument: " + arguments[i + 1]);
            }
        }
        return Result.success(type.get());
    }

    public Outcome<? extends CommandType, String> parseNew(String[] arguments) {
        if (arguments.length < 1) {
            return Outcome.ofError("No command specified");
        }
        Optional<? extends CommandType> type = filter.apply(arguments[0]);
        if (type.isEmpty()) {
            return Outcome.ofError("Unknown command: " + arguments[0]);
        }
        if (arguments.length != type.get().argumentCount() + 1) {
            return Outcome.ofError("Invalid number of arguments for command: " + type.get().longName());
        }
        Validator[] validators = type.get().argumentValidators();
        for (int i = 0; i < validators.length; i++) {
            if (!validators[i].validate(arguments[i + 1])) {
                return Outcome.ofError("Invalid argument: " + arguments[i + 1]);
            }
        }
        return Outcome.ofValue(type.get());
    }

}