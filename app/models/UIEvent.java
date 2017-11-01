package models;

public enum UIEvent {
    TYPE_VIEW_CLICKED("TYPE_VIEW_CLICKED"),
    TYPE_VIEW_LONG_CLICKED("TYPE_VIEW_LONG_CLICKED"),
    TYPE_VIEW_TEXT_SELECTION_CHANGED("TYPE_VIEW_TEXT_SELECTION_CHANGED"),
    TYPE_VIEW_TEXT_CHANGED("TYPE_VIEW_TEXT_CHANGED"),
    TYPE_VIEW_SCROLLED("TYPE_VIEW_SCROLLED"),
    TYPE_VIEW_FOCUSED("TYPE_VIEW_FOCUSED"),
    TYPE_VIEW_SELECTED("TYPE_VIEW_SELECTED"),
    TYPE_WINDOW_STATE_CHANGED("TYPE_WINDOW_STATE_CHANGED"),
    TYPE_WINDOW_CONTENT_CHANGED("TYPE_WINDOW_CONTENT_CHANGED"),
    TYPE_WINDOWS_CHANGED("TYPE_WINDOWS_CHANGED"),
    UNDEFINED("UNDEFINED");

    private String id;

    UIEvent(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static UIEvent eventStringToEnum(String actionString) {
        switch (actionString.toUpperCase()) {
            case "TYPE_VIEW_CLICKED":
                return TYPE_VIEW_CLICKED;
            case "TYPE_VIEW_LONG_CLICKED":
                return TYPE_VIEW_LONG_CLICKED;
            case "TYPE_VIEW_TEXT_SELECTION_CHANGED":
                return TYPE_VIEW_TEXT_SELECTION_CHANGED;
            case "TYPE_VIEW_TEXT_CHANGED":
                return TYPE_VIEW_TEXT_CHANGED;
            case "TYPE_VIEW_SCROLLED":
                return TYPE_VIEW_SCROLLED;
            case "TYPE_VIEW_FOCUSED":
                return TYPE_VIEW_FOCUSED;
            case "TYPE_VIEW_SELECTED":
                return TYPE_VIEW_SELECTED;
            case "TYPE_WINDOW_STATE_CHANGED":
                return TYPE_WINDOW_STATE_CHANGED;
            case "TYPE_WINDOW_CONTENT_CHANGED":
                return TYPE_WINDOW_CONTENT_CHANGED;
            case "TYPE_WINDOWS_CHANGED":
                return TYPE_WINDOWS_CHANGED;
        }
        return UNDEFINED;
    }
}
