package models;

import nlu.SimpleTextInterpreter;
import util.MergeUtils;
import util.Utils;


import java.util.*;

import static config.BackendConfiguration.MAX_SCORE_GAP_FOR_GROUPING_TOP_RESULTS_IN_FUZZY_ELEMENT_SEARCH;
import static config.BackendConfiguration.MIN_MATCH_PERCENTAGE_FOR_FUZZY_ELEMENT_SEARCH_IN_SCREENS;

public class UIScreen {
    private final String id;
    private final String screenType;
    private final String packageName;
    private final String title;
    private final String subTitle;
    private final HashMap<String, String> deviceInfo;
    private String parentScreenId = Utils.EMPTY_STRING;
    private HashMap<String, UIStep> nextStepToScreens = new HashMap<>();
    private HashMap<String, UIStep> lastStepToCurrentScreen = new HashMap<>();
    private HashMap<String, UIElement> uiElements = new HashMap<>();
    private HashMap<String, UIScreen> childScreens = new HashMap<>();
    private HashMap<String, SemanticAction> semanticActions = new HashMap<>();
    private long lastUpdatedAtMs = 0;

    public HashMap<String, UIScreen> getChildScreens() {
        return childScreens;
    }

    public String getParentScreenId() {
        return parentScreenId;
    }

    public void setParentScreenId(String parentScreenId) {
        this.parentScreenId = parentScreenId;
    }


    public void setNextStepToScreens(HashMap<String, UIStep> nextStepToScreens) {
        this.nextStepToScreens = nextStepToScreens;
    }

