package com.asterexcrisys.acm.services.console.handlers;

import org.jline.terminal.Terminal.Signal;
import org.jline.terminal.Terminal.SignalHandler;

@SuppressWarnings("unused")
public class ShellSignalHandler implements SignalHandler {

    public void handle(Signal signal) {
        switch (signal) {
            case INT -> {
                System.out.println("Received interrupt signal, closing the shell...");
                System.exit(0);
            }
            case QUIT -> {
                System.out.println("Received quit signal, closing the shell...");
                System.exit(0);
            }
            default -> {
                // This should not do anything
            }
        }
    }

}