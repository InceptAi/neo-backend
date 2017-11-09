package models;

import java.util.*;

public class UIStepTable {
    private HashMap<String, Set<UIStep>> uiStepMap;

    public UIStepTable() {
        uiStepMap = new HashMap<>();
    }

    public boolean addStep(String key, UIStep uiStep) {
        uiStepMap.putIfAbsent(key, new HashSet<>());
        Set<UIStep> uiStepList = uiStepMap.get(key) ;
        if (!uiStepList.contains(uiStep)) {
            uiStepList.add(uiStep);
            return true;
        }
        return false;
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
