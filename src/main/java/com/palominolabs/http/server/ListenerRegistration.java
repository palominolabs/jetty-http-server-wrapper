package com.palominolabs.http.server;

import com.google.common.base.Preconditions;
import com.google.inject.Provider;
import org.eclipse.jetty.server.handler.ContextHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EventListener;

/**
 * Handles adding an event listener instance or class
 */
final class ListenerRegistration {

    /**
     * Null iff listenerProvider is non-null.
     */
    @Nullable
    private final EventListener listener;

    /**
     * Null iff listener is non-null.
     */
    @Nullable
    private final Provider<? extends EventListener> listenerProvider;

    static ListenerRegistration forListener(@Nonnull EventListener listener) {
        return new ListenerRegistration(listener, null);
    }

    static ListenerRegistration forListenerProvider(@Nonnull Provider<? extends EventListener> listenerProvider) {
        return new ListenerRegistration(null, listenerProvider);
    }

    private ListenerRegistration(@Nullable EventListener listener,
        @Nullable Provider<? extends EventListener> listenerProvider) {
        Preconditions.checkArgument(
            (listener == null && listenerProvider != null) || (listener != null && listenerProvider == null),
            "One must be null and the other non-null");
        this.listener = listener;
        this.listenerProvider = listenerProvider;
    }

    void apply(ContextHandler handler) {
        if (listener != null) {
            handler.addEventListener(listener);
            return;
        }

        handler.addEventListener(listenerProvider.get());
    }
}
