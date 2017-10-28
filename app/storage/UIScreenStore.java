package storage;

import models.UIElement;
import models.UIPath;
import models.UIScreen;
import models.UIStep;
import nlu.SimpleTextInterpreter;
import nlu.TextInterpreter;
import scala.concurrent.java8.FuturesConvertersImpl;
import util.Utils;

import java.util.*;

public class UIScreenStore {
    private static UIScreenStore instance;
    private Map<String, UIScreen> uiScreenMap;
    private Map<String, Set<String>> packageNameToScreenIdMap;
    private TextInterpreter textInterpreter;

    private UIScreenStore() {
        uiScreenMap = new HashMap<>();
        packageNameToScreenIdMap = new HashMap<>();
        textInterpreter = new SimpleTextInterpreter();
    }

    public static UIScreenStore getInstance() {
        if (instance == null) {
            instance = new UIScreenStore();
        }
        return instance;
    }

    public UIScreen addScreen(UIScreen uiScreen) {
        String screenId = uiScreen.getId();
        UIScreen currentScreen = uiScreenMap.get(screenId);
        if (currentScreen == null) {
            currentScreen = uiScreen;
            uiScreenMap.put(screenId, uiScreen);
        } else {
            boolean mergeResult = currentScreen.mergeScreen(uiScreen);
            if (!mergeResult) {
                System.err.print("Screen merge failed for screen getId: " + screenId);
            }
        }
        addScreenToPackageMap(uiScreen);
        return currentScreen;
    }

    public UIScreen getScreen(String packageName, String title, String deviceInfo) {
        if (Utils.nullOrEmpty(deviceInfo)) {
            return getScreen(packageName, title);
        }
        return uiScreenMap.get(UIScreen.getScreenId(packageName, title, deviceInfo));
    }

    private UIScreen getScreen(String packageName, String title) {
        Set<UIScreen> uiScreens = new HashSet<>(uiScreenMap.values());
        for (UIScreen uiScreen: uiScreens) {
            if (uiScreen.getPackageName().equalsIgnoreCase(packageName) && uiScreen.getTitle().equalsIgnoreCase(title)) {
                return uiScreen;
            }
        }
        return null;
    }

    public UIScreen getScreen(String id) {
        return uiScreenMap.get(id);
    }


    public Set<UIScreen> getAllScreens() {
        return new HashSet<>(uiScreenMap.values());
    }

    public UIScreen updateScreen(UIScreen uiScreen) {
        String id = uiScreen.getId();
        if (uiScreenMap.containsKey(id)) {
            uiScreenMap.put(id, uiScreen);
            return uiScreen;
        }
        return null;
    }

    public boolean deleteScreen(String id) {
        return uiScreenMap.remove(id) != null;
    }

    private void addScreenToPackageMap(UIScreen uiScreen) {
        Set<String> screenList = packageNameToScreenIdMap.get(uiScreen.getPackageName());
        if (screenList == null) {
            screenList = new HashSet<>();
            screenList.add(uiScreen.getId());
            packageNameToScreenIdMap.put(uiScreen.getPackageName(), screenList);
        } else if (!screenList.contains(uiScreen.getId())) {
            screenList.add(uiScreen.getId());
        }
    }

    public UIScreen findTopMatchingScreenIdByKeyword(String keyWords, String packageName) {
        final double MAX_GAP_FOR_CONFIDENCE = 0.2;
        Map<String, Double> confidenceScores = findScreenIdsByKeywords(keyWords, packageName, MAX_GAP_FOR_CONFIDENCE);
        if (confidenceScores.size() != 1) {
            return null;
        }
        String bestMatchingScreenId = (String)confidenceScores.keySet().toArray()[0];
        return uiScreenMap.get(bestMatchingScreenId);
    }

    private LinkedHashMap<String, Double> findScreenIdsByKeywords(String keyWords, String packageName, double maxGapFromBest) {
        LinkedHashMap<String, Double> screenIdsToReturn = new LinkedHashMap<>();
        HashMap<String, Double> screenIdToConfidenceHashMap = new HashMap<>();
        //Search in all screens for this match in a given packageName
        Set<String> screenIdSet = packageNameToScreenIdMap.get(packageName);
        if (screenIdSet == null) {
            return screenIdsToReturn;
        }

        double bestMetric = 0;
        for (String screenId: screenIdSet) {
            UIScreen uiScreen = uiScreenMap.get(screenId);
            double matchMetric = textInterpreter.getMatchMetric(keyWords, uiScreen.getTitle());
            double navigationMetric = findBestNavigationStepMetricByKeyWords(keyWords, uiScreen);
            double totalMetric = navigationMetric >= 0 ? (navigationMetric + matchMetric) / 2 : matchMetric;
            if (bestMetric < totalMetric) {
                bestMetric = totalMetric;
            }
            screenIdToConfidenceHashMap.put(screenId, totalMetric);
        }

        //Search in all screens navigational elements for this match
        for (HashMap.Entry<String, Double> entry : screenIdToConfidenceHashMap.entrySet()) {
            if (entry.getValue() > bestMetric - maxGapFromBest && entry.getValue() > 0) {
                screenIdsToReturn.put(entry.getKey(), entry.getValue());
            }
        }

        return screenIdsToReturn;
    }

    private double findBestNavigationStepMetricByKeyWords(String keyWords, UIScreen uiScreen) {
        if (uiScreen == null || keyWords == null) {
            return -1;
        }
        double bestMatchMetric = 0;
        for (UIPath uiPath: uiScreen.getUiPaths()) {
            UIStep lastStep = uiPath.getLastStepInPath();
            if (lastStep == null || lastStep.getUiElementId() == null) {
                continue;
            }
            UIScreen srcScreen = uiScreenMap.get(lastStep.getSrcScreenId());
            if (srcScreen == null) {
                //SrcScreen can't be null
                Utils.printDebug("We should never come here");
            }
            UIElement lastUIElement = srcScreen.getUiElements().get(lastStep.getUiElementId());
            double matchMetric = textInterpreter.getMatchMetric(keyWords, lastUIElement.getAllText());
            if (matchMetric > bestMatchMetric) {
                bestMatchMetric = matchMetric;
            }
        }
        return bestMatchMetric;
    }
}
