package Core.Utils;

import javafx.application.Platform;

import java.util.Objects;

public final class ConsoleGUIUtils
{
    private ConsoleGUIUtils() {
        throw new UnsupportedOperationException();
    }

    public static void runSafe(final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        }
        else {
            Platform.runLater(runnable);
        }
    }
}