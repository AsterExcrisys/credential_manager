package com.asterexcrisys.acm.services.utility;

import com.asterexcrisys.acm.utility.HashingUtility;
import org.bouncycastle.util.Arrays;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class ClipboardManager {

    private static final Logger LOGGER = Logger.getLogger(ClipboardManager.class.getName());

    private final Clipboard clipboard;
    private String lastSelection;

    public ClipboardManager() {
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        lastSelection = null;
    }

    public boolean isOwner() {
        if (lastSelection == null) {
            return false;
        }
        Optional<String> currentSelection = get().flatMap(HashingUtility::hashMessage);
        if (currentSelection.isEmpty()) {
            return false;
        }
        return Arrays.areEqual(lastSelection.getBytes(), currentSelection.get().getBytes());
    }

    public void addListener(FlavorListener listener) {
        clipboard.addFlavorListener(Objects.requireNonNull(listener));
    }

    public void removeListener(FlavorListener listener) {
        clipboard.removeFlavorListener(Objects.requireNonNull(listener));
    }

    public Optional<String> get() {
        Transferable content = clipboard.getContents(DataFlavor.stringFlavor);
        if (content == null || !content.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return Optional.empty();
        }
        try {
            return Optional.of(content.getTransferData(DataFlavor.stringFlavor).toString());
        } catch (UnsupportedFlavorException | IOException e) {
            LOGGER.warning("Error retrieving data from the clipboard: " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean set(String data) {
        if (data == null || data.isBlank()) {
            return false;
        }
        Optional<String> currentSelection = HashingUtility.hashMessage(data);
        if (currentSelection.isEmpty()) {
            return false;
        }
        clipboard.setContents(new StringSelection(data), null);
        lastSelection = currentSelection.get();
        return true;
    }

    public void clear() {
        clipboard.setContents(new StringSelection(""), null);
        lastSelection = null;
    }

}