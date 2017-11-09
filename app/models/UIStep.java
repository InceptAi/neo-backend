package models;

import util.Utils;

public class UIStep {

    public static final String TO_ANOTHER_SCREEN = "INTER_SCREEN";
    public static final String WITHIN_SAME_SCREEN = "WITHIN_SAME_SCREEN";
    public static final String SOFT_STEP_INTER_SCREEN = "SOFT_STEP_INTER_SCREEN";
    public static final String UNDEFINED = "UNDEFINED";

    private final String srcScreenId;
    private final String uiElementId;
    private final String uiEventId;
    private final String uiStepTypeId;
    private final String dstScreenId;

    public UIStep(String srcScreenId, String dstScreenId,
                  String uiElementId, String uiEventId,
                  String uiStepTypeId) {
        this.srcScreenId = srcScreenId;
        this.dstScreenId = dstScreenId;
        this.uiElementId = uiElementId;
        this.uiEventId = uiEventId;
        this.uiStepTypeId = uiStepTypeId;
    }

    public UIStep() {
        this.uiElementId = Utils.EMPTY_STRING;
        this.srcScreenId = Utils.EMPTY_STRING;
        this.uiEventId = Utils.EMPTY_STRING;
        this.dstScreenId = Utils.EMPTY_STRING;
        this.uiStepTypeId = UNDEFINED;
    }

    public UIStep(UIStep uiStep) {
        this.srcScreenId = uiStep.srcScreenId;
        this.dstScreenId = uiStep.dstScreenId;
        this.uiElementId = uiStep.uiElementId;
        this.uiEventId = uiStep.uiEventId;
        this.uiStepTypeId = uiStep.uiStepTypeId;
    }


    //Factory constructor
    public static UIStep copyStep(UIStep uiStep) {
        if (uiStep == null) {
            return null;
        }
        return  new UIStep(uiStep);
    }


    public String getDstScreenId() {
        return dstScreenId;
    }

    public String getSrcScreenId() {
        return srcScreenId;
    }

    public String getUiElementId() {
        return uiElementId;
    }

    public String getUiEventId() {
        return uiEventId;
    }

    public String getUiStepTypeId() {
        return uiStepTypeId;
    }

    public boolean checkIfUndefined() {
        return uiStepTypeId.equalsIgnoreCase(UNDEFINED);
    }

    public boolean checkIfInterScreenStep() {
        return uiStepTypeId.equalsIgnoreCase(TO_ANOTHER_SCREEN);
    }

    public boolean checkIfWithinSameScreen() {
        return uiStepTypeId.equalsIgnoreCase(WITHIN_SAME_SCREEN);
    }

    public boolean checkIfSoftStep() { return uiStepTypeId.equalsIgnoreCase(SOFT_STEP_INTER_SCREEN); }

    @Override
    public String toString() {
        return "UIStep{" +
                "srcScreenId='" + srcScreenId + '\'' +
                ", uiElementId='" + uiElementId + '\'' +
                ", uiEventId='" + uiEventId + '\'' +
                ", uiStepTypeId='" + uiStepTypeId + '\'' +
                ", dstScreenId='" + dstScreenId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UIStep)) return false;

        UIStep uiStep = (UIStep) o;

        if (!srcScreenId.equals(uiStep.srcScreenId)) return false;
        if (!uiElementId.equals(uiStep.uiElementId)) return false;
        return dstScreenId.equals(uiStep.dstScreenId);
    }

    @Override
    public int hashCode() {
        int result = srcScreenId.hashCode();
        result = 31 * result + uiElementId.hashCode();
        result = 31 * result + dstScreenId.hashCode();
        return result;
    }
}
