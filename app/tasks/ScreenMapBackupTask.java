package tasks;

import akka.actor.ActorSystem;
import javax.inject.Inject;

import play.libs.concurrent.CustomExecutionContext;
import scala.concurrent.duration.Duration;
import services.UIScreenManager;

import java.util.concurrent.TimeUnit;

import static config.BackendConfiguration.INTERVAL_FOR_BACKING_UP_SCREEN_MAP_MINUTES;

@SuppressWarnings("unused")
public class ScreenMapBackupTask {

    private final ActorSystem actorSystem;
    private final ScreenMapBackupContext executor;
    private UIScreenManager uiScreenManager;

    private static class ScreenMapBackupContext extends CustomExecutionContext {

        @Inject
        public ScreenMapBackupContext(ActorSystem actorSystem) {
            super(actorSystem, "load-from-database-dispatcher");
        }
    }


    @Inject
    public ScreenMapBackupTask(ActorSystem actorSystem,
                               ScreenMapBackupContext executor,
                               UIScreenManager uiScreenManager) {
        this.actorSystem = actorSystem;
        this.executor = executor;
        this.uiScreenManager = uiScreenManager;

        this.initialize();
    }

    private void initialize() {
        this.actorSystem.scheduler().schedule(
                Duration.create(10, TimeUnit.SECONDS), // initialDelay
                Duration.create(INTERVAL_FOR_BACKING_UP_SCREEN_MAP_MINUTES, TimeUnit.MINUTES), // interval
                () -> {
                    System.out.println("Running screen backup");
                    uiScreenManager.writeAllScreensToBackendAsync();
                },
                //() -> System.out.println("Running block of code"),
                this.executor // using the custom executor
        );
    }
}
