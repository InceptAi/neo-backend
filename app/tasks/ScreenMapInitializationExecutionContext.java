package tasks;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;
import javax.inject.Inject;

public class ScreenMapInitializationExecutionContext extends CustomExecutionContext {

    @Inject
    public ScreenMapInitializationExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, "load-from-database-dispatcher");
    }
}