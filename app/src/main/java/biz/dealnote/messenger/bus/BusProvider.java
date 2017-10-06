package biz.dealnote.messenger.bus;

import com.squareup.otto.Bus;

/**
 * Maintains a singleton instance for obtaining the bus. Ideally this would be replaced with a more efficient means
 * such as through injection directly into interested classes.
 */
public final class BusProvider {

    private static final Bus BUS = new MainThreadBus(new Bus());

    private BusProvider() {
        // No instances.
    }

    public static Bus getInstance() {
        return BUS;
    }
}