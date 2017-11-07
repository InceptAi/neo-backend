package models;

import nlu.TextInterpreter;
import util.Utils;
import util.ViewUtils;

import java.util.*;

public class UIElement {
    public static String TITLE_RESOURCE_ID = "android:id/title";
    public static String SUMMARY_RESOURCE_ID = "android:id/summary";
    private String id = Utils.EMPTY_STRING;
    private String className = Utils.EMPTY_STRING;
    private String packageName = Utils.EMPTY_STRING;
    private String primaryText = Utils.EMPTY_STRING;
    private String resourceType = Utils.EMPTY_STRING;
    private HashMap<String, UIElement> childElements;
    private List<UIAction> uiActions;
    private HashMap<String, SemanticAction> semanticActions;
    private List<NavigationalAction> navigationalActions;
    private List<UIStep> lastStepToGetToThisElement;
    private int numToggleableChildren;
    private boolean isToggleable;
    private int leftX;
    private int topY;
    private int rightX;
    private int bottomY;

    public UIElement() {}

    public UIElement(String className, String packageName, String primaryText, boolean isToggleable) {
        this.className = className;
        this.packageName = packageName;
        this.uiActions = new ArrayList<>();
        this.semanticActions = new HashMap<>();
        this.navigationalActions = new ArrayList<>();
        this.childElements = new HashMap<>();
        this.lastStepToGetToThisElement = new ArrayList<>();
        this.primaryText = primaryText;
        this.numToggleableChildren = 0;
        this.isToggleable = isToggleable;
    }

    public void setCoordinates(int leftX, int topY, int rightX, int bottomY) {
        this.leftX = leftX;
        this.topY = topY;
        this.rightX = rightX;
        this.bottomY = bottomY;
    }


    public UIElement(String id, String className, String packageName,
                     String primaryText, String resourceType,
                     HashMap<String, UIElement> childElements,
                     List<UIAction> uiActions, HashMap<String, SemanticAction> semanticActions,
                     List<NavigationalAction> navigationalActions,
                     List<UIStep> lastStepToGetToThisElement,
                     int numToggleableChildren, boolean isToggleable,
                     int leftX, int topY, int rightX, int bottomY) {
        this.id = id;
        this.className = className;
        this.packageName = packageName;
        this.primaryText = primaryText;
        this.resourceType = resourceType;
        this.childElements = childElements;
        this.uiActions = uiActions;
        this.semanticActions = semanticActions;
        this.navigationalActions = navigationalActions;
        this.lastStepToGetToThisElement = lastStepToGetToThisElement;
        this.numToggleableChildren = numToggleableChildren;
        this.isToggleable = isToggleable;
        this.leftX = leftX;
        this.topY = topY;
        this.rightX = rightX;
        this.bottomY = bottomY;
    }

    public int getLeftX() {
        return leftX;
    }

    public void setLeftX(int leftX) {
        this.leftX = leftX;
    }

    public int getTopY() {
        return topY;
    }

    public void setTopY(int topY) {
        this.topY = topY;
    }

    public int getRightX() {
        return rightX;
    }

    public void setRightX(int rightX) {
        this.rightX = rightX;
    }

    public int getBottomY() {
        return bottomY;
    }

