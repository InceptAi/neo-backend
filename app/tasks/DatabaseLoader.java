package tasks;

import akka.actor.ActorSystem;
import javax.inject.Inject;
import scala.concurrent.duration.Duration;
import java.util.concurrent.TimeUnit;

public class DatabaseLoader {

    private final ActorSystem actorSystem;
    private final ScreenMapInitializationExecutionContext executor;

    @Inject
    public DatabaseLoader(ActorSystem actorSystem,
                          ScreenMapInitializationExecutionContext executor) {
        this.actorSystem = actorSystem;
        this.executor = executor;

        this.initialize();
    }

    private void initialize() {
        this.actorSystem.scheduler().scheduleOnce(
                Duration.create(1, TimeUnit.SECONDS), // initialDelay
                () -> System.out.println("Running block of code"),
                this.executor // using the custom executor
        );
    }
}
