package com.asterexcrisys.acm.utility;

import com.asterexcrisys.acm.types.console.*;
import java.util.Optional;

@SuppressWarnings("unused")
public final class ConsoleUtility {

    private ConsoleUtility() {
        // This class should not be instantiable
    }

    public static final class NonInteractiveShell {

        private NonInteractiveShell() {
            // This class should not be instantiable
        }

        public static Optional<? extends CommandType> fromCommandName(String value) {
            Optional<GenericNonInteractiveCommandType> type = GenericNonInteractiveCommandType.fromValue(value);
            if (type.isPresent()) {
                return type;
            }
            return VaultCommandType.fromValue(value);
        }

    }

    public static final class InteractiveShell {

        private InteractiveShell() {
            // This class should not be instantiable
        }

        public static Optional<? extends CommandType> fromCommandName(String value) {
            Optional<GenericInteractiveCommandType> type = GenericInteractiveCommandType.fromValue(value);
            if (type.isPresent()) {
                return type;
            }
            return CredentialCommandType.fromValue(value);
        }

        public static String[] justifyText(String text, int width) {
            if (text == null || text.isBlank() || width < 1) {
                return new String[0];
            }
            if (Math.ceilDiv(text.length(), width) <= 1) {
                return new String[] {text};
            }
            String[] lines = new String[Math.ceilDiv(text.length(), width)];
            for (int i = 0; i < text.length(); i++) {
                if (i % width == 0) {
                    lines[Math.ceilDiv(i, width)] = text.substring(i, Math.min(i + width, text.length()));
                }
            }
            return lines;
        }

    }

}