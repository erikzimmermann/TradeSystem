package de.codingair.tradesystem.spigot.utils;

/**
 * Just like the {@link java.util.function.Supplier} but with the extension of a throwable.
 *
 * @param <A> The class that should be returned.
 * @param <T> The class of the throwable.
 */
public interface Supplier<A, T extends Throwable> {
    A get() throws T;
}
