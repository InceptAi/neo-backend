package models;

public class NavigationalAction {
    private String uiActionId;
    private String nextUIScreenId;

    public NavigationalAction() {}

    public NavigationalAction(String uiActionId, String uiScreenId) {
        this.uiActionId = uiActionId;
        this.nextUIScreenId = uiScreenId;
    }

    public String getUiActionId() {
        return uiActionId;
    }

    public String getNextUIScreenId() {
        return nextUIScreenId;
    }

    public void setUiActionId(String uiActionId) {
        this.uiActionId = uiActionId;
    }

    public void setNextUIScreenId(String nextUIScreenId) {
        this.nextUIScreenId = nextUIScreenId;
    }
}