    public void setBottomY(int bottomY) {
        this.bottomY = bottomY;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public HashMap<String, SemanticAction> getSemanticActions() {
        return semanticActions;
    }

    public boolean isToggleable() {
        return isToggleable;
    }

    public void setIsToggleable(boolean toggleable) {
        this.isToggleable = toggleable;
    }

    public int getNumToggleableChildren() {
        return numToggleableChildren;
    }

    public void setNumToggleableChildren(int numToggleableChildren) {
        this.numToggleableChildren = numToggleableChildren;
    }

    public String getId() {
        return String.valueOf(hashCode());
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setChildElements(HashMap<String, UIElement> childElements) {
        this.childElements = childElements;
    }

    public void setLastStepToGetToThisElement(List<UIStep> lastStepToGetToThisElement) {
        this.lastStepToGetToThisElement = lastStepToGetToThisElement;
    }

    public String id() {
        return String.valueOf(hashCode());
    }

    public void addChildren(UIElement uiElement) {
        this.childElements.put(uiElement.getId(), uiElement);
        if (uiElement.isToggleable()) {
            numToggleableChildren++;
        }
        numToggleableChildren += uiElement.getNumToggleableChildren();
    }

    public void finalizeChildElementIds() {
        HashMap<String, UIElement> elementHashMap = new HashMap<>();
        for (UIElement childElement: childElements.values()) {
            childElement.finalizeChildElementIds();
            String finalChildElementId = childElement.getId();
            elementHashMap.put(finalChildElementId, childElement);
        }
        childElements = elementHashMap;
    }

    public boolean isTitleTextView() {
        return className.equalsIgnoreCase(ViewUtils.TEXT_VIEW_CLASS_NAME) && resourceType.equalsIgnoreCase(TITLE_RESOURCE_ID);
    }

    public void add(UIAction uiAction) {
        this.uiActions.add(uiAction);
    }

    public void add(UIStep uiStep) {
        this.lastStepToGetToThisElement.add(uiStep);
    }

    public void add(NavigationalAction navigationalAction) {
        this.navigationalActions.add(navigationalAction);
    }

    public void add(String uiActionId, SemanticAction semanticAction) {
            semanticActions.put(uiActionId, semanticAction);
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getPrimaryText() {
        return primaryText;
    }

    public HashMap<String, UIElement> getChildElements() {
        return childElements;
    }

    public List<UIStep> getLastStepToGetToThisElement() {
        return lastStepToGetToThisElement;
    }

    public List<UIAction> getUiActions() {
        return uiActions;
    }

    public List<NavigationalAction> getNavigationalActions() {
        return navigationalActions;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setPrimaryText(String primaryText) {
        this.primaryText = primaryText;
    }

    public void setUiActions(List<UIAction> uiActions) {
        this.uiActions = uiActions;
    }

    public void setSemanticActions(HashMap<String, SemanticAction> semanticActions) {
        this.semanticActions = semanticActions;
    }

    public void setNavigationalActions(List<NavigationalAction> navigationalActions) {
        this.navigationalActions = navigationalActions;
    }

    public boolean isClickable() {
        return uiActions.contains(UIAction.CLICK);
    }

    public ElementScore findElementByTextFuzzy(String textToFind, TextInterpreter textInterpreter, boolean needClickable) {
        double matchMetric = textInterpreter.getMatchMetric(textToFind, getAllText());

        if (matchMetric == 0) {
            return null;
        }

        Utils.printDebug("In findElementByTextFuzzy, metric: " + matchMetric + " text: " + textToFind + " eleText: " + getAllText());
        //Match is > 0 and is clickable if needed
        if (isClickable() || !needClickable) {
            Utils.printDebug("In findElementByTextFuzzy, found clickable match");
            return new ElementScore(this, matchMetric);
        }

        //Find a clickable child with non zero score
        double bestScore = 0;
        ElementScore bestElementScore = null;
        HashMap<String, Double> childScores = new HashMap<>();
        for (UIElement uiElement: childElements.values()) {
            ElementScore childElementScore = uiElement.findElementByTextFuzzy(textToFind, textInterpreter, needClickable);
            if (childElementScore != null && childElementScore.getScore() > bestScore) {
                bestElementScore = childElementScore;
            }
        }
        return bestElementScore;
    }


    HashMap<String, UIElement> findElementsByTextStrict(String className, String packageName, String inputText, boolean needClickable) {
        boolean textMatches = isMatchForText(inputText);
        if (!textMatches) {
            return new HashMap<>();
        }


        HashMap<String, UIElement> stringUIElementHashMap = new HashMap<>();

        if (!needClickable || isClickable() &&
                this.className.equalsIgnoreCase(className) &&
                this.packageName.equalsIgnoreCase(packageName)) {
            Utils.printDebug("In findElementsByTextStrict, found match for element: " + this.toString());
            stringUIElementHashMap.put(this.getId(), this);
            return stringUIElementHashMap;
        }

        for (UIElement childElement: childElements.values()) {
            stringUIElementHashMap.putAll(childElement.findElementsByTextStrict(className, packageName, inputText, needClickable));
        }
        return stringUIElementHashMap;
    }


    public UIElement findElementById(String elementId) {
        UIElement uiElement = childElements.get(elementId);
        if (uiElement != null) {
            return uiElement;
        }
        for (UIElement childElement: childElements.values()) {
            UIElement nestedElement = childElement.findElementById(elementId);
            if (nestedElement != null) {
                return nestedElement;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UIElement)) return false;

        UIElement uiElement = (UIElement) o;

        if (!className.equals(uiElement.className)) return false;
        if (!packageName.equals(uiElement.packageName)) return false;
        if (!primaryText.equals(uiElement.primaryText)) return false;
        return childElements != null ? childElements.equals(uiElement.childElements) : uiElement.childElements == null;
    }

    @Override
    public int hashCode() {
        int result = className.hashCode();
        result = 31 * result + packageName.hashCode();
        result = 31 * result + primaryText.hashCode();
        int childHashCode = 0;
        for (UIElement childElement: childElements.values()) {
            childHashCode = childHashCode + childElement.hashCode();
        }
        result = 31 * result + childHashCode;
        return result;
    }

    public String getChildText() {
        StringBuilder childTextBuilder = new StringBuilder();
        if (childElements != null && !childElements.isEmpty()) {
            for (UIElement uiElement : childElements.values()) {
                childTextBuilder.append(uiElement.getPrimaryText());
                childTextBuilder.append(" ");
            }
        }
        return childTextBuilder.toString().trim();
    }

    public String getAllText() {
        String toReturn = Utils.EMPTY_STRING;
        String childText = getChildText();
        if (!Utils.nullOrEmpty(primaryText)) {
            toReturn = toReturn + primaryText;
        }
        if (!Utils.nullOrEmpty(childText)) {
            toReturn = toReturn + " " + childText;
        }
        return toReturn.trim();
    }

    boolean isMatchForText(String inputText) {
        String inputToTest = inputText.toLowerCase();
        String childText = getChildText();
        return (primaryText.toLowerCase().contains(inputToTest) ||
                childText.toLowerCase().contains(inputToTest));
    }

    @Override
    public String toString() {
        return "UIElement{" +
                "getId='" + id() + '\'' +
                ", className='" + className + '\'' +
                ", packageName='" + packageName + '\'' +
                ", primaryText='" + primaryText + '\'' +
                ", childElements=" + childElements +
                ", uiActions=" + uiActions +
                ", semanticActions=" + semanticActions +
                ", navigationalActions=" + navigationalActions +
                ", lastStepToGetToThisElement=" + lastStepToGetToThisElement +
                '}';
    }

    public class ElementScore {
        private UIElement element;
        private double score;

        public ElementScore(UIElement element, double score) {
            this.element = element;
            this.score = score;
        }

        public UIElement getElement() {
            return element;
        }

        public void setElement(UIElement element) {
            this.element = element;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }

    public static UIElement findFirstParentWithGivenClassNames(String elementId, Set<String> classNames, UIElement topLevelParent) {
        if (topLevelParent == null) {
            return null;
        }

        if (classNames.contains(topLevelParent.getClassName()) &&
                topLevelParent.getChildElements().keySet().contains(elementId)) {
            //Found it, return this
            return topLevelParent;
        }
        for (UIElement uiElement: topLevelParent.getChildElements().values()) {
            UIElement matchingParentElement = findFirstParentWithGivenClassNames(elementId, classNames, uiElement);
            if (matchingParentElement != null) {
                return matchingParentElement;
            }
        }
        return null;
    }

    public static UIElement findFirstTitleTextViewInElement(UIElement topLevelParent) {
        if (topLevelParent == null) {
            return null;
        }

        if (topLevelParent.isTitleTextView()) {
            return topLevelParent;
        }

        for (UIElement uiElement: topLevelParent.getChildElements().values()) {
            UIElement titleTextViewElement = findFirstTitleTextViewInElement(uiElement);
            if (titleTextViewElement != null) {
                return titleTextViewElement;
            }
        }
        return null;
    }

}
