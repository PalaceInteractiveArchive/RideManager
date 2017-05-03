package network.palace.ridemanager.threads;

/**
 * Created by Marc on 5/2/17.
 */
public interface Callback<V> {

    /**
     * Called when the result is done.
     *
     * @param result the result of the computation
     * @param error  the error(s) that occurred, if any
     */
    void done(V result, Throwable error);
}
