package models;

import util.Utils;
import util.ViewUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SemanticAction {
    public static final String UNDEFINED = "UNDEFINED";
    public static final String TOGGLE = "TOGGLE";
    public static final String SEEK = "SEEK";
    @SuppressWarnings("unused")
    public static final String EDIT_TEXT = "EDIT_TEXT";
    @SuppressWarnings("unused")
    public static final String SUBMIT = "SUBMIT";
    @SuppressWarnings("unused")
    public static final String SELECT = "SELECT";
    public static final String NAVIGATE = "NAVIGATE";

    private String screenTitle;
    private String screenSubTitle;
    private String packageName;
    private String semanticActionDescription;
    private String semanticActionType;
    private String uiScreenId;
    private String uiElementId;
    private String uiElementParentId;
    private String uiActionId;
    private MatchingInfo matchingInfo;

    private SemanticAction(UIScreen uiScreen, UIElement uiElement, UIElement parentElement, UIAction uiAction) {
        String actionDescription = uiElement.fetchAllText();
        String actionType = SemanticAction.UNDEFINED;
        switch (uiElement.getClassName()) {
            case ViewUtils.SWITCH_CLASS_NAME:
            case ViewUtils.CHECK_BOX_CLASS_NAME:
            case ViewUtils.TOGGLE_BUTTON_CLASS_NAME:
                if (parentElement != null) {
                    if (!parentElement.checkIsClickable()) {
                        //Parent element is not clickable need to add actionType and text here
                        actionType = SemanticAction.TOGGLE;
                        actionDescription = parentElement.fetchAllText();
                    }
                } else {
                    actionType = SemanticAction.TOGGLE;
                    actionDescription = uiScreen.getTitle() + " " + uiElement.fetchAllText(); //Not sure if this will work
                }
                break;
            case ViewUtils.LINEAR_LAYOUT_CLASS_NAME:
            case ViewUtils.RELATIVE_LAYOUT_CLASS_NAME:
            case ViewUtils.FRAME_LAYOUT_CLASS_NAME:
                //We have a ll, rl, or fl which is clickable
                //Check if element has a child switch/checkbox -- assign toggle
                if (uiElement.getNumToggleableChildren() == 1) {
                    actionType = SemanticAction.TOGGLE;
                }
//              else if (uiElement.getNumToggleableChildren() > 1){
//                    //Not sure how to handle this -- leave it undefined
//              }
                break;
            case ViewUtils.CHECKED_TEXT_VIEW_CLASS_NAME:
            case ViewUtils.RADIO_BUTTON_CLASS_NAME:
                actionType = SemanticAction.TOGGLE;
                break;
//            case ViewUtils.SWITCH_CLASS_NAME:
//            case ViewUtils.CHECK_BOX_CLASS_NAME:
//                actionDescription = ViewUtils.isTemplateText(elementText) ? screenTitle : elementText;
//                break;
            case ViewUtils.SEEK_BAR_CLASS_NAME:
                actionType = SemanticAction.SEEK;
                break;
            case ViewUtils.BUTTON_CLASS_NAME:
            case ViewUtils.IMAGE_BUTTON_CLASS_NAME:
            case ViewUtils.IMAGE_VIEW_CLASS_NAME:
            case ViewUtils.EDIT_TEXT_VIEW_CLASS_NAME:
                //TODO validate this
                actionType = uiElement.fetchAllText();
                break;
            default:
                break;
        }
        this.semanticActionDescription = actionDescription;
        this.semanticActionType = actionType;
        this.uiScreenId = uiScreen.getId();
        this.uiElementId = uiElement.id();
        this.uiActionId = uiAction.id();
        this.screenTitle = uiScreen.getTitle();
        this.screenSubTitle = uiScreen.getSubTitle();
        this.packageName = uiScreen.getPackageName();
        this.matchingInfo = uiScreen.getMatchingInfo();
        this.uiElementParentId = parentElement != null ? parentElement.getId() : Utils.EMPTY_STRING;
    }


    private SemanticAction() {
        screenTitle = Utils.EMPTY_STRING;
        semanticActionDescription = SemanticAction.UNDEFINED;
        semanticActionType = Utils.EMPTY_STRING;
        uiScreenId = Utils.EMPTY_STRING;
        uiElementId = Utils.EMPTY_STRING;
        uiActionId = Utils.EMPTY_STRING;
        screenTitle = Utils.EMPTY_STRING;
        packageName = Utils.EMPTY_STRING;
        matchingInfo = new MatchingInfo();
        screenSubTitle = Utils.EMPTY_STRING;
        uiElementParentId = Utils.EMPTY_STRING;
    }

    public SemanticAction(String screenTitle, String screenSubTitle, String packageName,
                          String semanticActionDescription, String semanticActionType,
                          String uiScreenId, String uiElementId, String uiActionId,
                          MatchingInfo matchingInfo, String uiElementParentId) {
        this.screenTitle = screenTitle;
        this.screenSubTitle = screenSubTitle;
        this.packageName = packageName;
        this.semanticActionDescription = semanticActionDescription;
        this.semanticActionType = semanticActionType;
        this.uiScreenId = uiScreenId;
        this.uiElementId = uiElementId;
        this.uiActionId = uiActionId;
        this.matchingInfo = matchingInfo;
        this.uiElementParentId = uiElementParentId;
    }

    /**
     * Factory constructor to create an instance
     * @return Instance of SemanticAction or null on error.
     */
    public static SemanticAction create(UIScreen uiScreen, UIElement uiElement, UIElement parentElement, UIAction uiAction) {
        if (uiAction.equals(UIAction.CLICK) || uiAction.equals(UIAction.SELECT)) {
            return new SemanticAction(uiScreen, uiElement, parentElement, uiAction);
        }
        return new SemanticAction();
    }


    public String getSemanticActionType() {
        return semanticActionType;
    }

    public void setSemanticActionType(String semanticActionType) {
        this.semanticActionType = semanticActionType;
    }

    public void setUiActionId(String uiActionId) {
        this.uiActionId = uiActionId;
    }

    public void setSemanticActionDescription(String semanticActionDescription) {
        this.semanticActionDescription = semanticActionDescription;
    }

    public String getUiScreenId() {
        return uiScreenId;
    }

    public void setUiScreenId(String uiScreenId) {
        this.uiScreenId = uiScreenId;
    }

    public String getUiElementId() {
        return uiElementId;
    }

    public void setUiElementId(String uiElementId) {
        this.uiElementId = uiElementId;
    }

    public String getUiActionId() {
        return uiActionId;
    }

    public String getSemanticActionDescription() {
        return semanticActionDescription;
    }

    public String fetchSemanticActionId() {
        return String.valueOf(hashCode());
    }

    public String getScreenSubTitle() {
        return screenSubTitle;
    }

    public void setScreenSubTitle(String screenSubTitle) {
        this.screenSubTitle = screenSubTitle;
    }

    public String getUiElementParentId() {
        return uiElementParentId;
    }

    public void setUiElementParentId(String uiElementParentId) {
        this.uiElementParentId = uiElementParentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SemanticAction)) return false;

        SemanticAction that = (SemanticAction) o;

        if (!screenTitle.equals(that.screenTitle)) return false;
        if (!screenSubTitle.equals(that.screenSubTitle)) return false;
        if (!packageName.equals(that.packageName)) return false;
        if (!semanticActionDescription.equals(that.semanticActionDescription)) return false;
        if (!semanticActionType.equals(that.semanticActionType)) return false;
        if (!uiScreenId.equals(that.uiScreenId)) return false;
        if (!uiElementId.equals(that.uiElementId)) return false;
        if (!matchingInfo.equals(that.matchingInfo)) return false;
        return uiActionId.equals(that.uiActionId);
    }

    @Override
    public int hashCode() {
        int result = screenTitle.hashCode();
        result = 31 * result + screenSubTitle.hashCode();
        result = 31 * result + packageName.hashCode();
        result = 31 * result + semanticActionDescription.hashCode();
        result = 31 * result + semanticActionType.hashCode();
        result = 31 * result + uiScreenId.hashCode();
        result = 31 * result + uiElementId.hashCode();
        result = 31 * result + uiActionId.hashCode();
        result = 31 * result + matchingInfo.hashCode();
        result = 31 * result + uiElementParentId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SemanticAction{" +
                "screenTitle='" + screenTitle + '\'' +
                "screenSubTitle='" + screenSubTitle + '\'' +
                ", packageName='" + packageName + '\'' +
                ", semanticActionDescription='" + semanticActionDescription + '\'' +
                ", semanticActionType='" + semanticActionType + '\'' +
                ", uiScreenId='" + uiScreenId + '\'' +
                ", uiElementId='" + uiElementId + '\'' +
                ", uiElementParentId='" + uiElementParentId + '\'' +
                ", uiActionId='" + uiActionId + '\'' +
                ", deviceInfo='" + matchingInfo + '\'' +
                '}';
    }

    public String getScreenTitle() {
        return screenTitle;
    }

    public void setScreenTitle(String screenTitle) {
        this.screenTitle = screenTitle;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public MatchingInfo getMatchingInfo() {
        return matchingInfo;
    }

    public void setMatchingInfo(MatchingInfo matchingInfo) {
        this.matchingInfo = matchingInfo;
    }

    public List<String> fetchStringsToMatch() {
        List<String> stringList = new ArrayList<>();
        String commonString = screenTitle + " "  + screenSubTitle + " " + semanticActionDescription;
        commonString = commonString.replaceAll("\\s+"," ");
        switch (semanticActionType) {
            case TOGGLE:
                //Generate text for on/off/enable/disable etc.
                List<String> replacementWords = Arrays.asList("on", "off", "enable", "disable");
                for (String replacementWord: replacementWords) {
                    stringList.add(Utils.replaceWord(commonString, ViewUtils.getMapForOnOffTemplateReplacement(replacementWord)));
                }
                break;
            default:
                break;
        }
        List<String> descriptionListToReturn = new ArrayList<>();
        for (String description: stringList) {
            descriptionListToReturn.add(Utils.removeSuccessiveDuplicateWords(description));
        }
        return descriptionListToReturn;
    }


    public static boolean checkIfSemanticActionIsDefined(SemanticAction semanticAction) {
        return !semanticAction.getSemanticActionType().equalsIgnoreCase(SemanticAction.UNDEFINED);
    }
}
