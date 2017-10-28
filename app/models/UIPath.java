package models;

import java.util.ArrayList;
import java.util.List;

public class UIPath {
    public enum UIPathType {
        HARD_PATH("HARD_PATH"),
        SOFT_PATH("SOFT_PATH"),
        UNDEFINED("UNDEFINED");

        private String id;

        UIPathType(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    private List<UIStep> uiSteps;
    private SemanticActionType semanticActionType = SemanticActionType.UNDEFINED;
    private UIPathType pathType = UIPathType.UNDEFINED;

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

    public UIPath(SemanticActionType semanticActionType, UIStep uiStep) {
        this.uiSteps = new ArrayList<>();
        this.uiSteps.add(uiStep);
        this.semanticActionType = semanticActionType;
        if (uiStep.isSoftStep()) {
            this.pathType = UIPathType.SOFT_PATH;
        } else {
            this.pathType = UIPathType.HARD_PATH;
        }
    }

    public UIPath(SemanticActionType semanticActionType) {
        this.semanticActionType = semanticActionType;
        this.uiSteps = new ArrayList<>();
        this.pathType = UIPathType.UNDEFINED;
    }

    public UIPath(SemanticActionType semanticActionType, UIPathType uiPathType) {
        this.semanticActionType = semanticActionType;
        this.uiSteps = new ArrayList<>();
        this.pathType = uiPathType;
    }

    public UIPathType getPathType() {
        return pathType;
    }

    public void setPathType(UIPathType pathType) {
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

    public SemanticActionType getSemanticActionType() {
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
        if (uiStep.isSoftStep()) {
            newUIPath.pathType = UIPathType.SOFT_PATH;
        } else {
            newUIPath.pathType = UIPathType.HARD_PATH;
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

    public int length() {
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
        UIPath subPath = new UIPath(SemanticActionType.NAVIGATE, uiPath.pathType);
        for (int stepIndex = srcIndex; stepIndex < pathLength; stepIndex++) {
            UIStep uiStep = uiPath.uiSteps.get(stepIndex);
            subPath.uiSteps.add(UIStep.copyStep(uiStep));
        }
        return subPath;
    }
}
