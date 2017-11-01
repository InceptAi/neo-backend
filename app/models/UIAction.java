package models;

public enum UIAction {
    CLICK("CLICK"),
    EDIT_TEXT("EDIT_TEXT"),
    SCROLL("SCROLL"),
    FOCUS("FOCUS"),
    SELECT("SELECT"),
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
            case "CHECK":
                return CLICK;
            case "EDIT_TEXT":
                return EDIT_TEXT;
            case "SCROLL":
                return SCROLL;
            case "FOCUS":
                return FOCUS;
            case "SELECT":
                return SELECT;
        }
        return UNDEFINED;
    }

}
