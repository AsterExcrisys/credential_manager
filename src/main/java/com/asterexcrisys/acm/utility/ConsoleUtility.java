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

    }

}