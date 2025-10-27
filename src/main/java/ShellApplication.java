import com.asterexcrisys.acm.CredentialManager;
import com.asterexcrisys.acm.VaultManager;
import com.asterexcrisys.acm.constants.GlobalConstants;
import com.asterexcrisys.acm.constants.HashingConstants;
import com.asterexcrisys.acm.services.console.TableBuilder;
import com.asterexcrisys.acm.types.utility.*;
import com.asterexcrisys.acm.utility.EncryptionUtility;
import com.asterexcrisys.acm.utility.GlobalUtility;
import com.asterexcrisys.acm.services.console.ShellSignalHandler;
import com.asterexcrisys.acm.services.console.ShellArgumentParser;
import com.asterexcrisys.acm.types.console.*;
import com.asterexcrisys.acm.types.encryption.Credential;
import com.asterexcrisys.acm.utility.PathUtility;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.LineReader;
import org.jline.reader.LineReader.Option;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Reference;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import oshi.SystemInfo;
import oshi.hardware.ComputerSystem;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Stream;
import static org.jline.keymap.KeyMap.alt;
import static org.jline.keymap.KeyMap.ctrl;

@SuppressWarnings("unused")
public class ShellApplication {

    private static final Logger LOGGER = Logger.getLogger(ShellApplication.class.getName());
    private static final boolean DEBUG = GlobalUtility.isDebugEnabled();

