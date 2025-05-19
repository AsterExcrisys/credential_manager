import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public class TpmHandler {

    private static final Linker linker;

    static {
        linker = Linker.nativeLinker();
    }

    public static void main(String[] args) throws Throwable {
        System.load("tpm_handler.so");

        SymbolLookup symbols = SymbolLookup.loaderLookup();

        MethodHandle initializeTpm = linker.downcallHandle(
            symbols.find("initialize_tpm").orElseThrow(),
            FunctionDescriptor.of(ValueLayout.JAVA_INT)
        );

        MethodHandle finalizeTpm = linker.downcallHandle(
            symbols.find("finalize_tpm").orElseThrow(),
            FunctionDescriptor.ofVoid()
        );

        int result = (int) initializeTpm.invokeExact();
        if (result != 0) {
            System.err.println("TPM initialization failed: " + result);
            return;
        }

        System.out.println("TPM initialized successfully.");

        // Clean up
        finalizeTpm.invokeExact();
        System.out.println("TPM cleaned up.");
    }

}