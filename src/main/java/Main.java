import com.asterexcrisys.acm.services.Utility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Main {

    // Full Project Name: Aegis Credential Manager

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] arguments) throws IOException {
        configureParser(arguments);
        configureLogger(Logger.getGlobal());
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

}