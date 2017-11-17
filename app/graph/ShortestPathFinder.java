package graph;


import models.UIPath;
import models.UIScreen;
import models.UIStep;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import storage.NavigationGraphStore;
import util.Utils;

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
        if (srcScreen == null || dstScreen == null) {
            return null;
        }

        if (srcScreen.equals(dstScreen)) {
            return new UIPath();
        }

        DirectedGraph<String, UIStep> screenGraph = navigationGraphStore.getScreenGraph();
        GraphPath<String, UIStep> shortestPath = null;
        try {
            shortestPath = DijkstraShortestPath.findPathBetween(screenGraph, srcScreen.getId(), dstScreen.getId());
        } catch (IllegalArgumentException e) {
            Utils.printDebug("Exception in shortest path computation " + e.toString());
        }

        if (shortestPath == null) {
            return null;
        }

        List<UIStep> uiStepList = shortestPath.getEdgeList();
        return new UIPath(uiStepList);
    }
}
