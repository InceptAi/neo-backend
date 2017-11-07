package models;

import util.Utils;

public class UIStep {

    public enum UIStepType {
        TO_ANOTHER_SCREEN("INTER_SCREEN"),
        WITHIN_SAME_SCREEN("WITHIN_SAME_SCREEN"),
        SOFT_STEP_INTER_SCREEN("SOFT_STEP_INTER_SCREEN"),
        UNDEFINED("UNDEFINED");

        private String id;

        UIStepType(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    private String srcScreenId;
    private String uiElementId;
    private String uiEventId;
    private String uiStepTypeId;
    private String dstScreenId;

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
        this.uiStepTypeId = UIStepType.UNDEFINED.id();
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

    public boolean isUndefined() {
        return uiStepTypeId.equalsIgnoreCase(UIStepType.UNDEFINED.id());
    }

    public boolean isInterScreenStep() {
        return uiStepTypeId.equalsIgnoreCase(UIStepType.TO_ANOTHER_SCREEN.id());
    }

    public boolean isWithinSameScreen() {
        return uiStepTypeId.equalsIgnoreCase(UIStepType.WITHIN_SAME_SCREEN.id());
    }

    public boolean isSoftStep() { return uiStepTypeId.equalsIgnoreCase(UIStepType.SOFT_STEP_INTER_SCREEN.id()); }

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
