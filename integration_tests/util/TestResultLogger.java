package util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.util.Optional;

/**
 * JUnit 5 extension that prints one result line per test method.
 * Registered via BaseIT — all IT subclasses inherit it automatically.
 */
public class TestResultLogger implements TestWatcher {

    @Override
    public void testSuccessful(ExtensionContext ctx) {
        System.out.printf("  ✓  %s%n", ctx.getDisplayName());
    }

    @Override
    public void testFailed(ExtensionContext ctx, Throwable cause) {
        System.out.printf("  ✗  %s  —  %s%n", ctx.getDisplayName(), cause.getMessage());
    }

    @Override
    public void testDisabled(ExtensionContext ctx, Optional<String> reason) {
        System.out.printf("  ○  %s  [DISABLED]%n", ctx.getDisplayName());
    }

    @Override
    public void testAborted(ExtensionContext ctx, Throwable cause) {
        System.out.printf("  ○  %s  [ABORTED]%n", ctx.getDisplayName());
    }
}
