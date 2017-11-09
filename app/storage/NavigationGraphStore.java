package storage;

import models.UIScreen;
import models.UIStep;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import util.Utils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;

@Singleton
public class NavigationGraphStore {
    private DirectedGraph<String, UIStep> screenGraph;

    @Inject
    public NavigationGraphStore()
    {
        screenGraph = new DefaultDirectedGraph<String, UIStep>(UIStep.class);
    }

    private boolean addNavigationEdgeToGraph(UIStep uiStep) {
        if (uiStep == null) {
            return false;
        }

        UIStep currentEdge = screenGraph.getEdge(uiStep.getSrcScreenId(), uiStep.getDstScreenId());
        if (currentEdge == null || !uiStep.checkIfSoftStep() || currentEdge.checkIfSoftStep()) {
            screenGraph.addVertex(uiStep.getSrcScreenId());
            screenGraph.addVertex(uiStep.getDstScreenId());
            Utils.printDebug("Adding src, dst, ui step: " + uiStep.getSrcScreenId() + ", " +  uiStep.getDstScreenId() + "," + uiStep.toString());
        }
        return screenGraph.addEdge(uiStep.getSrcScreenId(), uiStep.getDstScreenId(), uiStep);
    }


    public void updateNavigationGraph(UIScreen uiScreen) {
        if (uiScreen == null) {
            return;
        }
        for (UIStep uiStep: uiScreen.getNextStepToScreens().values()) {
            addNavigationEdgeToGraph(uiStep);
        }
        for (UIStep uiStep: uiScreen.getLastStepToCurrentScreen().values()) {
            addNavigationEdgeToGraph(uiStep);
        }
    }

    public DirectedGraph<String, UIStep> getScreenGraph() {
        return screenGraph;
    }
}
