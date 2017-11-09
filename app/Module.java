import com.google.inject.AbstractModule;
import java.time.Clock;

import graph.PathFinder;
import graph.ShortestPathFinder;
import services.*;
import storage.NavigationGraphStore;
import storage.SemanticActionStore;
import services.UIScreenManager;
import util.Utils;

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 *
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
public class Module extends AbstractModule {

    @Override
    public void configure() {
        // Use the system clock as the default implementation of Clock
        bind(Clock.class).toInstance(Clock.systemDefaultZone());
        // Ask Guice to create an instance of ApplicationTimer when the
        // application starts.
        bind(ApplicationTimer.class).asEagerSingleton();
        // Set AtomicCounter as the implementation for Counter.
        bind(Counter.class).to(AtomicCounter.class);
        bind(DatabaseBackend.class).to(FirestoreBackend.class);
        bind(PathFinder.class).to(ShortestPathFinder.class);
        bind(UIScreenManager.class).asEagerSingleton();
        bind(SemanticActionStore.class).asEagerSingleton();
        bind(NavigationGraphStore.class).asEagerSingleton();
        //initialize neo stuff
        Utils.printDebug("In Configure of module");
        bind(NeoLifecycle.class).asEagerSingleton();
    }
}