    public void setLastStepToCurrentScreen(HashMap<String, UIStep> lastStepToCurrentScreen) {
        this.lastStepToCurrentScreen = lastStepToCurrentScreen;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getScreenType() {
        return screenType;
    }

    public String getId() {
        //return getScreenId(packageName, title, subTitle, screenType, deviceInfo.toString());
        return id;
    }

    public long getLastUpdatedAtMs() {
        return lastUpdatedAtMs;
    }

    public void setLastUpdatedAtMs(long lastUpdatedAtMs) {
        this.lastUpdatedAtMs = lastUpdatedAtMs;
    }

    public UIScreen() {
        this.id = Utils.EMPTY_STRING;
        this.screenType = Utils.EMPTY_STRING;
        this.packageName = Utils.EMPTY_STRING;
        this.title = Utils.EMPTY_STRING;
        this.subTitle = Utils.EMPTY_STRING;
        this.deviceInfo = new HashMap<>();
        this.lastUpdatedAtMs = 0;
    }

    public UIScreen(String id, String parentScreenId, String screenType, String packageName,
                    String title, String subTitle, HashMap<String, UIStep> nextStepToScreens,
                    HashMap<String, UIStep> lastStepToCurrentScreen, HashMap<String, UIElement> uiElements,
                    HashMap<String, String> deviceInfo, HashMap<String, UIScreen> childScreens,
                    HashMap<String, SemanticAction> semanticActions, long lastUpdatedAtMs) {
        this.parentScreenId = parentScreenId;
        this.screenType = screenType;
        this.packageName = packageName;
        this.title = title;
        this.subTitle = subTitle;
        this.nextStepToScreens = nextStepToScreens;
        this.lastStepToCurrentScreen = lastStepToCurrentScreen;
        this.uiElements = uiElements;
        this.deviceInfo = deviceInfo;
        this.childScreens = childScreens;
        this.semanticActions = semanticActions;
        this.id = getScreenId(packageName, title, subTitle, screenType, deviceInfo.toString());
        this.lastUpdatedAtMs = lastUpdatedAtMs;
    }

    public UIScreen(String title, String subTitle, String packageName, String screenType, HashMap<String, String> deviceInfo) {
        this.screenType = screenType;
        this.packageName = packageName;
        this.title = title;
        this.subTitle = subTitle;
        this.deviceInfo = deviceInfo;
        this.id = getScreenId(packageName, title, subTitle, screenType, deviceInfo.toString());
        this.lastUpdatedAtMs = System.currentTimeMillis();
        //Other initializations
        this.uiElements = new HashMap<>();
        this.nextStepToScreens = new HashMap<>();
        this.lastStepToCurrentScreen = new HashMap<>();
        this.childScreens = new HashMap<>();
        this.semanticActions = new HashMap<>();
    }

    public void setUiElements(HashMap<String, UIElement> uiElements) {
        this.uiElements = uiElements;
    }

    public void setChildScreens(HashMap<String, UIScreen> childScreens) {
        this.childScreens = childScreens;
    }

    public HashMap<String, SemanticAction> getSemanticActions() {
        return semanticActions;
    }

    public void setSemanticActions(HashMap<String, SemanticAction> semanticActions) {
        this.semanticActions = semanticActions;
    }

    public void add(UIElement uiElement) {
        this.uiElements.put(uiElement.id(), uiElement);
    }

    public void addUIStepForDestinationScreen(String destId, UIStep uiStep) {
        nextStepToScreens.put(destId, uiStep);
    }

    public void addChildScreen(UIScreen childScreen) {
        childScreens.put(childScreen.getId(), childScreen);
    }

    public void addUIStepToCurrentScreen(UIStep uiStep) {
        lastStepToCurrentScreen.put(getId(), uiStep);
    }

    public HashMap<String, UIStep> getNextStepToScreens() {
        return nextStepToScreens;
    }

    public HashMap<String, UIStep> getLastStepToCurrentScreen() {
        return lastStepToCurrentScreen;
    }

    public HashMap<String, UIElement> getUiElements() {
        return uiElements;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getTitle() {
        return title;
    }

    public HashMap<String, String> getDeviceInfo() {
        return deviceInfo;
    }

    public HashMap<String, List<UIElement>> findElementsInScreenHierarchical(String className,
                                                                             String packageName,
                                                                             String text,
                                                                             boolean needClickable,
                                                                             boolean fuzzySearch,
                                                                             boolean prioritizeTitleText) {
        //Look in the main screen first
        HashMap<String, List<UIElement>> stringListHashMap = new HashMap<>();
        List<UIElement> uiElementListMainScreen = new ArrayList<>();
        if (fuzzySearch) {
            uiElementListMainScreen.addAll(findElementsInScreenFuzzyNested(className, packageName, text, needClickable));
        } else {
            uiElementListMainScreen.addAll(findElementsInScreenStrictNested(className, packageName, text, needClickable));
        }

        if (uiElementListMainScreen.isEmpty()) { //Only find in subScreens if nothing in main screen
            //Find in subScreens
            final boolean ENABLE_FUZZY_SEARCH_IN_CHILD_SCREENS = true;
            for (UIScreen uiScreen: childScreens.values()) {
                stringListHashMap.putAll(uiScreen.findElementsInScreenHierarchical(
                        className,
                        packageName,
                        text,
                        needClickable,
                        ENABLE_FUZZY_SEARCH_IN_CHILD_SCREENS,
                        false));
            }
        } else {
            stringListHashMap.put(getId(), uiElementListMainScreen);
        }
        if (prioritizeTitleText) {
            //Shorten the list based on whether match is for title text view
            for (String screenId: stringListHashMap.keySet()) {
                List<UIElement> currentList =  stringListHashMap.get(screenId);
                if (currentList.size() > 1) {
                    List<UIElement> prioritizedList = shortenListBasedOnTitleText(text, currentList);
                    currentList.clear();
                    currentList.addAll(prioritizedList);
                }
            }
        }
        return stringListHashMap;
    }

    private List<UIElement> shortenListBasedOnTitleText(String textToMatch,
                                                        List<UIElement> candidateList) {
        final double MAX_GAP_IN_MATCHING_METRIC = 0.2;
        final double MIN_MATCHING_METRIC = 0;
        SimpleTextInterpreter simpleTextInterpreter = new SimpleTextInterpreter(MIN_MATCHING_METRIC);
        double bestMatchMetric = 0;
        HashMap<String, Double> elementScoreHashMap = new HashMap<>();
        HashMap<String, UIElement> candidateElementHashMap = new HashMap<>();
        for (UIElement candidateElement : candidateList) {
            //For each element, find the first TextView with title
            UIElement titleTextViewElement = UIElement.findFirstTitleTextViewInElement(candidateElement);
            if (titleTextViewElement != null) {
                double matchMetric = simpleTextInterpreter.getMatchMetric(textToMatch, titleTextViewElement.getPrimaryText());
                elementScoreHashMap.put(candidateElement.getId(), matchMetric);
                candidateElementHashMap.put(candidateElement.getId(), candidateElement);
                if (matchMetric > bestMatchMetric) {
                    bestMatchMetric = matchMetric;
                }
            }
        }

        Map<String, Double> sortedElementScoreHashMap = Utils.sortHashMapByValueDescending(elementScoreHashMap);
        List<UIElement> shortenedList = new ArrayList<>();
        for (HashMap.Entry<String, Double> entry : sortedElementScoreHashMap.entrySet()) {
            if (entry.getValue() > 0 && entry.getValue() > bestMatchMetric - MAX_GAP_IN_MATCHING_METRIC) {
                shortenedList.add(candidateElementHashMap.get(entry.getKey()));
            }
        }
        return shortenedList;
    }

    public List<UIElement> findElementsInScreen(String className, String packageName,
                                                String text, boolean needClickable,
                                                boolean fuzzySearch) {
        if (fuzzySearch) {
            return findElementsInScreenFuzzyNested(className, packageName, text, needClickable);
        } else {
            return findElementsInScreenStrictNested(className, packageName, text, needClickable);
        }
    }


    private List<UIElement> findElementsInScreenFuzzyNested(String className, String packageName, String text, boolean needClickable) {
        HashMap<String, UIElement> uiElementListToReturn = new HashMap<>();
        HashMap<String, Double> matchingUIElementScores = new HashMap<>();
        HashMap<String, UIElement> matchingUIElements = new HashMap<>();
        SimpleTextInterpreter textInterpreter = new SimpleTextInterpreter(MIN_MATCH_PERCENTAGE_FOR_FUZZY_ELEMENT_SEARCH_IN_SCREENS);
        double bestMatchingScore = 0;
        for (UIElement uiElement: uiElements.values()) {
            //Do fuzzy search
            UIElement.ElementScore matchingElementScore = uiElement.findElementByTextFuzzy(text, textInterpreter, needClickable);
            if (matchingElementScore != null) {
                matchingUIElementScores.put(matchingElementScore.getElement().id(), matchingElementScore.getScore());
                matchingUIElements.put(matchingElementScore.getElement().id(), matchingElementScore.getElement());
                if (matchingElementScore.getScore() > bestMatchingScore) {
                    bestMatchingScore = matchingElementScore.getScore();
                }
            }
        }

        //Sort the hash map based on match metric
        Map<String, Double> sortedMetricMap = Utils.sortHashMapByValueDescending(matchingUIElementScores);
        for (HashMap.Entry<String, Double> entry : sortedMetricMap.entrySet()) {
            if (entry.getValue() > bestMatchingScore - MAX_SCORE_GAP_FOR_GROUPING_TOP_RESULTS_IN_FUZZY_ELEMENT_SEARCH) {
                uiElementListToReturn.put(entry.getKey(), matchingUIElements.get(entry.getKey()));
            }
        }
        return new ArrayList<>(uiElementListToReturn.values());
    }

    private List<UIElement> findElementsInScreenStrictNested(String className, String packageName,
                                                             String text, boolean needClickable) {
        Utils.printDebug("In Strict nested matching, input text " + text + " class: " + className + " pkg: " + packageName);
        HashMap<String, UIElement> uiElementMap = new HashMap<>();
        for (UIElement uiElement: uiElements.values()) {
            Utils.printDebug("In Strict nested matching, looking at top level element: " + uiElement.toString());
            uiElementMap.putAll(uiElement.findElementsByTextStrict(className, packageName, text, needClickable));
        }
        return new ArrayList<>(uiElementMap.values());
    }


    public UIElement findTopLevelElementById(String elementId) {
        return uiElements.get(elementId);
    }

    public UIElementTuple findElementAndTopLevelParentById(String elementId) {
        UIElement topLevelElement = uiElements.get(elementId);
        UIElement actualElement = null;
        if (topLevelElement == null) {
            //Search for child elements
            for (UIElement uiElement: uiElements.values()) {
                topLevelElement = uiElement;
                actualElement = uiElement.findElementById(elementId);
                if (actualElement != null) {
                    break;
                }
            }
        } else {
            actualElement = topLevelElement;
        }

        return new UIElementTuple(actualElement, topLevelElement);
    }



    public boolean mergeScreen(UIScreen uiScreen, boolean checkEquality) {
        if (checkEquality && !this.equals(uiScreen)) {
            return false;
        }
        uiElements = MergeUtils.mergeUIElements(uiElements, uiScreen.getUiElements());
        //uiPaths = MergeUtils.mergeUIPaths(uiPaths, uiScreen.getUiPaths());
        nextStepToScreens = MergeUtils.mergeHops(nextStepToScreens, uiScreen.getNextStepToScreens());
        lastStepToCurrentScreen = MergeUtils.mergeHops(lastStepToCurrentScreen, uiScreen.getLastStepToCurrentScreen());
        childScreens = MergeUtils.mergeChildScreens(childScreens, uiScreen.childScreens);
        lastUpdatedAtMs = lastUpdatedAtMs > uiScreen.getLastUpdatedAtMs() ? lastUpdatedAtMs : uiScreen.getLastUpdatedAtMs();
        return true;
    }

    public void addSemanticActionToScreen(SemanticAction semanticAction) {
        if (semanticAction == null) {
            return;
        }
        semanticActions.put(semanticAction.fetchSemanticActionId(), semanticAction);
    }

    @Override
    public String toString() {
        return "UIScreen{" +
                "id='" + id + '\'' +
                ", screenType='" + screenType + '\'' +
                ", packageName='" + packageName + '\'' +
                ", title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                '}';
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UIScreen)) return false;

        UIScreen uiScreen = (UIScreen) o;

        if (!screenType.equals(uiScreen.screenType)) return false;
        if (!packageName.equals(uiScreen.packageName)) return false;
        if (!title.equals(uiScreen.title)) return false;
        if (!subTitle.equals(uiScreen.subTitle)) return false;
        return deviceInfo.equals(uiScreen.deviceInfo);
    }

