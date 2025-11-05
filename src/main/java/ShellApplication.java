import com.asterexcrisys.acm.CredentialManager;
import com.asterexcrisys.acm.TokenManager;
import com.asterexcrisys.acm.VaultManager;
import com.asterexcrisys.acm.constants.GlobalConstants;
import com.asterexcrisys.acm.constants.HashingConstants;
import com.asterexcrisys.acm.constants.StorageConstants;
import com.asterexcrisys.acm.exceptions.HashingException;
import com.asterexcrisys.acm.services.console.TableBuilder;
import com.asterexcrisys.acm.services.encryption.GenericEncryptor;
import com.asterexcrisys.acm.services.encryption.KeyEncryptor;
import com.asterexcrisys.acm.services.storage.HardwareStore;
import com.asterexcrisys.acm.services.storage.SoftwareStore;
import com.asterexcrisys.acm.services.storage.Store;
import com.asterexcrisys.acm.services.utility.ConfigurationManager;
import com.asterexcrisys.acm.types.encryption.Token;
import com.asterexcrisys.acm.types.encryption.Vault;
import com.asterexcrisys.acm.types.encryption.VaultType;
import com.asterexcrisys.acm.types.storage.SoftwareStoreType;
import com.asterexcrisys.acm.types.storage.StoreMode;
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
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
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
            printMessage(Level.SEVERE, "An error occurred during the application start-up");
            System.exit(1);
            return;
        }
        Optional<SecretKey> masterKey = loadMasterKey();
        if (masterKey.isEmpty()) {
            printMessage(Level.SEVERE, "An error occurred while loading the master key");
            System.exit(1);
            return;
        }
        try (VaultManager manager = new VaultManager(masterKey.get())) {
            switch (checkNonInteractiveCommands(manager, programArguments)) {
                case Triplet(FlowInstruction instruction, EvaluationResult result, Message message) when instruction == FlowInstruction.TERMINATE -> {
                    if (!message.isSensitive()) {
                        LOGGER.log(result.level(), message.content());
                    }
                    reader.getHistory().purge();
                    printMessage(result.level(), message.content());
                    return;
                }
                case Triplet(FlowInstruction instruction, EvaluationResult result, Message message) when message.content() != null -> {
                    reader.getHistory().purge();
                    printMessage(result.level(), message.content());
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
                switch (checkInteractiveCommands(manager, shellArguments)) {
                    case Triplet(LoopInstruction instruction, EvaluationResult result, Message message) when instruction == LoopInstruction.EXIT -> {
                        if (!message.isSensitive()) {
                            LOGGER.log(result.level(), message.content());
                        }
                        reader.getHistory().purge();
                        printMessage(result.level(), message.content());
                        return;
                    }
                    case Triplet(LoopInstruction instruction, EvaluationResult result, Message message) when instruction == LoopInstruction.SKIP -> {
                        reader.getHistory().purge();
                        printMessage(result.level(), message.content());
                        continue;
                    }
                    case Triplet(LoopInstruction instruction, EvaluationResult result, Message message) when message.content() != null -> {
                        reader.getHistory().purge();
                        printMessage(result.level(), message.content());
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
        builder.option(Option.AUTO_LIST, true);
        builder.option(Option.LIST_PACKED, true);
        builder.option(Option.AUTO_MENU, true);
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

    private static void printMessage(Level level, String message) {
        if (level.intValue() >= Level.FINEST.intValue() && level.intValue() <= Level.INFO.intValue()) {
            System.out.println(message);
            System.out.flush();
        } else if (level.intValue() >= Level.WARNING.intValue() && level.intValue() <= Level.SEVERE.intValue()) {
            System.err.println(message);
            System.err.flush();
        }
    }

    private static String buildTable(CellSize cellSize, List<String> attributes, List<List<String>> records) {
        try (TableBuilder builder = new TableBuilder(cellSize)) {
            builder.addAttributes(attributes);
            builder.addRecords(records);
            return builder.build();
        }
    }

    // TODO: replace StoreMode.SOFTWARE with StoreMode.HARDWARE once TPM2 implementation is ready
    private static Optional<SecretKey> loadMasterKey() {
        ConfigurationManager manager = new ConfigurationManager();
        StoreMode mode = GlobalUtility.ifThrows(() -> {
            return StoreMode.valueOf(manager.get(StorageConstants.STORE_MODE_PROPERTY).orElseThrow());
        }, StoreMode.SOFTWARE);
        return switch (mode) {
            case HARDWARE -> {
                Outcome<HardwareStore, Exception> outcome = GlobalUtility.wrapExceptionNew(() -> {
                    return new HardwareStore(StorageConstants.MASTER_KEY_STORE);
                });
                if (outcome.isFailure()) {
                    LOGGER.severe("Error loading hardware store: " + outcome.getError().getMessage());
                    manager.put(StorageConstants.STORE_MODE_PROPERTY, StoreMode.SOFTWARE.name());
                    yield loadMasterKey();
                }
                manager.put(StorageConstants.STORE_MODE_PROPERTY, StoreMode.HARDWARE.name());
                yield outcome.getValue().retrieve(StorageConstants.MASTER_KEY_IDENTIFIER).or(() -> {
                    return createMasterKey(outcome.getValue());
                });
            }
            case SOFTWARE -> {
                Outcome<SoftwareStore, Exception> outcome = GlobalUtility.wrapExceptionNew(() -> {
                    return new SoftwareStore(
                            StorageConstants.MASTER_KEY_STORE,
                            GlobalUtility.getSystemIdentifier().orElseThrow(HashingException::new),
                            SoftwareStoreType.JCEKS
                    );
                });
                if (outcome.isFailure()) {
                    LOGGER.severe("Error loading software store: " + outcome.getError().getMessage());
                    manager.put(StorageConstants.STORE_MODE_PROPERTY, StoreMode.NONE.name());
                    yield loadMasterKey();
                }
                manager.put(StorageConstants.STORE_MODE_PROPERTY, StoreMode.SOFTWARE.name());
                yield outcome.getValue().retrieve(StorageConstants.MASTER_KEY_IDENTIFIER).or(() -> {
                    return createMasterKey(outcome.getValue());
                });
            }
            case NONE -> {
                ComputerSystem computerSystem = (new SystemInfo()).getHardware().getComputerSystem();
                manager.put(StorageConstants.STORE_MODE_PROPERTY, StoreMode.NONE.name());
                yield KeyEncryptor.deriveKey(
                        computerSystem.getHardwareUUID(),
                        EncryptionUtility.checkPadding(
                                computerSystem.getSerialNumber().getBytes(StandardCharsets.UTF_8),
                                HashingConstants.SALT_SIZE
                        )
                );
            }
        };
    }

    private static Optional<SecretKey> createMasterKey(Store<SecretKey> store) {
        Optional<SecretKey> key = GenericEncryptor.generateKey();
        if (key.isEmpty()) {
            return Optional.empty();
        }
        boolean isSaved = store.save(StorageConstants.MASTER_KEY_IDENTIFIER, key.get());
        if (!isSaved) {
            return Optional.empty();
        }
        return key;
    }

    private static Triplet<FlowInstruction, EvaluationResult, String> validateArguments(String[] arguments, ShellType type) {
        ShellArgumentParser parser = new ShellArgumentParser(type.filter());
        Outcome<? extends CommandType, String> outcome = parser.parseNew(arguments);
        if (outcome.isSuccess()) {
            return Triplet.of(FlowInstruction.PROCEED, EvaluationResult.SUCCESS, null);
        }
        return Triplet.of(FlowInstruction.TERMINATE, EvaluationResult.FAILURE, outcome.getError());
    }

    private static Triplet<FlowInstruction, EvaluationResult, Message> checkNonInteractiveCommands(VaultManager vaultManager, String[] arguments) {
        Triplet<FlowInstruction, EvaluationResult, Message> result = checkGenericNonInteractiveCommands(vaultManager, arguments);
        if (result.first() != FlowInstruction.PROCEED) {
            return result;
        }
        return checkVaultCommands(vaultManager, arguments);
    }

    private static Triplet<LoopInstruction, EvaluationResult, Message> checkInteractiveCommands(VaultManager vaultManager, String[] arguments) {
        Triplet<LoopInstruction, EvaluationResult, Message> result = checkGenericInteractiveCommands(vaultManager, arguments);
        if (result.first() != LoopInstruction.CONTINUE) {
            return result;
        }
        result = checkCredentialCommands(vaultManager, arguments);
        if (result.first() != LoopInstruction.CONTINUE) {
            return result;
        }
        return checkTokenCommands(vaultManager, arguments);
    }

    private static Triplet<FlowInstruction, EvaluationResult, Message> checkGenericNonInteractiveCommands(VaultManager manager, String[] arguments) {
        if (GenericNonInteractiveCommandType.SHOW_INFORMATION.is(arguments[0])) {
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    Message.of(buildTable(
                            CellSize.WRAP_SMALL,
                            List.of("Name", "Version"),
                            List.of(
                                    List.of(GlobalConstants.APPLICATION_NAME),
                                    List.of(GlobalConstants.APPLICATION_VERSION)
                            )
                    ), false)
            );
        }
        if (GenericNonInteractiveCommandType.IMPORT_VAULT.is(arguments[0])) {
            if (!manager.importVault(Paths.get(arguments[1]), VaultType.valueOf(arguments[2]), arguments[3], arguments[4])) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to import vault with name: " + arguments[2], false)
                );
            }
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to import vault with name: " + arguments[2], false)
            );
        }
        if (GenericNonInteractiveCommandType.EXPORT_VAULT.is(arguments[0])) {
            if (!manager.exportVault(Paths.get(arguments[1]), VaultType.valueOf(arguments[2]), arguments[3], arguments[4])) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to export vault with name: " + arguments[2], false)
                );
            }
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to export vault with name: " + arguments[2], false)
            );
        }
        if (GenericNonInteractiveCommandType.CLEAR_CONTEXT.is(arguments[0])) {
            boolean isHistoryCleared = PathUtility.deleteRecursively(Paths.get("./data/console/"));
            boolean isStatusCleared = PathUtility.deleteRecursively(Paths.get("./data/logs/"));
            if (!isHistoryCleared || !isStatusCleared) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to clear context: application's history records and/or status logs may have not been completely cleared", false)
                );
            }
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to clear context: application's history records and status logs have been cleared", false)
            );
        }
        if (GenericNonInteractiveCommandType.WIPE_DATA.is(arguments[0])) {
            String user = GlobalUtility.getSystemUser();
            if (!user.equalsIgnoreCase(arguments[1])) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to wipe data of user: " + user, true)
                );
            }
            boolean isDataCleared = PathUtility.deleteRecursively(Paths.get("./data/"));
            if (!isDataCleared) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to wipe data of user: " + user, true)
                );
            }
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to wipe data of user: " + user, true)
            );
        }
        if (GenericNonInteractiveCommandType.TEST_GIVEN_PASSWORD.is(arguments[0])) {
            Pair<PasswordStrength, String[]> advices = manager.testGivenPassword(arguments[1]);
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    Message.of(String.format(
                            "%s\n%s",
                            buildTable(
                                    CellSize.WRAP_SMALL,
                                    List.of("Strength", "Score"),
                                    List.of(List.of(
                                            advices.first().name(),
                                            String.valueOf(advices.first().score())
                                    ))
                            ),
                            buildTable(
                                    CellSize.WRAP_MEDIUM,
                                    List.of("Advice"),
                                    Stream.of(advices.second()).map(Collections::singletonList).toList()
                            )
                    ), true)
            );
        }
        return Triplet.of(FlowInstruction.PROCEED, EvaluationResult.SUCCESS, Message.of(null, false));
    }

    private static Triplet<FlowInstruction, EvaluationResult, Message> checkVaultCommands(VaultManager manager, String[] arguments) {
        if (VaultCommandType.GET.is(arguments[0])) {
            if (!manager.authenticate(arguments[1], arguments[2])) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        Message.of("Authentication failed to vault with name: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    FlowInstruction.PROCEED,
                    EvaluationResult.SUCCESS,
                    Message.of("Authentication succeeded to vault with name: " + arguments[1], false)
            );
        }
        if (VaultCommandType.GET_ALL.is(arguments[0])) {
            Optional<List<String>> vaults = manager.getAllVaults();
            if (vaults.isEmpty()) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to get all vaults", false)
                );
            }
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    Message.of(buildTable(
                            CellSize.WRAP_SMALL,
                            List.of("Name"),
                            vaults.get().stream().map(Collections::singletonList).toList()
                    ), true)
            );
        }
        if (VaultCommandType.ADD.is(arguments[0])) {
            if (!manager.addVault(arguments[1], arguments[2])) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to add vault with name: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to add vault with name: " + arguments[1], false)
            );
        }
        if (VaultCommandType.REMOVE.is(arguments[0])) {
            if (!manager.removeVault(arguments[1], arguments[2])) {
                return Triplet.of(
                        FlowInstruction.TERMINATE,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to remove vault with name: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    FlowInstruction.TERMINATE,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to remove vault with name: " + arguments[1], false)
            );
        }
        return Triplet.of(
                FlowInstruction.TERMINATE,
                EvaluationResult.FAILURE,
                Message.of("No command found with name: " + arguments[0], false)
        );
    }

    private static Triplet<LoopInstruction, EvaluationResult, Message> checkGenericInteractiveCommands(VaultManager vaultManager, String[] arguments) {
        Optional<CredentialManager> credentialManager = vaultManager.getCredentialManager();
        if (credentialManager.isEmpty()) {
            return Triplet.of(
                    LoopInstruction.EXIT,
                    EvaluationResult.FAILURE,
                    Message.of("No current authenticated vault was found", false)
            );
        }
        if (GenericInteractiveCommandType.CURRENT_VAULT.is(arguments[0])) {
            Optional<Vault> vault = credentialManager.get().getVault();
            if (vault.isEmpty()) {
                return Triplet.of(
                        LoopInstruction.EXIT,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to retrieve authenticated vault", false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of("Current vault: " + vault.get().getName(), true)
            );
        }
        if (GenericInteractiveCommandType.GENERATE_PASSWORD.is(arguments[0])) {
            int length = Integer.parseInt(arguments[1]);
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of("Generated password: " + credentialManager.get().generatePassword(length), true)
            );
        }
        if (GenericInteractiveCommandType.TEST_EXISTING_PASSWORD.is(arguments[0])) {
            Optional<Pair<PasswordStrength, String[]>> advices = credentialManager.get().testExistingPassword(arguments[1]);
            if (advices.isEmpty()) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("No password found with platform: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of(String.format(
                            "%s\n%s",
                            buildTable(
                                    CellSize.WRAP_SMALL,
                                    List.of("Strength", "Score"),
                                    List.of(List.of(
                                            advices.get().first().name(),
                                            String.valueOf(advices.get().first().score())
                                    ))
                            ),
                            buildTable(
                                    CellSize.WRAP_MEDIUM,
                                    List.of("Advice"),
                                    Stream.of(advices.get().second()).map(Collections::singletonList).toList()
                            )
                    ), true)
            );
        }
        if (GenericInteractiveCommandType.ENCRYPT_TEXT.is(arguments[0])) {
            Optional<String> encryptedText = vaultManager.getTokenManager().flatMap((tokenManager) -> {
                return tokenManager.encryptText(arguments[1], arguments[2]);
            });
            if (encryptedText.isEmpty()) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to encrypt text using token with identifier: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of("Encrypted text: " + encryptedText.get(), true)
            );
        }
        if (GenericInteractiveCommandType.DECRYPT_TEXT.is(arguments[0])) {
            Optional<String> decryptedText = vaultManager.getTokenManager().flatMap((tokenManager) -> {
                return tokenManager.decryptText(arguments[1], arguments[2]);
            });
            if (decryptedText.isEmpty()) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to decrypt text using token with identifier: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of("Decrypted text: " + decryptedText.get(), true)
            );
        }
        if (GenericInteractiveCommandType.ENCRYPT_FILE.is(arguments[0])) {
            boolean isEncrypted = vaultManager.getTokenManager().flatMap((tokenManager) -> {
                if (!tokenManager.encryptFile(arguments[1], Paths.get(arguments[2]))) {
                    return Optional.empty();
                }
                return Optional.of(tokenManager);
            }).isPresent();
            if (!isEncrypted) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to encrypt file using token with identifier: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to encrypt file using token with identifier: " + arguments[1], true)
            );
        }
        if (GenericInteractiveCommandType.DECRYPT_FILE.is(arguments[0])) {
            boolean isEncrypted = vaultManager.getTokenManager().flatMap((tokenManager) -> {
                if (!tokenManager.decryptFile(arguments[1], Paths.get(arguments[2]))) {
                    return Optional.empty();
                }
                return Optional.of(tokenManager);
            }).isPresent();
            if (!isEncrypted) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to decrypt file using token with identifier: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to decrypt file using token with identifier: " + arguments[1], true)
            );
        }
        if (GenericInteractiveCommandType.QUIT_SHELL.is(arguments[0]) || GenericInteractiveCommandType.EXIT_SHELL.is(arguments[0])) {
            return Triplet.of(
                    LoopInstruction.EXIT,
                    EvaluationResult.SUCCESS,
                    Message.of("Closing the shell...", false)
            );
        }
        return Triplet.of(
                LoopInstruction.CONTINUE,
                EvaluationResult.SUCCESS,
                Message.of(null, false)
        );
    }

    private static Triplet<LoopInstruction, EvaluationResult, Message> checkCredentialCommands(VaultManager vaultManager, String[] arguments) {
        Optional<CredentialManager> credentialManager = vaultManager.getCredentialManager();
        if (credentialManager.isEmpty()) {
            return Triplet.of(
                    LoopInstruction.EXIT,
                    EvaluationResult.FAILURE,
                    Message.of("No current authenticated vault was found", false)
            );
        }
        if (CredentialCommandType.GET.is(arguments[0])) {
            Optional<Credential> credential = credentialManager.get().getCredential(arguments[1]);
            if (credential.isEmpty()) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("No credential found with platform: " + arguments[1], false)
                );
            }
            Optional<String> username = credential.get().getDecryptedUsername();
            Optional<String> password = credential.get().getDecryptedPassword();
            if (username.isEmpty() || password.isEmpty()) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to decrypt credential of platform: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of(buildTable(
                            CellSize.WRAP_SMALL,
                            List.of("Platform", "Username", "Password", "Expiration", "Last Modification"),
                            List.of(List.of(
                                    credential.get().getPlatform(),
                                    username.get(),
                                    password.get(),
                                    credential.get().getExpiration().map(Instant::toString).orElse("N/A"),
                                    credential.get().getLastModification().toString()
                            ))
                    ), true)
            );
        }
        if (CredentialCommandType.GET_ALL.is(arguments[0])) {
            Optional<List<String>> credentials = credentialManager.get().getAllCredentials();
            if (credentials.isEmpty()) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("No credential found", false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of(buildTable(
                            CellSize.WRAP_SMALL,
                            List.of("Platform"),
                            credentials.get().stream().map(Collections::singletonList).toList()
                    ), true)
            );
        }
        if (CredentialCommandType.SET.is(arguments[0])) {
            if (!credentialManager.get().setCredential(arguments[1], arguments[2], arguments[3])) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to set credential with platform: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to set credential with platform: " + arguments[1], false)
            );
        }
        if (CredentialCommandType.ADD.is(arguments[0])) {
            if (!credentialManager.get().addCredential(arguments[1], arguments[2], arguments[3])) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to add credential with platform: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to add credential with platform: " + arguments[1], false)
            );
        }
        if (CredentialCommandType.REMOVE.is(arguments[0])) {
            if (!credentialManager.get().removeCredential(arguments[1])) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to remove credential with platform: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to remove credential with platform: " + arguments[1], false)
            );
        }
        if (CredentialCommandType.REMOVE_ALL.is(arguments[0])) {
            if (!credentialManager.get().removeAllCredentials()) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to remove all credentials", false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to remove all credentials", false)
            );
        }
        return Triplet.of(
                LoopInstruction.CONTINUE,
                EvaluationResult.SUCCESS,
                Message.of(null, false)
        );
    }

    private static Triplet<LoopInstruction, EvaluationResult, Message> checkTokenCommands(VaultManager vaultManager, String[] arguments) {
        Optional<TokenManager> tokenManager = vaultManager.getTokenManager();
        if (tokenManager.isEmpty()) {
            return Triplet.of(
                    LoopInstruction.EXIT,
                    EvaluationResult.FAILURE,
                    Message.of("No current authenticated vault was found", false)
            );
        }
        if (TokenCommandType.GET.is(arguments[0])) {
            Optional<Token> token = tokenManager.get().getToken(arguments[1]);
            if (token.isEmpty()) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("No token found with identifier: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of(buildTable(
                            CellSize.WRAP_SMALL,
                            List.of("Identifier", "Expiration", "Last Modification"),
                            List.of(List.of(
                                    token.get().getIdentifier(),
                                    token.get().getExpiration().map(Instant::toString).orElse("N/A"),
                                    token.get().getLastModification().toString()
                            ))
                    ), true)
            );
        }
        if (TokenCommandType.GET_ALL.is(arguments[0])) {
            Optional<List<String>> tokens = tokenManager.get().getAllTokens();
            if (tokens.isEmpty()) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("No token found", false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of(buildTable(
                            CellSize.WRAP_SMALL,
                            List.of("Identifier"),
                            tokens.get().stream().map(Collections::singletonList).toList()
                    ), true)
            );
        }
        if (TokenCommandType.SET.is(arguments[0])) {
            if (!tokenManager.get().setToken(arguments[1])) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to set token with identifier: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to set token with identifier: " + arguments[1], false)
            );
        }
        if (TokenCommandType.ADD.is(arguments[0])) {
            if (!tokenManager.get().addToken(arguments[1])) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to add token with identifier: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to add token with identifier: " + arguments[1], false)
            );
        }
        if (TokenCommandType.REMOVE.is(arguments[0])) {
            if (!tokenManager.get().removeToken(arguments[1])) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to remove token with identifier: " + arguments[1], false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to remove token with identifier: " + arguments[1], false)
            );
        }
        if (TokenCommandType.REMOVE_ALL.is(arguments[0])) {
            if (!tokenManager.get().removeAllTokens()) {
                return Triplet.of(
                        LoopInstruction.SKIP,
                        EvaluationResult.FAILURE,
                        Message.of("Failed to remove all tokens", false)
                );
            }
            return Triplet.of(
                    LoopInstruction.SKIP,
                    EvaluationResult.SUCCESS,
                    Message.of("Succeeded to remove all tokens", false)
            );
        }
        return Triplet.of(
                LoopInstruction.CONTINUE,
                EvaluationResult.SUCCESS,
                Message.of(null, false)
        );
    }

}