    public static void main(String[] programArguments) {
        LineReader reader;
        try {
            Files.createDirectories(Paths.get("./data/"));
            LogManager.getLogManager().reset();
            configureLogger(LogManager.getLogManager().getLogger(GlobalConstants.ROOT_LOGGER));
            reader = configureReader();
            switch (validateArguments(programArguments, ShellType.NON_INTERACTIVE)) {
                case Triplet(FlowInstruction instruction, EvaluationResult result, String message) when instruction == FlowInstruction.TERMINATE -> {
                    LOGGER.log(result.level(), message);
                    printMessage(result.level(), message);
                    return;
                }
                case Triplet(FlowInstruction instruction, EvaluationResult result, String message) when message != null -> {
                    printMessage(result.level(), message);
                }
                default -> {
                    // No operation needed
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error starting up application: " + e.getMessage());
            printMessage(Level.SEVERE, "An error occurred during the application startup");
            System.exit(1);
            return;
        }
        ComputerSystem computerSystem = (new SystemInfo()).getHardware().getComputerSystem();
        try (VaultManager manager = new VaultManager(
                computerSystem.getHardwareUUID(),
                Base64.getEncoder().encodeToString(EncryptionUtility.checkPadding(
                        computerSystem.getSerialNumber().getBytes(StandardCharsets.UTF_8),
                        HashingConstants.SALT_SIZE
                ))
        )) {
            switch (checkGenericNonInteractiveCommands(manager, programArguments)) {
                case Triplet(FlowInstruction instruction, EvaluationResult result, String message) when instruction == FlowInstruction.TERMINATE -> {
                    LOGGER.log(result.level(), message);
                    printMessage(result.level(), message);
                    return;
                }
                case Triplet(FlowInstruction instruction, EvaluationResult result, String message) when message != null -> {
                    printMessage(result.level(), message);
                }
                default -> {
                    // No operation needed
                }
            }
            switch (checkVaultCommands(manager, programArguments)) {
                case Triplet(FlowInstruction instruction, EvaluationResult result, String message) when instruction == FlowInstruction.TERMINATE -> {
                    LOGGER.log(result.level(), message);
                    printMessage(result.level(), message);
                    return;
                }
                case Triplet(FlowInstruction instruction, EvaluationResult result, String message) when message != null -> {
                    printMessage(result.level(), message);
                }
                default -> {
                    // No operation needed
                }
            }
            while (true) {
                String[] shellArguments = reader.readLine(GlobalConstants.SHELL_PROMPT).trim().split("\\s+");
                switch (validateArguments(shellArguments, ShellType.INTERACTIVE)) {
                    case Triplet(FlowInstruction instruction, EvaluationResult result, String message) when instruction == FlowInstruction.TERMINATE -> {
                        LOGGER.log(result.level(), message);
                        printMessage(result.level(), message);
                        continue;
                    }
                    case Triplet(FlowInstruction instruction, EvaluationResult result, String message) when message != null -> {
                        printMessage(result.level(), message);
                    }
                    default -> {
                        // No operation needed
                    }
                }
                switch (checkGenericInteractiveCommands(manager, shellArguments)) {
                    case Triplet(LoopInstruction instruction, EvaluationResult result, String message) when instruction == LoopInstruction.EXIT -> {
                        LOGGER.log(result.level(), message);
                        printMessage(result.level(), message);
                        return;
                    }
                    case Triplet(LoopInstruction instruction, EvaluationResult result, String message) when instruction == LoopInstruction.SKIP -> {
                        printMessage(result.level(), message);
                        continue;
                    }
                    case Triplet(LoopInstruction instruction, EvaluationResult result, String message) when message != null -> {
                        printMessage(result.level(), message);
                    }
                    default -> {
                        // No operation needed
                    }
                }
                switch (checkCredentialCommands(manager, shellArguments)) {
                    case Triplet(LoopInstruction instruction, EvaluationResult result, String message) when instruction == LoopInstruction.EXIT -> {
                        LOGGER.log(result.level(), message);
                        printMessage(result.level(), message);
                        return;
                    }
                    case Triplet(LoopInstruction instruction, EvaluationResult result, String message) when instruction == LoopInstruction.SKIP -> {
                        printMessage(result.level(), message);
                        continue;
                    }
                    case Triplet(LoopInstruction instruction, EvaluationResult result, String message) when message != null -> {
                        printMessage(result.level(), message);
                    }
                    default -> {
                        // No operation needed
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error executing application: " + e.getMessage());
            printMessage(Level.SEVERE, "An error occurred during the application execution");
            System.exit(1);
        }
    }

    private static void configureLogger(Logger logger) throws IOException {
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }
        Files.createDirectories(Paths.get("./data/logs/"));
        if (DEBUG) {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            consoleHandler.setLevel(Level.INFO);
            logger.addHandler(consoleHandler);
        }
        FileHandler fileHandler = new FileHandler(
                String.format("./data/logs/%s.log", GlobalUtility.getCurrentDate()),
                true
        );
        fileHandler.setLevel(Level.WARNING);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);
    }

    private static LineReader configureReader() throws IOException {
        Files.createDirectories(Paths.get("./data/console/"));
        LineReaderBuilder builder = LineReaderBuilder.builder();
        builder.appName(String.format("%s (%s)", GlobalConstants.APPLICATION_NAME, GlobalConstants.APPLICATION_VERSION));
        builder.terminal(configureTerminal());
        builder.parser(new DefaultParser());
        builder.completer(new AggregateCompleter(
                new StringsCompleter(Stream.of(GenericInteractiveCommandType.values()).map(CommandType::longName).toArray(String[]::new)),
                new StringsCompleter(Stream.of(CredentialCommandType.values()).map(CommandType::longName).toArray(String[]::new))
        ));
        builder.history(new DefaultHistory());
        builder.variable(LineReader.HISTORY_FILE, Paths.get("./data/console/history.txt"));
        builder.option(Option.HISTORY_BEEP, false);
        builder.option(Option.HISTORY_IGNORE_DUPS, true);
        builder.option(Option.HISTORY_IGNORE_SPACE, true);
        builder.option(LineReader.Option.AUTO_LIST, true);
        builder.option(LineReader.Option.LIST_PACKED, true);
        builder.option(LineReader.Option.AUTO_MENU, true);
        builder.option(Option.MENU_COMPLETE, true);
        builder.option(Option.AUTO_FRESH_LINE, true);
        builder.option(Option.CASE_INSENSITIVE, true);
        LineReader reader = builder.build();
        KeyMap<Binding> bindings = reader.getKeyMaps().get(LineReader.MAIN);
        bindings.bind(new Reference(LineReader.CAPITALIZE_WORD), alt('C'));
        bindings.bind(new Reference(GlobalConstants.UPCASE_BINDING), alt('U'));
        bindings.bind(new Reference(GlobalConstants.DOWNCASE_BINDING), alt('L'));
        bindings.bind(new Reference(LineReader.TRANSPOSE_CHARS), ctrl('T'));
        bindings.bind(new Reference(LineReader.KILL_LINE), ctrl('K'));
        bindings.bind(new Reference(LineReader.HISTORY_INCREMENTAL_SEARCH_BACKWARD), ctrl('R'));
        return reader;
    }

    private static Terminal configureTerminal() throws IOException {
        TerminalBuilder builder = TerminalBuilder.builder();
        builder.system(true);
        builder.name(String.format("%s (%s)", GlobalConstants.APPLICATION_NAME, GlobalConstants.APPLICATION_VERSION));
        builder.type(GlobalConstants.TERMINAL_TYPE);
        builder.streams(System.in, System.out);
        builder.encoding(StandardCharsets.UTF_8);
        builder.nativeSignals(true);
        builder.signalHandler(new ShellSignalHandler());
        return builder.build();
    }

    public static void printMessage(Level level, String message) {
        if (level.intValue() >= Level.FINEST.intValue() && level.intValue() <= Level.INFO.intValue()) {
            System.out.println(message);
            System.out.flush();
        } else if (level.intValue() >= Level.WARNING.intValue() && level.intValue() <= Level.SEVERE.intValue()) {
            System.err.println(message);
            System.err.flush();
        }
    }

    private static Triplet<FlowInstruction, EvaluationResult, String> validateArguments(String[] arguments, ShellType type) {
        ShellArgumentParser parser = new ShellArgumentParser(type.filter());
        Result<? extends CommandType, String> result = parser.parseOld(arguments);
        if (result.isSuccess()) {
            return Triplet.of(FlowInstruction.PROCEED, EvaluationResult.SUCCESS, null);
        }
        Optional<String> error = result.getError();
        return Triplet.of(FlowInstruction.TERMINATE, EvaluationResult.FAILURE, error.orElse("Unknown error"));
    }

    private static Triplet<FlowInstruction, EvaluationResult, String> checkGenericNonInteractiveCommands(VaultManager manager, String[] arguments) {
        if (GenericNonInteractiveCommandType.IMPORT_VAULT.is(arguments[0])) {
            if (!manager.importVault(Paths.get(arguments[1]), arguments[2], arguments[3])) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        "Failed to import vault with name: " + arguments[2]
                );
            }
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    "Succeeded to import vault with name: " + arguments[2]
            );
        }
        if (GenericNonInteractiveCommandType.EXPORT_VAULT.is(arguments[0])) {
            if (!manager.exportVault(Paths.get(arguments[1]), arguments[2], arguments[3])) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        "Failed to export vault with name: " + arguments[2]
                );
            }
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    "Succeeded to export vault with name: " + arguments[2]
            );
        }
        if (GenericNonInteractiveCommandType.CLEAR_CONTEXT.is(arguments[0])) {
            boolean isHistoryCleared = PathUtility.deleteRecursively(Paths.get("./data/console/"));
            boolean isStatusCleared = PathUtility.deleteRecursively(Paths.get("./data/logs/"));
            if (!isHistoryCleared || !isStatusCleared) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        "Failed to clear context: application's history records and/or status logs may have not been completely cleared"
                );
            }
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    "Succeeded to clear context: application's history records and status logs have been cleared"
            );
        }
        if (GenericNonInteractiveCommandType.TEST_GIVEN_PASSWORD.is(arguments[0])) {
            Pair<PasswordStrength, String[]> advices = manager.testGivenPassword(arguments[1]);
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    buildTable(
                            CellSize.WRAP_SMALL,
                            List.of("Strength", "Score"),
                            List.of(List.of(
                                    advices.first().name(),
                                    String.valueOf(advices.first().score())
                            ))
                    ) + '\n' + buildTable(
                            CellSize.WRAP_MEDIUM,
                            List.of("Advice"),
                            Stream.of(advices.second()).map(Collections::singletonList).toList()
                    )
            );
        }
        return Triplet.of(FlowInstruction.PROCEED, EvaluationResult.SUCCESS, null);
    }

    private static Triplet<FlowInstruction, EvaluationResult, String> checkVaultCommands(VaultManager manager, String[] arguments) {
        if (VaultCommandType.GET.is(arguments[0])) {
            if (!manager.authenticate(arguments[1], arguments[2])) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        "Authentication failed to vault with name: " + arguments[1]
                );
            }
            return Triplet.of(
                    FlowInstruction.PROCEED,
                    EvaluationResult.SUCCESS,
                    "Authentication succeeded to vault with name: " + arguments[1]
            );
        }
        if (VaultCommandType.GET_ALL.is(arguments[0])) {
            Optional<List<String>> vaults = manager.getAllVaults();
            if (vaults.isEmpty()) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        "Failed to get all vaults"
                );
            }
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    buildTable(
                            CellSize.WRAP_SMALL,
                            List.of("Name"),
                            vaults.get().stream().map(Collections::singletonList).toList()
                    )
            );
        }
        if (VaultCommandType.ADD.is(arguments[0])) {
            if (!manager.addVault(arguments[1], arguments[2])) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        "Failed to add vault with name: " + arguments[1]
                );
            }
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    "Succeeded to add vault with name: " + arguments[1]
            );
        }
        if (VaultCommandType.REMOVE.is(arguments[0])) {
            if (!manager.removeVault(arguments[1], arguments[2])) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        "Failed to remove vault with name: " + arguments[1]
                );
            }
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    "Succeeded to remove vault with name: " + arguments[1]
            );
        }
        return Triplet.of(
                FlowInstruction.TERMINATE,
                EvaluationResult.FAILURE,
                "No command found with name: " + arguments[0]
        );
    }

    private static Triplet<LoopInstruction, EvaluationResult, String> checkGenericInteractiveCommands(VaultManager vaultManager, String[] arguments) {
        Optional<CredentialManager> credentialManager = vaultManager.getManager();
        if (credentialManager.isEmpty()) {
            return Triplet.of(
                    LoopInstruction.EXIT,
                    EvaluationResult.FAILURE,
                    "No current authenticated vault was found"
            );
        }
        if (GenericInteractiveCommandType.CURRENT_VAULT.is(arguments[0])) {
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    "Current vault: " + credentialManager.get().getVault().getName()
            );
        }
        if (GenericInteractiveCommandType.GENERATE_PASSWORD.is(arguments[0])) {
            int length = Integer.parseInt(arguments[1]);
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    "Generated password: " + credentialManager.get().generatePassword(length)
            );
        }
        if (GenericInteractiveCommandType.TEST_EXISTING_PASSWORD.is(arguments[0])) {
            Optional<Pair<PasswordStrength, String[]>> advices = credentialManager.get().testExistingPassword(arguments[1]);
            if (advices.isEmpty()) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        "No password found with platform: " + arguments[1]
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    buildTable(
                            CellSize.WRAP_SMALL,
                            List.of("Strength", "Score"),
                            List.of(List.of(
                                    advices.get().first().name(),
                                    String.valueOf(advices.get().first().score())
                            ))
                    ) + '\n' + buildTable(
                            CellSize.WRAP_MEDIUM,
                            List.of("Advice"),
                            Stream.of(advices.get().second()).map(Collections::singletonList).toList()
                    )
            );
        }
        if (GenericInteractiveCommandType.QUIT_SHELL.is(arguments[0]) || GenericInteractiveCommandType.EXIT_SHELL.is(arguments[0])) {
            return Triplet.of(
                    LoopInstruction.EXIT,
                    EvaluationResult.SUCCESS,
                    "Closing the shell..."
            );
        }
        return Triplet.of(
                LoopInstruction.CONTINUE,
                EvaluationResult.SUCCESS,
                null
        );
    }

    private static Triplet<LoopInstruction, EvaluationResult, String> checkCredentialCommands(VaultManager vaultManager, String[] arguments) {
        Optional<CredentialManager> credentialManager = vaultManager.getManager();
        if (credentialManager.isEmpty()) {
            return Triplet.of(
                    LoopInstruction.EXIT,
                    EvaluationResult.FAILURE,
                    "No current authenticated vault was found"
            );
        }
        if (CredentialCommandType.GET.is(arguments[0])) {
            Optional<Credential> credential = credentialManager.get().getCredential(arguments[1]);
            if (credential.isEmpty()) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        "No credential found with platform: " + arguments[1]
                );
            }
            Optional<String> username = credential.get().getDecryptedUsername();
            Optional<String> password = credential.get().getDecryptedPassword();
            if (username.isEmpty() || password.isEmpty()) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        "Failed to decrypt credential of platform: " + arguments[1]
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    buildTable(
                            CellSize.WRAP_SMALL,
                            List.of("Platform", "Username", "Password"),
                            List.of(List.of(
                                    credential.get().getPlatform(),
                                    username.get(),
                                    password.get()
                            ))
                    )
            );
        }
        if (CredentialCommandType.GET_ALL.is(arguments[0])) {
            Optional<List<String>> credentials = credentialManager.get().getAllCredentials();
            if (credentials.isEmpty()) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        "No credential found"
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    buildTable(
                            CellSize.WRAP_SMALL,
                            List.of("Platform"),
                            credentials.get().stream().map(Collections::singletonList).toList()
                    )
            );
        }
        if (CredentialCommandType.SET.is(arguments[0])) {
            if (!credentialManager.get().setCredential(arguments[1], arguments[2], arguments[3])) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        "Failed to set credential with platform: " + arguments[1]
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    "Succeeded to set credential with platform: " + arguments[1]
            );
        }
        if (CredentialCommandType.ADD.is(arguments[0])) {
            if (!credentialManager.get().addCredential(arguments[1], arguments[2], arguments[3])) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        "Failed to add credential with platform: " + arguments[1]
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    "Succeeded to add credential with platform: " + arguments[1]
            );
        }
        if (CredentialCommandType.REMOVE.is(arguments[0])) {
            if (!credentialManager.get().removeCredential(arguments[1])) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        "Failed to remove credential with platform: " + arguments[1]
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    "Succeeded to remove credential with platform: " + arguments[1]
            );
        }
        if (CredentialCommandType.REMOVE_ALL.is(arguments[0])) {
            if (!credentialManager.get().removeAllCredentials()) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        "Failed to remove all credentials"
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    "Succeeded to remove all credentials"
            );
        }
        return Triplet.of(
                LoopInstruction.CONTINUE,
                EvaluationResult.SUCCESS,
                null
        );
    }

    private static String buildTable(CellSize cellSize, List<String> attributes, List<List<String>> records) {
        try (TableBuilder builder = new TableBuilder(cellSize)) {
            builder.addAttributes(attributes);
            builder.addRecords(records);
            return builder.build();
        }
    }

}