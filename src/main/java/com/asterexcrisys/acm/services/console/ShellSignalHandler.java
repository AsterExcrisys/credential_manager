package com.asterexcrisys.acm.services.console;

import org.jline.terminal.Terminal.Signal;
import org.jline.terminal.Terminal.SignalHandler;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class ShellSignalHandler implements SignalHandler {

    private static final Logger LOGGER = Logger.getLogger(ShellSignalHandler.class.getName());

    public void handle(Signal signal) {
        switch (signal) {
            case INT -> {
                LOGGER.info("Interrupt signal received");
                System.out.println("Received interrupt signal, closing the shell...");
                System.exit(0);
            }
            case QUIT -> {
                LOGGER.info("Quit signal received");
                System.out.println("Received quit signal, closing the shell...");
                System.exit(0);
            }
            default -> {
                // No handling needed
            }
        }
    }

}