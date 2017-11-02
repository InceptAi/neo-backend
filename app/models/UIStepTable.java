package models;

import java.util.*;

public class UIStepTable {
    HashMap<String, Set<UIStep>> uiStepMap;

    public UIStepTable() {
        uiStepMap = new HashMap<>();
    }

    public boolean addStep(String key, UIStep uiStep) {
        Set<UIStep> uiStepList = uiStepMap.get(key);
        if (uiStepList == null) {
            uiStepList = new HashSet<>();
        }
        boolean exists = false;
        for (UIStep stepInList: uiStepList) {
            if (stepInList.equals(uiStep)) {
                exists = true;
                break;
            }
        }
        if (exists) {
            return false;
        } else {
            uiStepList.add(uiStep);
            return true;
        }
    }

    public Set<UIStep> getSteps(String key) {
        return uiStepMap.get(key);
    }

    public Set<String> getKeys() {
        return uiStepMap.keySet();
    }

    public HashMap<String, Set<UIStep>> getUiStepMap() {
        return uiStepMap;
    }

    public void setUiStepMap(HashMap<String, Set<UIStep>> uiStepMap) {
        this.uiStepMap = uiStepMap;
    }


}
