package com.palominolabs.http.server;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Captures a provider from a binder for later use. This allows you to create a Provider for something before you've run
 * your module's configure() or have a reference to the resulting Injector. The provider will only work once the
 * injector has been created, naturally.
 *
 * Binders will let you get a provider for a key, but that's only possible at module configuration time. This wrapper
 * class allows you to effectively save a provider for something at module construction time.
 *
 * To use, call saveProvider on your instance inside your module's configure() method, and pass the instance around
 * wherever you need the relevant Provider. It simply proxies the binder's Provider, so it will obey the same scoping,
 * etc.
 *
 * It saves the provider to a volatile field, so you can safely use this Provider in threads other than the one that ran
 * the module configure() method. However, this class is really just designed for working around awkward chicken-and-egg
 * problems while initializing a system, so if you find yourself using it beyond that, are you sure you can't just
 * inject things normally into whatever you're currently passing this Provider implementation to?
 *
 * @param <T> the type to capture a provider for
 */
@ThreadSafe
public class BinderProviderCapture<T> implements Provider<T> {

    @Nonnull
    private final Key<T> key;

    /**
     * Non-null once saveProvider is called.
     */
    volatile private Provider<T> provider;

    /**
     * @param key Key to capture a provider for.
     */
    public BinderProviderCapture(@Nonnull Key<T> key) {
        this.key = key;
    }

    /**
     * @param klass Class to capture a provider for.
     */
    public BinderProviderCapture(@Nonnull Class<T> klass) {
        this(Key.get(klass));
    }

    /**
     * Call this from inside your module's configure().
     *
     * @param binder Guice binder
     */
    public void saveProvider(Binder binder) {
        provider = binder.getProvider(key);
    }

    @Override
    public T get() {
        return provider.get();
    }
}
