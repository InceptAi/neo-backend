package storage;

import models.UIScreen;
import nlu.SimpleTextInterpreter;
import nlu.TextInterpreter;
import services.DatabaseBackend;
import services.FirestoreBackend;
import util.Utils;

import java.util.*;

public class UIScreenStore {
    private static final boolean WRITE_EVERY_SCREEN_TO_BACKEND = true;
    private static UIScreenStore instance;
    private Map<String, UIScreen> uiScreenMap;
    private Map<String, Set<String>> packageNameToScreenIdMap;
    private Map<String, Set<String>> screenTitleToScreenIdMap;
    private TextInterpreter textInterpreter;
    private DatabaseBackend databaseBackend;

    private UIScreenStore(DatabaseBackend databaseBackend) {
        uiScreenMap = new HashMap<>();
        packageNameToScreenIdMap = new HashMap<>();
        screenTitleToScreenIdMap = new HashMap<>();
        textInterpreter = new SimpleTextInterpreter();
        this.databaseBackend = databaseBackend;
    }

    public static UIScreenStore getInstance() {
        if (instance == null) {
            instance = new UIScreenStore(new FirestoreBackend());
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
            boolean mergeResult = currentScreen.mergeScreen(uiScreen, true);
            if (!mergeResult) {
                System.err.print("Screen merge failed for screen getId: " + screenId);
            }
        }
        addScreenToPackageMap(uiScreen);
        addScreenToTitleMap(uiScreen);
        if (WRITE_EVERY_SCREEN_TO_BACKEND) {
            writeScreenToBackend(uiScreen);
        }
        return currentScreen;
    }

    public void writeScreenToBackend(UIScreen uiScreen) {
        List<UIScreen> screenList = Arrays.asList(uiScreen);
        databaseBackend.saveScreens(screenList);
    }

    public void writeAllScreensToBackend() {
        List<UIScreen> screenList = new ArrayList<>(uiScreenMap.values());
        databaseBackend.saveScreens(screenList);
    }

    public UIScreen addScreenAdvanced(UIScreen uiScreen) {
        String screenId = uiScreen.getId();
        UIScreen currentScreen = uiScreenMap.get(screenId);
        if (currentScreen == null) {
            currentScreen = uiScreen;
            uiScreenMap.put(screenId, uiScreen);
        } else {
            boolean mergeResult = currentScreen.mergeScreen(uiScreen, true);
            if (!mergeResult) {
                System.err.print("Screen merge failed for screen getId: " + screenId);
            }
        }

        //Check if a screen exists with same title and empty subtitle -- remove it and merge
        String screenIdWithEmptySubtitle = UIScreen.getScreenId(
                uiScreen.getPackageName(),
                uiScreen.getTitle(),
                Utils.EMPTY_STRING,
                uiScreen.getScreenType(),
                uiScreen.getDeviceInfo().toString());
        UIScreen emptySubtitleScreen = uiScreenMap.get(screenIdWithEmptySubtitle);
        if (emptySubtitleScreen != null) {
            //Remove the empty subtitle screen
            currentScreen.mergeScreen(emptySubtitleScreen, false);
        }
        addScreenToPackageMap(uiScreen);
        addScreenToTitleMap(uiScreen);
        if (WRITE_EVERY_SCREEN_TO_BACKEND) {
            writeScreenToBackend(uiScreen);
        }
        return currentScreen;
    }

