package services;

import models.UIScreen;
import nlu.SimpleTextInterpreter;
import nlu.TextInterpreter;
import storage.NavigationGraphStore;
import storage.SemanticActionStore;
import util.ResultFunction;
import util.Utils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class UIScreenManager {
    private Map<String, UIScreen> uiScreenMap;
    private Map<String, Set<String>> packageNameToScreenIdMap;
    private Map<String, Set<String>> screenTitleToScreenIdMap;
    private TextInterpreter textInterpreter;
    private DatabaseBackend databaseBackend;
    private NavigationGraphStore navigationGraphStore;
    private SemanticActionStore semanticActionStore;
    private List<UIScreen> uiScreensToDelete;

    @Inject
    public UIScreenManager(DatabaseBackend databaseBackend,
                           NavigationGraphStore navigationGraphStore,
                           SemanticActionStore semanticActionStore) {
        uiScreenMap = new HashMap<>();
        packageNameToScreenIdMap = new HashMap<>();
        screenTitleToScreenIdMap = new HashMap<>();
        textInterpreter = new SimpleTextInterpreter();
        this.databaseBackend = databaseBackend;
        this.navigationGraphStore = navigationGraphStore;
        this.semanticActionStore = semanticActionStore;
        this.uiScreensToDelete = new ArrayList<>();
        //Start fetch of screens from database and update the hash map here
        startLoading();
    }

    private void startLoading() {
        databaseBackend.loadAllScreens((ResultFunction<List<UIScreen>>) uiScreenList -> {
            UIScreenManager.this.updateScreenMap(uiScreenList);
            return null;
        });
    }

    public void writeScreenToBackend(UIScreen uiScreen) {
        List<UIScreen> screenList = Collections.singletonList(uiScreen);
        databaseBackend.saveScreensAsync(screenList);
    }

    public void commitScreensToBackendAsync() {
        List<UIScreen> screenList = new ArrayList<>(uiScreenMap.values());
        databaseBackend.saveScreensAsync(screenList);
        databaseBackend.deleteScreensAsync(uiScreensToDelete);
    }

    private void updateScreenMap(List<UIScreen> uiScreenList) {
        if (uiScreenList == null) {
            return;
        }
        for (UIScreen uiScreen: uiScreenList) {
            addScreenAdvanced(uiScreen);
        }
    }

    public UIScreen addScreenAdvanced(UIScreen uiScreen) {
        String screenId = uiScreen.getId();
            UIScreen screenInMap = uiScreenMap.get(screenId);
        if (screenInMap == null) {
            screenInMap = uiScreen;
            uiScreenMap.put(screenId, uiScreen);
        } else {
            boolean mergeResult = screenInMap.mergeScreen(uiScreen, true);
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
            screenInMap.mergeScreen(emptySubtitleScreen, false);
            uiScreenMap.remove(emptySubtitleScreen.getId());
            uiScreensToDelete.add(emptySubtitleScreen);
        }
        addScreenToPackageMap(screenInMap);
        addScreenToTitleMap(screenInMap);
        navigationGraphStore.updateNavigationGraph(screenInMap);
        semanticActionStore.updateSemanticActionStore(screenInMap);
        return screenInMap;
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
}
