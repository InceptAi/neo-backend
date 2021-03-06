package models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UIPath {
    private static final String HARD_PATH = "HARD_PATH";
    private static final String SOFT_PATH = "SOFT_PATH";
    private static final String UNDEFINED = "UNDEFINED";

    private List<UIStep> uiSteps;
    private String semanticActionType = SemanticAction.UNDEFINED;
    private String pathType = UNDEFINED;

    private UIPath(UIPath uiPath) {
        this.uiSteps = new ArrayList<>();
        for (UIStep uiStep: uiPath.uiSteps) {
            uiSteps.add(UIStep.copyStep(uiStep));
        }
        this.semanticActionType = uiPath.semanticActionType;
        this.pathType = uiPath.pathType;
    }

    //Factory
    private static UIPath copyPath(UIPath uiPath){
        if (uiPath == null) {
            return null;
        }
        return new UIPath(uiPath);
    }

    public UIPath() {
        this.semanticActionType = SemanticAction.UNDEFINED;
        this.uiSteps = new ArrayList<>();
        this.pathType = UNDEFINED;
    }

    public UIPath(List<UIStep> uiSteps) {
        this.semanticActionType = SemanticAction.UNDEFINED;
        this.uiSteps = uiSteps;
        this.pathType = UNDEFINED;
    }

    public UIPath(String semanticActionType, UIStep uiStep) {
        this.uiSteps = new ArrayList<>();
        this.uiSteps.add(uiStep);
        this.semanticActionType = semanticActionType;
        if (uiStep.checkIfSoftStep()) {
            this.pathType = SOFT_PATH;
        } else {
            this.pathType = HARD_PATH;
        }
    }

    public UIPath(String semanticActionType) {
        this.semanticActionType = semanticActionType;
        this.uiSteps = new ArrayList<>();
        this.pathType = UNDEFINED;
    }

    private UIPath(String semanticActionType, String uiPathType) {
        this.semanticActionType = semanticActionType;
        this.uiSteps = new ArrayList<>();
        this.pathType = uiPathType;
    }

    public String getPathType() {
        return pathType;
    }

    public void setPathType(String pathType) {
        this.pathType = pathType;
    }

    public List<UIStep> getUiSteps() {
        return uiSteps;
    }

    public UIStep getLastStepInPath() {
        if (uiSteps.isEmpty()) {
            return null;
        }
        return uiSteps.get(uiSteps.size() - 1);
    }

    public String getSemanticActionType() {
        return semanticActionType;
    }

    public static UIPath createNewPath(UIPath uiPath, UIStep uiStep) {
        if (uiPath == null || uiStep == null) {
            return null;
        }
        UIPath newUIPath = UIPath.copyPath(uiPath);
        for (UIStep currentStep: newUIPath.getUiSteps()) {
            if (currentStep.equals(uiStep)) {
                //This new step already exists in the path, so can't add again. invalid path.
                return null;
            }
            //TODO -- think about this, I think we need this path for reverse lookup
//            if (currentStep.getSrcScreenId().equals(uiStep.getDstScreenId())) {
//                //New UI steps dst already exists as a src in the path, so invalid path (X, Y), (Y, Z), (Z, X) -- avoid paths like this
//                return null;
//            } 
        }
        newUIPath.uiSteps.add(UIStep.copyStep(uiStep));
        if (uiStep.checkIfSoftStep()) {
            newUIPath.pathType = SOFT_PATH;
        } else {
            newUIPath.pathType = HARD_PATH;
        }
        return newUIPath;
    }

    public String getId() {
        return String.valueOf(hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UIPath)) return false;

        UIPath uiPath = (UIPath) o;

        return uiSteps.equals(uiPath.uiSteps);
    }

    @Override
    public int hashCode() {
        return uiSteps.hashCode();
    }


    @Override
    public String toString() {
        return "UIPath{" +
                "uiSteps=" + uiSteps +
                ", semanticActionType=" + semanticActionType +
                '}';
    }

    private int length() {
        return uiSteps.size();
    }


    public static UIPath findSubPath(UIPath uiPath, String srcScreenId) {
        if (uiPath == null) {
            return null;
        }
        int pathLength = uiPath.length();
        int srcIndex = -1;
        for (int stepIndex = 0; stepIndex < pathLength; stepIndex++) {
            UIStep uiStep = uiPath.uiSteps.get(stepIndex);
            if (uiStep.getSrcScreenId().equalsIgnoreCase(srcScreenId)) {
                srcIndex = stepIndex;
            }
        }
        if (srcIndex == -1) {
            //Didn't find src, return null
            return null;
        }
        UIPath subPath = new UIPath(SemanticAction.NAVIGATE, uiPath.pathType);
        for (int stepIndex = srcIndex; stepIndex < pathLength; stepIndex++) {
            UIStep uiStep = uiPath.uiSteps.get(stepIndex);
            subPath.uiSteps.add(UIStep.copyStep(uiStep));
        }
        return subPath;
    }

    public static class UIPathComparator implements Comparator<UIPath> {
        @Override
        public int compare(UIPath path1, UIPath path2) {
            //0 if equal
            //-1 if nodeInfo1 is smaller
            //1 if nodeInfo2 is smaller
            if (path1 == null && path2 == null) {
                return 0;
            } else if (path1 == null) {
                return 1;
            } else if (path2 == null) {
                return -1;
            } else {
                if (path1.equals(path2)) {
                    return 0;
                } else if (path1.length() < path2.length()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }
}
