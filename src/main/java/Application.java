import com.asterexcrisys.acm.constants.Global;
import com.asterexcrisys.acm.services.Utility;
import com.asterexcrisys.acm.types.console.CredentialCommandType;
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
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.jline.keymap.KeyMap.alt;

@SuppressWarnings("unused")
public class Application {

    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

    public static void main(String[] arguments) {
        try {
            configureParser(arguments);
            configureLogger(Logger.getGlobal());
            LineReader reader = configureReader();
        } catch (IOException e) {
            System.out.println("An error occurred during the application execution, ");
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
        bindings.bind(new Reference("upcase-word"), alt('U'));
        bindings.bind(new Reference("downcase-word"), alt('L'));
        return reader;
    }

    private static Terminal configureTerminal() throws IOException {
        TerminalBuilder builder = TerminalBuilder.builder();
        builder.system(true);
        builder.streams(System.in, System.out);
        builder.encoding(StandardCharsets.UTF_8);
        return builder.build();
    }

}