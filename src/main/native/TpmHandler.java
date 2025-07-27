import com.asterexcrisys.acm.exceptions.NativeException;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Logger;

public class TpmHandler implements AutoCloseable {

    private static final String NATIVE_LIBRARY = "tpm_handler.c";
    private static final Logger LOGGER;
    private static final Linker LINKER;
    private static final SymbolLookup LOOKUP;

    private final MemorySegment context;
    private final MethodHandle sealKey;
    private final MethodHandle unsealKey;

    static {
        System.load(NATIVE_LIBRARY);
        LOGGER = Logger.getLogger(TpmHandler.class.getName());
        LINKER = Linker.nativeLinker();
        LOOKUP = SymbolLookup.loaderLookup();
    }

    public TpmHandler() throws Throwable {
        MethodHandle initialiseContext = LINKER.downcallHandle(
                LOOKUP.find("initialize_context").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS)
        );
        context = (MemorySegment) initialiseContext.invokeExact();
        if (context.address() == 0) {
            throw new NativeException();
        }
        sealKey = LINKER.downcallHandle(
                LOOKUP.find("seal_key_to_file").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        );
        unsealKey = LINKER.downcallHandle(
                LOOKUP.find("unseal_key_from_file").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );
    }

    public boolean sealKey(String key, String fileName) {
        if (key == null || key.isBlank() || fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Parameters cannot be null or blank");
        }
        try (Arena arena = Arena.ofAuto()) {
            MemorySegment keyPointer = arena.allocateUtf8String(key);
            MemorySegment fileNamePointer = arena.allocateUtf8String(fileName);
            long keyLength = keyPointer.byteSize() - 1;
            return (boolean) sealKey.invokeExact(context, keyPointer, keyLength, fileNamePointer);
        } catch (Throwable e) {
            LOGGER.warning("Error sealing the key to file: " + e.getMessage());
            return false;
        }
    }

    public Optional<String> unsealKey(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Parameters cannot be null or blank");
        }
        try (Arena arena = Arena.ofAuto()) {
            MemorySegment fileNamePointer = arena.allocateUtf8String(fileName);
            MemorySegment keyLengthPointer = arena.allocate(ValueLayout.JAVA_LONG);
            MemorySegment keyPointer = (MemorySegment) unsealKey.invokeExact(context, fileNamePointer, keyLengthPointer);
            if (keyPointer.address() == 0) {
                return Optional.empty();
            }
            long keyLength = keyLengthPointer.get(ValueLayout.JAVA_LONG, 0);
            ByteBuffer buffer = keyPointer.asByteBuffer().limit((int) keyLength);
            byte[] key = new byte[(int) keyLength];
            buffer.get(key);
            return Optional.of(new String(key, StandardCharsets.UTF_8));
        } catch (Throwable e) {
            LOGGER.warning("Error unsealing the key from file: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void close() {
        MethodHandle finaliseContext = LINKER.downcallHandle(
                LOOKUP.find("finalise_context").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
        try {
            finaliseContext.invokeExact(context);
        } catch (Throwable e) {
            LOGGER.warning("Error clearing up resources used by TPM2 context: " + e.getMessage());
        }
    }

}