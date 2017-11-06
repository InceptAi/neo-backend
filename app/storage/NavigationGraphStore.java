package storage;

import models.UIStep;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import util.Utils;


public class NavigationGraphStore {
    private static NavigationGraphStore instance;
    private DirectedGraph<String, UIStep> screenGraph;

    private NavigationGraphStore() {
        screenGraph = new DefaultDirectedGraph<String, UIStep>(UIStep.class);
    }

    public static NavigationGraphStore getInstance() {
        if (instance == null) {
            instance = new NavigationGraphStore();
        }
        return instance;
    }

    public boolean addNavigationEdgeToGraph(UIStep uiStep) {
        if (uiStep == null) {
            return false;
        }

        UIStep currentEdge = screenGraph.getEdge(uiStep.getSrcScreenId(), uiStep.getDstScreenId());
        if (currentEdge == null || !uiStep.isSoftStep() || currentEdge.isSoftStep()) {
            screenGraph.addVertex(uiStep.getSrcScreenId());
            screenGraph.addVertex(uiStep.getDstScreenId());
            Utils.printDebug("Adding src, dst, uistep: " + uiStep.getSrcScreenId() + ", " +  uiStep.getDstScreenId() + "," + uiStep.toString());
        }
        return screenGraph.addEdge(uiStep.getSrcScreenId(), uiStep.getDstScreenId(), uiStep);
    }

    public DirectedGraph<String, UIStep> getScreenGraph() {
        return screenGraph;
    }
}
