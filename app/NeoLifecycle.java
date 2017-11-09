import play.inject.ApplicationLifecycle;
import util.Utils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("WeakerAccess")
@Singleton
public class NeoLifecycle {

    @Inject
    public NeoLifecycle(ApplicationLifecycle applicationLifecycle) {
        initialize();
        applicationLifecycle.addStopHook(() -> {
            //Long runningTime = stop.getEpochSecond() - start.getEpochSecond();
            //logger.info("ApplicationTimer demo: Stopping application at " + clock.instant() + " after " + runningTime + "s.");
            Utils.printDebug("In stop hook");
            shutdown();
            return CompletableFuture.completedFuture(null);
        });
    }

    private void initialize() {
        //Put all global initializations here
    }

    private void shutdown() {
        //Synchronous Flush to the database
        Utils.printDebug("In shutdown");
    }
}