    public UIScreen getScreen(String packageName, String title, String subTitle, String screenType, String deviceInfo) {
        if (Utils.nullOrEmpty(deviceInfo)) {
            return getScreen(packageName, title);
        }
        Set<String> screenIdListForTitleAndPackage = screenTitleToScreenIdMap.get(getKeyForTitleMap(title, packageName, screenType));
        if (screenIdListForTitleAndPackage != null) {
            ArrayList<String> screenIdList = new ArrayList<>(screenIdListForTitleAndPackage);
            if (screenIdList.size() == 1) {
                //One match only -- bingo
                return uiScreenMap.get(screenIdList.get(0));
            } else {
                return uiScreenMap.get(UIScreen.getScreenId(packageName, title, subTitle, screenType, deviceInfo));
            }
        }
        return null;
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

    public Set<UIScreen> getAllScreensFromDatabase() {
        return new HashSet<>(databaseBackend.getAllScreens());
    }

    public Set<UIScreen> getAllScreensWithoutPaths() {
        HashSet<UIScreen> screenHashSet = new HashSet<>();
        for (UIScreen uiScreen: uiScreenMap.values()) {
            if (uiScreen.getLastStepToCurrentScreen().size() == 0) {
                screenHashSet.add(uiScreen);
            }
        }
        return screenHashSet;
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

    private String getKeyForTitleMap(String title, String packageName, String screenType) {
        String key = title + " " + packageName + " " + screenType;
        return Utils.sanitizeText(key);
    }

    private void addScreenToTitleMap(UIScreen uiScreen) {
        String keyForTitleMap = getKeyForTitleMap(uiScreen.getTitle(), uiScreen.getPackageName(), uiScreen.getScreenType());
        Set<String> screenList = screenTitleToScreenIdMap.get(keyForTitleMap);
        if (screenList == null) {
            screenList = new HashSet<>();
            screenList.add(uiScreen.getId());
            screenTitleToScreenIdMap.put(keyForTitleMap, screenList);
        } else if (!screenList.contains(uiScreen.getId())) {
            screenList.add(uiScreen.getId());
        }
    }

    public UIScreen findTopMatchingScreenIdByKeywordAndScreenType(String keyWords, String packageName, String screenType) {
        final double MAX_GAP_FOR_CONFIDENCE = 0.2;
        Map<String, Double> confidenceScores = findScreenIdsByKeywords(keyWords, packageName, MAX_GAP_FOR_CONFIDENCE);
//        if (confidenceScores.size() != 1) {
//            return null;
//        }
        //Search in all screens navigational elements for this match
        for (HashMap.Entry<String, Double> entry : confidenceScores.entrySet()) {
            UIScreen matchingScreen = uiScreenMap.get(entry.getKey());
            if (matchingScreen != null && matchingScreen.getScreenType().equalsIgnoreCase(screenType)) {
                return matchingScreen;
            }
        }
        return null;
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
            double totalMetric = textInterpreter.getMatchMetric(keyWords, uiScreen.getTitle());
            //double matchMetric = textInterpreter.getMatchMetric(keyWords, uiScreen.getTitle());
            //double navigationMetric = findBestNavigationStepMetricByKeyWords(keyWords, uiScreen);
            //double totalMetric = navigationMetric >= 0 ? (navigationMetric + matchMetric) / 2 : matchMetric;
            if (bestMetric < totalMetric) {
                bestMetric = totalMetric;
            }
            screenIdToConfidenceHashMap.put(screenId, totalMetric);
        }

        //Search in all screens navigational elements for this match
        Map<String, Double> sortedConfidenceHashMap = Utils.sortHashMapByValueDescending(screenIdToConfidenceHashMap);
        for (HashMap.Entry<String, Double> entry : sortedConfidenceHashMap.entrySet()) {
            if (entry.getValue() > bestMetric - maxGapFromBest && entry.getValue() > 0) {
                screenIdsToReturn.put(entry.getKey(), entry.getValue());
            }
        }

        return screenIdsToReturn;
    }

    /*
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
            if (srcScreen == null || srcScreen.getUiElements() == null) {
                //SrcScreen can't be null
                Utils.printDebug("We should never come here");
            }
            UIScreen.UIElementTuple lastUIElementTuple = srcScreen.findElementAndTopLevelParentById(lastStep.getUiElementId());
            //UIElement lastUIElement = srcScreen.getUiElements().get(lastStep.getUiElementId());
            if (lastUIElementTuple == null || lastUIElementTuple.getUiElement() != null) {
                Utils.printDebug("We should never come here");
            }
            assert lastUIElementTuple != null && lastUIElementTuple.getUiElement() != null;
            double matchMetric = textInterpreter.getMatchMetric(keyWords, lastUIElementTuple.getUiElement().getAllText());
            if (matchMetric > bestMatchMetric) {
                bestMatchMetric = matchMetric;
            }
        }
        return bestMatchMetric;
    }
    */
}