    @Override
    public int hashCode() {
        return getScreenHash(packageName, title, subTitle, screenType, deviceInfo.toString());
    }

    public class UIElementTuple {
        private UIElement uiElement;
        private UIElement topLevelParent;

        UIElementTuple(UIElement uiElement, UIElement topLevelParent) {
            this.uiElement = uiElement;
            this.topLevelParent = topLevelParent;

        }

        public UIElement getUiElement() {
            return uiElement;
        }

        public void setUiElement(UIElement uiElement) {
            this.uiElement = uiElement;
        }

        public UIElement getTopLevelParent() {
            return topLevelParent;
        }

        public void setTopLevelParent(UIElement topLevelParent) {
            this.topLevelParent = topLevelParent;
        }
    }

    private static int getScreenHash(String packageName, String title, String subTitle, String screenType, String deviceInfo) {
        int result = packageName.toLowerCase().hashCode();
        result = 31 * result + Utils.sanitizeText(title).hashCode();
        result = 31 * result + Utils.sanitizeText(subTitle).hashCode();
        result = 31 * result + Utils.sanitizeText(screenType).hashCode();
        result = 31 * result + Utils.sanitizeText(deviceInfo).hashCode();
        return result;
    }

    public static String getScreenId(String packageName, String title, String subTitle, String screenType, String deviceInfo) {
        return String.valueOf(getScreenHash(packageName, title, subTitle, screenType, deviceInfo));
    }

}
