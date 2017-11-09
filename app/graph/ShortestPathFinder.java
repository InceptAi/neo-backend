package graph;


import models.UIPath;
import models.UIScreen;
import models.UIStep;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import storage.NavigationGraphStore;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class ShortestPathFinder implements PathFinder {
    private NavigationGraphStore navigationGraphStore;

    @Inject
    public ShortestPathFinder(NavigationGraphStore navigationGraphStore) {
        this.navigationGraphStore = navigationGraphStore;
    }

    @Override
    public UIPath findPathBetweenScreens(UIScreen srcScreen, UIScreen dstScreen) {
        DirectedGraph<String, UIStep> screenGraph = navigationGraphStore.getScreenGraph();
        GraphPath<String, UIStep> shortestPath =
                DijkstraShortestPath.findPathBetween(screenGraph, srcScreen.getId(), dstScreen.getId());
        if (shortestPath == null) {
            return new UIPath();
        }
        List<UIStep> uiStepList = shortestPath.getEdgeList();
        return new UIPath(uiStepList);
    }
}
