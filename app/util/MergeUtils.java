package util;

import models.*;

import java.util.*;

public class MergeUtils {

    public static UIStepTable mergeUITables(UIStepTable uiStepTable1, UIStepTable uiStepTable2) {

        if (uiStepTable1 == null && uiStepTable2 == null) {
            return null;
        } else if (uiStepTable1 == null) {
            return uiStepTable2;
        } else if (uiStepTable2 == null) {
            return uiStepTable1;
        }

        UIStepTable mergedTable = new UIStepTable();
        mergedTable.getUiStepMap().putAll(uiStepTable1.getUiStepMap());
        Set<String> keys2 = uiStepTable2.getKeys();
        for (String key: keys2) {
            Set<UIStep> uiStepSet2 = uiStepTable2.getSteps(key);
            Set<UIStep> mergedSet = mergedTable.getSteps(key);
            if (mergedSet == null) {
                mergedSet = new HashSet<>();
                mergedTable.getUiStepMap().put(key, mergedSet);
            }
            mergedSet.addAll(uiStepSet2);
        }
        return mergedTable;
    }

    public static List<UIPath> mergeUIPaths(List<UIPath> uiPathListOld, List<UIPath> uiPathListUpdated) {
        HashMap<String, UIPath> uiPathMapOld = new HashMap<>();
        HashMap<String, UIPath> uiPathMapUpdated = new HashMap<>();
        for (UIPath uiPath: uiPathListOld) {
            uiPathMapOld.put(uiPath.getId(), uiPath);
        }
        for (UIPath uiPath: uiPathListUpdated) {
            uiPathMapUpdated.put(uiPath.getId(), uiPath);
        }
        HashMap<String, UIPath> mergedMap = new HashMap<>();
        mergedMap.putAll(uiPathMapOld);
        mergedMap.putAll(uiPathMapUpdated);
        return new ArrayList<>(mergedMap.values());
    }

    public static HashMap<String, UIElement> mergeUIElements(HashMap<String, UIElement> uiElementHashMapOld,
                                                             HashMap<String, UIElement> uiElementHashMapUpdated) {
        HashMap<String, UIElement> mergedHashMap = new HashMap<>();
        mergedHashMap.putAll(uiElementHashMapOld);
        mergedHashMap.putAll(uiElementHashMapUpdated);
        return mergedHashMap;
    }

    public static List<UIPath> getUIPathBasedOnLastScreenPath(List<UIPath> lastScreenUIPaths, UIStep uiStep) {
        List<UIPath> uiPathList = new ArrayList<>();
        if (lastScreenUIPaths == null || lastScreenUIPaths.isEmpty()) {
            uiPathList.add(new UIPath(SemanticActionType.NAVIGATE, uiStep));
        } else {
            for (UIPath uiPath: lastScreenUIPaths) {
                UIPath updatedPath = UIPath.createNewPath(uiPath, uiStep);
                if (updatedPath != null) {
                    uiPathList.add(updatedPath);
                }
            }
        }
        return uiPathList;
    }


    public static List<String> getDifferingUIElementIdsInCurrentScreen(Set<String> currentElementIds, Set<String> otherElementIds) {
        List<String> differingIds = new ArrayList<>();
        for (String currentId: currentElementIds) {
            if (!otherElementIds.contains(currentId)) {
                differingIds.add(currentId);
            }
        }
        return differingIds;
    }

}