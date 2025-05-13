package com.asterexcrisys.acm.services.console;

import com.asterexcrisys.acm.types.console.CommandType;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.SyntaxError;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ArgumentParser implements Parser {

    private final Function<String, Optional<? extends CommandType>> filter;

    public ArgumentParser(Function<String, Optional<? extends CommandType>> filter) {
        this.filter = filter;
    }

    public ParsedLine parse(String line, int cursor, ParseContext context) throws SyntaxError {
        String[] arguments = line.split("\\s+");
        if (arguments.length < 1) {
            throw new SyntaxError(0, 0, "No command specified");
        }
        Optional<? extends CommandType> type = filter.apply(arguments[0]);
        if (type.isEmpty()) {
            throw new SyntaxError(0, 0, "Unknown command: " + arguments[0]);
        }
        if (arguments.length != type.get().argumentCount() + 1) {
            throw new SyntaxError(0, 0, "Invalid number of arguments for command: " + type.get().longName());
        }
        Validator[] validators = type.get().argumentValidators();
        for (int i = 0; i < validators.length; i++) {
            if (!validators[i].validate(arguments[i + 1])) {
                throw new SyntaxError(0, 0, "Invalid argument: " + arguments[i + 1]);
            }
        }
        return new ParsedLine() {
            public String word() {
                return "";
            }

            public int wordCursor() {
                return 0;
            }

            public int wordIndex() {
                return 0;
            }

            public List<String> words() {
                return List.of(arguments);
            }

            public String line() {
                return line;
            }

            public int cursor() {
                return cursor;
            }
        };
    }

}