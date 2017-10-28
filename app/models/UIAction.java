package models;

public enum UIAction {
    CLICK("CLICK"),
    EDIT_TEXT("EDIT_TEXT"),
    SCROLL("SCROLL"),
    FOCUS("FOCUS"),
    SELECT("SELECT"),
    POPUP("POPUP"),
    CONTENT_CHANGE("CONTENT_CHANGE"),
    WINDOW_STATE_CHANGE("WINDOW_STATE_CHANGE"),
    WINDOW_CHANGE("WINDOW_CHANGE"),
    UNDEFINED("UNDEFINED");

    private String id;

    UIAction(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static UIAction actionStringToEnum(String actionString) {
        switch (actionString.toUpperCase()) {
            case "CLICK":
            case "TYPE_VIEW_CLICKED":
            case "TYPE_VIEW_LONG_CLICKED": //TODO find a diff action for LONG CLICKED
            case "CHECK":
                return CLICK;
            case "EDIT_TEXT":
            case "TYPE_VIEW_TEXT_SELECTION_CHANGED":
            case "TYPE_VIEW_TEXT_CHANGED":
                return EDIT_TEXT;
            case "SCROLL":
            case "TYPE_VIEW_SCROLLED":
                return SCROLL;
            case "FOCUS":
            case "TYPE_VIEW_FOCUSED":
                return FOCUS;
            case "SELECT":
            case "TYPE_VIEW_SELECTED":
                return SELECT;
            case "POPUP":
                return POPUP;
            case "WINDOW_CHANGE":
                return WINDOW_CHANGE;
            case "TYPE_WINDOW_STATE_CHANGED":
                return WINDOW_STATE_CHANGE;
            case "CONTENT_CHANGE":
                return CONTENT_CHANGE;

        }
        return UNDEFINED;
    }

}
