import com.asterexcrisys.acm.CredentialManager;
import com.asterexcrisys.acm.VaultManager;
import com.asterexcrisys.acm.constants.Global;
import com.asterexcrisys.acm.services.Utility;
import com.asterexcrisys.acm.services.console.handlers.ShellSignalHandler;
import com.asterexcrisys.acm.services.console.parsers.ShellArgumentParser;
import com.asterexcrisys.acm.types.console.*;
import com.asterexcrisys.acm.types.encryption.Credential;
import com.asterexcrisys.acm.types.utility.Pair;
import com.asterexcrisys.acm.types.utility.PasswordStrength;
import com.asterexcrisys.acm.types.utility.Result;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.LineReader;
import org.jline.reader.LineReader.Option;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Reference;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.jline.keymap.KeyMap.alt;
import static org.jline.keymap.KeyMap.ctrl;

@SuppressWarnings("unused")
public class ShellApplication {

    // TODO: gcc -fPIC -shared -o tpm_handler.so tpm_handler.c -ltss2-esys
    // TODO: https://mvnrepository.com/artifact/com.github.oshi/oshi-core/6.8.1

    private static final Logger LOGGER = Logger.getLogger(ShellApplication.class.getName());

    public static void main(String[] programArguments) {
        LineReader reader;
        try {
            Files.createDirectories(Paths.get("./data/"));
            configureLogger(Logger.getGlobal());
            reader = configureReader();
            if (validateArguments(programArguments, ShellType.NON_INTERACTIVE)) {
                return;
            }
        } catch (Exception e) {
            System.out.println("An error occurred during the application startup");
            System.exit(1);
            return;
        }
        try (VaultManager manager = new VaultManager("test", "test")) {
            if (checkVaultCommands(manager, programArguments)) {
                return;
            }
            while (true) {
                String[] commandArguments = reader.readLine(Global.SHELL_PROMPT).trim().split("\\s+");
                if (validateArguments(commandArguments, ShellType.INTERACTIVE)) {
                    continue;
                }
                if (checkGenericCommands(manager, commandArguments)) {
                    return;
                }
                if (checkCredentialCommands(manager, commandArguments)) {
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred during the application execution");
            System.exit(1);
        }
    }

    private static void configureLogger(Logger logger) throws IOException {
        Files.createDirectories(Paths.get("./data/logs/"));
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);
        FileHandler fileHandler = new FileHandler(
                String.format("./data/logs/%s.log", Utility.getCurrentDate()),
                true
        );
        fileHandler.setLevel(Level.ALL);
        logger.addHandler(fileHandler);
    }

    private static LineReader configureReader() throws IOException {
        Files.createDirectories(Paths.get("./data/console/"));
        LineReaderBuilder builder = LineReaderBuilder.builder();
        builder.appName(String.format("%s (%s)", Global.APPLICATION_NAME, Global.APPLICATION_VERSION));
        builder.terminal(configureTerminal());
        builder.parser(new DefaultParser());
        builder.completer(new StringsCompleter(Arrays.stream(CredentialCommandType.values()).map(CredentialCommandType::longName).toArray(String[]::new)));
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
        bindings.bind(new Reference(Global.UPCASE_BINDING), alt('U'));
        bindings.bind(new Reference(Global.DOWNCASE_BINDING), alt('L'));
        bindings.bind(new Reference(LineReader.TRANSPOSE_CHARS), ctrl('T'));
        bindings.bind(new Reference(LineReader.KILL_LINE), ctrl('K'));
        bindings.bind(new Reference(LineReader.HISTORY_INCREMENTAL_SEARCH_BACKWARD), ctrl('R'));
        return reader;
    }

    private static Terminal configureTerminal() throws IOException {
        TerminalBuilder builder = TerminalBuilder.builder();
        builder.system(true);
        builder.name(String.format("%s (%s)", Global.APPLICATION_NAME, Global.APPLICATION_VERSION));
        builder.type(Global.TERMINAL_TYPE);
        builder.streams(System.in, System.out);
        builder.encoding(StandardCharsets.UTF_8);
        builder.nativeSignals(true);
        builder.signalHandler(new ShellSignalHandler());
        return builder.build();
    }

    private static boolean validateArguments(String[] arguments, ShellType type) {
        ShellArgumentParser parser = new ShellArgumentParser(type.filter());
        Result<? extends CommandType, String> result = parser.parse(arguments);
        if (result.isSuccess()) {
            return false;
        }
        Optional<String> error = result.getError();
        if (error.isPresent()) {
            System.out.println(error.get());
        } else {
            System.out.println("Unknown error");
        }
        return true;
    }

    private static boolean checkVaultCommands(VaultManager manager, String[] arguments) {
        if (VaultCommandType.GET.is(arguments[0])) {
            if (!manager.authenticate(arguments[1], arguments[2])) {
                System.out.println("Authentication failed to vault with name: " + arguments[1]);
                return true;
            }
            System.out.println("Authenticated succeeded to vault with name: " + arguments[1]);
            return false;
        }
        if (VaultCommandType.GET_ALL.is(arguments[0])) {
            System.out.println("Existing vaults: " + manager.getAllVaults());
            return true;
        }
        if (VaultCommandType.ADD.is(arguments[0])) {
            manager.addVault(arguments[1], arguments[2]);
            System.out.println("Added vault: " + arguments[1]);
            return true;
        }
        if (VaultCommandType.REMOVE.is(arguments[0])) {
            manager.removeVault(arguments[1], arguments[2]);
            System.out.println("Removed vault: " + arguments[1]);
            return true;
        }
        if (VaultCommandType.IMPORT.is(arguments[0])) {
            // TODO: to be implemented
            return true;
        }
        if (VaultCommandType.EXPORT.is(arguments[0])) {
            // TODO: to be implemented
            return true;
        }
        if (VaultCommandType.TEST_GIVEN.is(arguments[0])) {
            System.out.println("Advices: " + manager.testGivenPassword(arguments[1]));
            return false;
        }
        System.out.println("No command found with name: " + arguments[0]);
        return true;
    }

    private static boolean checkGenericCommands(VaultManager vaultManager, String[] arguments) {
        Optional<CredentialManager> credentialManager = vaultManager.getManager();
        if (credentialManager.isEmpty()) {
            System.out.println("No current authenticated vault was found");
            return true;
        }
        if (GenericCommandType.CURRENT_VAULT.is(arguments[0])) {
            System.out.println("Current vault: " + credentialManager.get().getVault().getName());
            return false;
        }
        if (GenericCommandType.GENERATE_PASSWORD.is(arguments[0])) {
            int length = Integer.parseInt(arguments[1]);
            System.out.println("Generated password: " + credentialManager.get().generatePassword(length));
            return false;
        }
        if (GenericCommandType.TEST_EXISTING.is(arguments[0])) {
            Optional<Pair<PasswordStrength, String[]>> advices = credentialManager.get().testExistingPassword(arguments[1]);
            if (advices.isPresent()) {
                System.out.println("Advices: " + advices.get());
            } else {
                System.out.println("No password found with platform: " + arguments[1]);
            }
            return false;
        }
        if (GenericCommandType.QUIT.is(arguments[0]) || GenericCommandType.EXIT.is(arguments[0])) {
            System.out.println("Closing the shell...");
            return true;
        }
        return false;
    }

    private static boolean checkCredentialCommands(VaultManager vaultManager, String[] arguments) {
        Optional<CredentialManager> credentialManager = vaultManager.getManager();
        if (credentialManager.isEmpty()) {
            System.out.println("No current authenticated vault was found");
            return true;
        }
        if (CredentialCommandType.GET.is(arguments[0])) {
            Optional<Credential> credential = credentialManager.get().getCredential(arguments[1]);
            if (credential.isEmpty()) {
                System.out.println("No credential found with platform: " + arguments[1]);
                return false;
            }
            System.out.println("Platform: " + credential.get().getPlatform());
            System.out.println("Username: " + credential.get().getDecryptedUsername());
            System.out.println("Password: " + credential.get().getDecryptedPassword());
            return false;
        }
        if (CredentialCommandType.GET_ALL.is(arguments[0])) {
            Optional<List<String>> credentials = credentialManager.get().getAllCredentials();
            if (credentials.isEmpty()) {
                System.out.println("No credential found");
                return false;
            }
            System.out.println(credentials.get());
            return false;
        }
        if (CredentialCommandType.SET.is(arguments[0])) {
            if (!credentialManager.get().setCredential(arguments[1], arguments[2], arguments[3])) {
                System.out.println("Failed to set credential with platform: " + arguments[1]);
                return false;
            }
            System.out.println("Succeeded to set credential with platform: " + arguments[1]);
            return false;
        }
        if (CredentialCommandType.ADD.is(arguments[0])) {
            if (!credentialManager.get().addCredential(arguments[1], arguments[2], arguments[3])) {
                System.out.println("Failed to add credential with platform: " + arguments[1]);
                return false;
            }
            System.out.println("Succeeded to add credential with platform: " + arguments[1]);
            return false;
        }
        if (CredentialCommandType.REMOVE.is(arguments[0])) {
            if (!credentialManager.get().removeCredential(arguments[1])) {
                System.out.println("Failed to remove credential with platform: " + arguments[1]);
                return false;
            }
            System.out.println("Succeeded to remove credential with platform: " + arguments[1]);
            return false;
        }
        if (CredentialCommandType.REMOVE_ALL.is(arguments[0])) {
            if (!credentialManager.get().removeAllCredentials()) {
                System.out.println("Failed to remove all credentials");
                return false;
            }
            System.out.println("Succeeded to remove all credentials");
            return false;
        }
        return false;
    }

}