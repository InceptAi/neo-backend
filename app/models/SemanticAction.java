package models;

import util.Utils;
import util.ViewUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SemanticAction {
    private String screenTitle;
    private String packageName;
    private String semanticActionDescription;
    private String semanticActionName;
    private String uiScreenId;
    private String uiElementId;
    private String uiActionId;
    private String deviceInfo;

    private SemanticAction(UIScreen uiScreen, UIElement uiElement, UIElement parentElement, UIAction uiAction) {
        //Utils.printDebug("In semantic action create, uiElement: " + uiElement.toString() + " parentElement: " + parentElement);
        //Utils.printDebug("Adding semantic action with className:" + className + " elementText:" + elementText);
        String actionDescription = uiElement.getAllText();
        String actionName = SemanticActionType.UNDEFINED.id();
        switch (uiElement.getClassName()) {
            case ViewUtils.SWITCH_CLASS_NAME:
            case ViewUtils.CHECK_BOX_CLASS_NAME:
                if (parentElement != null) {
                    if (!parentElement.isClickable()) {
                        //Parent element is not clickable need to add actionName and text here
                        actionName = SemanticActionType.TOGGLE.id();
                        actionDescription = parentElement.getAllText();
                    }
                } else {
                    actionName = SemanticActionType.TOGGLE.id();
                    actionDescription = uiScreen.getTitle() + " " + uiElement.getAllText(); //Not sure if this will work
                }
                break;
            case ViewUtils.LINEAR_LAYOUT_CLASS_NAME:
            case ViewUtils.RELATIVE_LAYOUT_CLASS_NAME:
            case ViewUtils.FRAME_LAYOUT_CLASS_NAME:
                //We have a ll, rl, or fl which is clickable
                //Check if element has a child switch/checkbox -- assign toggle
                if (uiElement.getNumToggleableChildren() == 1) {
                    actionName = SemanticActionType.TOGGLE.id();
                }
//              else if (uiElement.getNumToggleableChildren() > 1){
//                    //Not sure how to handle this -- leave it undefined
//              }
                break;
            case ViewUtils.CHECKED_TEXT_VIEW_CLASS_NAME:
            case ViewUtils.RADIO_BUTTON_CLASS_NAME:
                actionName = SemanticActionType.TOGGLE.id();
                break;
//            case ViewUtils.SWITCH_CLASS_NAME:
//            case ViewUtils.CHECK_BOX_CLASS_NAME:
//                actionDescription = ViewUtils.isTemplateText(elementText) ? screenTitle : elementText;
//                actionName = SemanticActionType.TOGGLE.getId();
//                break;
            case ViewUtils.SEEK_BAR_CLASS_NAME:
                actionName = SemanticActionType.SEEK.id();
                break;
            case ViewUtils.BUTTON_CLASS_NAME:
            case ViewUtils.IMAGE_BUTTON_CLASS_NAME:
                //TODO validate this
                actionName = uiElement.getAllText();
                break;
            default:
                break;
        }
        this.semanticActionDescription = actionDescription;
        this.semanticActionName = actionName;
        this.uiScreenId = uiScreen.getId();
        this.uiElementId = uiElement.id();
        this.uiActionId = uiAction.id();
        this.screenTitle = uiScreen.getTitle();
        this.packageName = uiScreen.getPackageName();
        this.deviceInfo = uiScreen.getDeviceInfo().toString();
    }


    public SemanticAction() {
        screenTitle = Utils.EMPTY_STRING;
        semanticActionDescription = SemanticActionType.UNDEFINED.id();
        semanticActionName = Utils.EMPTY_STRING;
        uiScreenId = Utils.EMPTY_STRING;
        uiElementId = Utils.EMPTY_STRING;
        uiActionId = Utils.EMPTY_STRING;
        screenTitle = Utils.EMPTY_STRING;
        packageName = Utils.EMPTY_STRING;
        deviceInfo = Utils.EMPTY_STRING;
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


    public String getSemanticActionName() {
        return semanticActionName;
    }

    public void setSemanticActionName(String semanticActionName) {
        this.semanticActionName = semanticActionName;
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

    public String getId() {
        return String.valueOf(hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SemanticAction)) return false;

        SemanticAction that = (SemanticAction) o;

        if (!screenTitle.equals(that.screenTitle)) return false;
        if (!packageName.equals(that.packageName)) return false;
        if (!semanticActionDescription.equals(that.semanticActionDescription)) return false;
        if (!semanticActionName.equals(that.semanticActionName)) return false;
        if (!uiScreenId.equals(that.uiScreenId)) return false;
        if (!uiElementId.equals(that.uiElementId)) return false;
        if (!deviceInfo.equals(that.deviceInfo)) return false;
        return uiActionId.equals(that.uiActionId);
    }

    @Override
    public int hashCode() {
        int result = screenTitle.hashCode();
        result = 31 * result + packageName.hashCode();
        result = 31 * result + semanticActionDescription.hashCode();
        result = 31 * result + semanticActionName.hashCode();
        result = 31 * result + uiScreenId.hashCode();
        result = 31 * result + uiElementId.hashCode();
        result = 31 * result + uiActionId.hashCode();
        result = 31 * result + deviceInfo.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SemanticAction{" +
                "screenTitle='" + screenTitle + '\'' +
                ", packageName='" + packageName + '\'' +
                ", semanticActionDescription='" + semanticActionDescription + '\'' +
                ", semanticActionName='" + semanticActionName + '\'' +
                ", uiScreenId='" + uiScreenId + '\'' +
                ", uiElementId='" + uiElementId + '\'' +
                ", uiActionId='" + uiActionId + '\'' +
                ", deviceInfo='" + deviceInfo + '\'' +
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

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public List<String> fetchStringsToMatch() {
        List<String> stringList = new ArrayList<>();
        SemanticActionType semanticActionType = SemanticActionType.typeStringToEnum(semanticActionName);
        String commonString = screenTitle + " "  + semanticActionDescription;
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
        return stringList;
    }


    public static boolean isUndefined(SemanticAction semanticAction) {
        return semanticAction.getSemanticActionName().equalsIgnoreCase(SemanticActionType.UNDEFINED.id());
    }
}
