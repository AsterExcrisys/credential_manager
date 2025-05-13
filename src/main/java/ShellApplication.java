import com.asterexcrisys.acm.CredentialManager;
import com.asterexcrisys.acm.VaultManager;
import com.asterexcrisys.acm.constants.Global;
import com.asterexcrisys.acm.services.Utility;
import com.asterexcrisys.acm.services.console.ArgumentParser;
import com.asterexcrisys.acm.types.console.CredentialCommandType;
import com.asterexcrisys.acm.types.console.GenericCommandType;
import com.asterexcrisys.acm.types.utility.Pair;
import com.asterexcrisys.acm.types.utility.PasswordStrength;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.LineReader;
import org.jline.reader.LineReader.Option;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Reference;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.jline.keymap.KeyMap.alt;
import static org.jline.keymap.KeyMap.ctrl;

@SuppressWarnings("unused")
public class ShellApplication {

    private static final Logger LOGGER = Logger.getLogger(ShellApplication.class.getName());

    public static void main(String[] programArguments) {
        LineReader reader;
        try {
            Files.createDirectories(Paths.get("./data/"));
            configureParser(programArguments);
            configureLogger(Logger.getGlobal());
            reader = configureReader();
        } catch (Exception e) {
            System.out.println("An error occurred during the application startup");
            System.exit(1);
            return;
        }
        try (VaultManager manager = new VaultManager(null, null)) {
            while (true) {
                String[] shellArguments = reader.readLine(Global.SHELL_PROMPT).trim().split("\\s+");
                if (checkGenericCommands(reader, manager, shellArguments)) {
                    break;
                }
                if (checkCredentialCommands(reader, manager, shellArguments)) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred during the application execution");
            System.exit(1);
        }
    }

    private static void configureParser(String[] arguments) {
        // TODO: use JLine to configure the argument parser
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
        builder.appName(Global.APPLICATION_NAME);
        builder.terminal(configureTerminal());
        builder.parser(new ArgumentParser(Utility.InteractiveShell::fromValue));
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
        builder.streams(System.in, System.out);
        builder.encoding(StandardCharsets.UTF_8);
        return builder.build();
    }

    private static boolean checkGenericCommands(LineReader reader, VaultManager manager, String[] arguments) {
        if (!manager.isAuthenticated()) {
            return false;
        }
        Optional<CredentialManager> optional = manager.getManager();
        if (optional.isEmpty()) {
            System.out.println("No current vault was found");
            return false;
        }

        if (GenericCommandType.CURRENT_VAULT.is(arguments[0])) {
            System.out.println("Current vault: " + optional.get().getVault().getName());
            return false;
        }
        if (GenericCommandType.GENERATE_PASSWORD.is(arguments[0])) {
            int length = Integer.parseInt(arguments[1]);
            System.out.println("Generated password: " + optional.get().generatePassword(length));
            return false;
        }
        if (GenericCommandType.TEST_GIVEN.is(arguments[0])) {
            System.out.println("Advices: " + optional.get().testGivenPassword(arguments[1]));
            return false;
        }
        if (GenericCommandType.TEST_EXISTING.is(arguments[0])) {
            Optional<Pair<PasswordStrength, String[]>> advices = optional.get().testExistingPassword(arguments[1]);
            if (advices.isPresent()) {
                System.out.println("Advices: " + advices.get());
            } else {
                System.out.println("No password found with platform: " + arguments[1]);
            }
            return false;
        }
        return GenericCommandType.QUIT.is(arguments[0]);
    }

    private static boolean checkVaultCommands(LineReader reader, VaultManager manager, String[] arguments) {
        return false;
    }

    private static boolean checkCredentialCommands(LineReader reader, VaultManager manager, String[] arguments) {
        return false;
    }

}