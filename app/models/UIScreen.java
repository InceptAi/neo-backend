package models;

import nlu.SimpleTextInterpreter;
import util.MergeUtils;
import util.Utils;


import java.util.*;

public class UIScreen {
    private String id = Utils.EMPTY_STRING;
    private String packageName = Utils.EMPTY_STRING;
    private String title = Utils.EMPTY_STRING;
    private List<UIPath> uiPaths;
    private HashMap<String, UIElement> uiElements;
    private HashMap<String, String> deviceInfo;


    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return getScreenId(packageName, title, deviceInfo.toString());
    }

    public UIScreen() {
        this.uiPaths = new ArrayList<>();
        this.uiElements = new HashMap<>();
        this.deviceInfo = new HashMap<>();
    }

    public void setUiPaths(List<UIPath> uiPaths) {
        this.uiPaths = uiPaths;
    }

    public void add(UIPath uiPath) {
        this.uiPaths.add(uiPath);
    }

    public void add(UIElement uiElement) {
        this.uiElements.put(uiElement.id(), uiElement);
    }

    public void update(String odlElementId, UIElement uiElement) {
        this.uiElements.remove(odlElementId);
        this.uiElements.put(uiElement.id(), uiElement);
    }

    public List<UIPath> getUiPaths() {
        return uiPaths;
    }

    public HashMap<String, UIElement> getUiElements() {
        return uiElements;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTitle() {
        return title;
    }

    public HashMap<String, String> getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(HashMap<String, String> deviceInfo) {
        this.deviceInfo = deviceInfo;
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
        final double MIN_MATCH_PERCENTAGE_FOR_ELEMENT = 0.5;
        HashMap<String, UIElement> uiElementListToReturn = new HashMap<>();
        HashMap<String, Double> matchingUIElementScores = new HashMap<>();
        HashMap<String, UIElement> matchingUIElements = new HashMap<>();
        SimpleTextInterpreter textInterpreter = new SimpleTextInterpreter(MIN_MATCH_PERCENTAGE_FOR_ELEMENT);
        for (UIElement uiElement: uiElements.values()) {
            //Do fuzzy search
            UIElement.ElementScore matchingElementScore = uiElement.findElementByTextFuzzy(text, textInterpreter, needClickable);
            if (matchingElementScore != null) {
                matchingUIElementScores.put(matchingElementScore.getElement().id(), matchingElementScore.getScore());
                matchingUIElements.put(matchingElementScore.getElement().id(), matchingElementScore.getElement());
            }
        }

        //Sort the hash map based on match metric
        Map<String, Double> sortedMetricMap = Utils.sortHashMapByValueDescending(matchingUIElementScores);
        for (HashMap.Entry<String, Double> entry : sortedMetricMap.entrySet()) {
            //TODO: This will not work for nested elements being returned as uiElements.get -- returns null for nested ids
            //uiElementListToReturn.put(entry.getKey(), uiElements.get(entry.getKey()));
            uiElementListToReturn.put(entry.getKey(), matchingUIElements.get(entry.getKey()));
        }
        return new ArrayList<UIElement>(uiElementListToReturn.values());
    }

//    private List<UIElement> findElementsInScreenFuzzyOld(String className, String packageName, String text, boolean isClickable) {
//        List<UIElement> uiElementListToReturn = new ArrayList<>();
//        HashMap<String, Double> matchingUIElements = new HashMap<>();
//        SimpleTextInterpreter textInterpreter = new SimpleTextInterpreter(0);
//        for (UIElement uiElement: uiElements.values()) {
//            //Do fuzzy search
//            double matchMetric = textInterpreter.getMatchMetric(text, uiElement.getAllText());
//            if ( matchMetric > 0 && (!isClickable || uiElement.isClickable())) {
//                matchingUIElements.put(uiElement.id(), matchMetric);
//            }
//        }
//        //Sort the hash map based on match metric
//        Map<String, Double> sortedMetricMap = Utils.sortHashMapByValueDescending(matchingUIElements);
//        for (HashMap.Entry<String, Double> entry : sortedMetricMap.entrySet()) {
//            uiElementListToReturn.add(uiElements.get(entry.getKey()));
//        }
//        return uiElementListToReturn;
//    }


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

//    private List<UIElement> findElementsInScreenStrict(String className, String packageName, String text, boolean isClickable) {
//        List<UIElement> uiElementList = new ArrayList<>();
//        for (UIElement uiElement: uiElements.values()) {
//            if (uiElement.getClassName().equals(className) &&
//                    uiElement.getPackageName().equals(packageName) &&
//                    uiElement.isMatchForText(text)) {
//                uiElementList.add(uiElement);
//            }
//        }
//        return uiElementList;
//    }

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


    public static String getScreenId(String packageName, String title, String deviceInfo) {
        String toHash = packageName.toLowerCase() + "#" + title.replaceAll(" ", "_").toLowerCase() + "#" + deviceInfo.replaceAll(" ", "_").toLowerCase();
        int hashCode = (toHash).hashCode();
        return String.valueOf(hashCode);
    }

    public boolean mergeScreen(UIScreen uiScreen) {
        if (!this.equals(uiScreen)) {
            return false;
        }
        uiElements = MergeUtils.mergeUIElements(uiElements, uiScreen.getUiElements());
        uiPaths = MergeUtils.mergeUIPaths(uiPaths, uiScreen.getUiPaths());
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UIScreen)) return false;

        UIScreen uiScreen = (UIScreen) o;

        if (!packageName.equals(uiScreen.packageName)) return false;
        if (!title.equals(uiScreen.title)) return false;
        return deviceInfo.equals(uiScreen.deviceInfo);
    }

    @Override
    public int hashCode() {
        int result = packageName.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + deviceInfo.hashCode();
        return result;
    }

    public class UIElementTuple {
        private UIElement uiElement;
        private UIElement topLevelParent;

        public UIElementTuple(UIElement uiElement, UIElement topLevelParent) {
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

}
