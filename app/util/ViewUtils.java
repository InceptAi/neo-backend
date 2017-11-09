package util;

import views.RenderingView;

import java.util.HashMap;
import java.util.Map;

public class ViewUtils {
    public static final String TEXT_VIEW_CLASS_NAME = "android.widget.TextView";
    public static final String IMAGE_BUTTON_CLASS_NAME = "android.widget.ImageButton";
    public static final String IMAGE_VIEW_CLASS_NAME = "android.widget.ImageView";
    public static final String LINEAR_LAYOUT_CLASS_NAME = "android.widget.LinearLayout";
    public static final String FRAME_LAYOUT_CLASS_NAME = "android.widget.FrameLayout";
    public static final String RELATIVE_LAYOUT_CLASS_NAME = "android.widget.RelativeLayout";
    public static final String CHECK_BOX_CLASS_NAME = "android.widget.CheckBox";
    public static final String SWITCH_CLASS_NAME = "android.widget.Switch";
    public static final String SEEK_BAR_CLASS_NAME = "android.widget.SeekBar";
    public static final String CHECKED_TEXT_VIEW_CLASS_NAME = "android.widget.CheckedTextView";
    public static final String EDIT_TEXT_VIEW_CLASS_NAME = "android.widget.EditText";
    public static final String BUTTON_CLASS_NAME = "android.widget.Button";
    public static final String RADIO_BUTTON_CLASS_NAME = "android.widget.RadioButton";
    public static final String TOGGLE_BUTTON_CLASS_NAME = "android.widget.ToggleButton";


    @SuppressWarnings("unused")
    private static final String VIEWPAGER_CLASS = "ViewPager";
    private static final String NULL_STRING = "null";

    //On/off
    static final String ON_TEXT = "on";
    static final String OFF_TEXT = "off";

    //Texts
    public static final String SWITCH_TEXT = "SWITCH_ON_OFF";
    public static final String ON_OFF_TEXT = "TEXT_ON_OFF";
    public static final String CHECK_BOX_TEXT = "CHECK_BOX_ON_OFF";
    public static final String SEEK_BAR_TEXT = "SEEK_BAR_VALUE";
    public static final String EDIT_TEXT_VIEW_TEXT = "EDIT_TEXT_VIEW_TEXT";
    private static final String MULTIPLE_WORD_MATCH_DELIMITER = "#";
    public static final String ON_OFF_KEYWORD_REPLACEMENT = ON_TEXT + MULTIPLE_WORD_MATCH_DELIMITER + OFF_TEXT;

    @SuppressWarnings("unused")
    public static final HashMap<String , String> ON_MAP = new HashMap<String , String>() {{
        put(ViewUtils.SWITCH_TEXT,    "on");
        put(ViewUtils.ON_OFF_TEXT, "on");
    }};

    @SuppressWarnings("unused")
    public static final HashMap<String , String> OFF_MAP = new HashMap<String , String>() {{
        put(ViewUtils.SWITCH_TEXT,    "off");
        put(ViewUtils.ON_OFF_TEXT, "off");
    }};

    @SuppressWarnings("unused")
    public static final HashMap<String , String> ENABLE_MAP = new HashMap<String , String>() {{
        put(ViewUtils.SWITCH_TEXT,    "enable");
        put(ViewUtils.ON_OFF_TEXT, "enable");
    }};

    @SuppressWarnings("unused")
    public static final HashMap<String , String> DISABLE_MAP = new HashMap<String , String>() {{
        put(ViewUtils.SWITCH_TEXT,    "disable");
        put(ViewUtils.ON_OFF_TEXT, "disable");
    }};


    private ViewUtils() {}

    public static HashMap<String, String> getMapForOnOffTemplateReplacement(String replacementText) {
        if (Utils.nullOrEmpty(replacementText)) {
            return new HashMap<>();
        }

        return new HashMap<String , String>() {{
            put(ViewUtils.SWITCH_TEXT.trim().toLowerCase(), replacementText.toLowerCase());
            put(ViewUtils.ON_OFF_TEXT.trim().toLowerCase(), replacementText.toLowerCase());
            put(ViewUtils.CHECK_BOX_TEXT.trim().toLowerCase(), replacementText.toLowerCase());
        }};
    }

    private static boolean isTextView(RenderingView renderingView) {
        return TEXT_VIEW_CLASS_NAME.equals(renderingView.getClassName());
    }

    public static boolean isImage(RenderingView renderingView) {
        return IMAGE_BUTTON_CLASS_NAME.equals(renderingView.getClassName());
    }

    public static boolean isNotNullValuedString(String target) {
        return !NULL_STRING.equals(target);
    }

    public static boolean isLinearRelativeOrFrameLayout(RenderingView renderingView) {
        String className = renderingView.getClassName();
        return LINEAR_LAYOUT_CLASS_NAME.equals(className) || RELATIVE_LAYOUT_CLASS_NAME.equals(className) || FRAME_LAYOUT_CLASS_NAME.equals(className);
    }

    public static boolean isLinearOrRelativeLayoutClassName(String className) {
        return LINEAR_LAYOUT_CLASS_NAME.equalsIgnoreCase(className) || RELATIVE_LAYOUT_CLASS_NAME.equalsIgnoreCase(className);
    }

    public static boolean isCheckBox(RenderingView renderingView) {
        return CHECK_BOX_CLASS_NAME.equals(renderingView.getClassName());
    }

    public static String getTextBasedOnClass(String className, String text) {
        //TODO -- return all text in default cases
        String textToReturn = Utils.EMPTY_STRING;
        switch (className) {

            case ViewUtils.SWITCH_CLASS_NAME:
            case ViewUtils.TOGGLE_BUTTON_CLASS_NAME:
                textToReturn = SWITCH_TEXT;
                break;

            case ViewUtils.SEEK_BAR_CLASS_NAME:
                textToReturn = SEEK_BAR_TEXT;
                break;

            case ViewUtils.CHECK_BOX_CLASS_NAME:
                textToReturn = CHECK_BOX_TEXT;
                break;

            case ViewUtils.EDIT_TEXT_VIEW_CLASS_NAME:
                textToReturn = EDIT_TEXT_VIEW_TEXT;
                break;

            case ViewUtils.TEXT_VIEW_CLASS_NAME:
                textToReturn = ViewUtils.isTextOnOrOff(text) ? ON_OFF_TEXT : text;
                break;

            case ViewUtils.IMAGE_BUTTON_CLASS_NAME:
            case ViewUtils.CHECKED_TEXT_VIEW_CLASS_NAME:
            case ViewUtils.RADIO_BUTTON_CLASS_NAME:
            case ViewUtils.IMAGE_VIEW_CLASS_NAME:
            case ViewUtils.BUTTON_CLASS_NAME:
                textToReturn = text;
                break;

            case ViewUtils.FRAME_LAYOUT_CLASS_NAME:
            case ViewUtils.RELATIVE_LAYOUT_CLASS_NAME:
            case ViewUtils.LINEAR_LAYOUT_CLASS_NAME:
            default:
                break;
        }
        return textToReturn;
    }

    public static boolean isTemplateText(String text) {
        String[] templateTexts = {SWITCH_TEXT, SEEK_BAR_TEXT, EDIT_TEXT_VIEW_TEXT, CHECK_BOX_TEXT};
        for (String templateText: templateTexts) {
            if (templateText.trim().equalsIgnoreCase(text.trim())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTextOnOrOff(String text) {
        return text.toLowerCase().equals(ON_TEXT.toLowerCase())
                || text.toLowerCase().equals(OFF_TEXT.toLowerCase());
    }

    public static boolean isToggleable(String className) {
        return className.equalsIgnoreCase(SWITCH_CLASS_NAME) ||
                className.equalsIgnoreCase(CHECK_BOX_CLASS_NAME) ||
                className.equalsIgnoreCase(CHECKED_TEXT_VIEW_CLASS_NAME) ||
                className.equalsIgnoreCase(RADIO_BUTTON_CLASS_NAME) ||
                className.equalsIgnoreCase(TOGGLE_BUTTON_CLASS_NAME);
    }

    public static String getTitleFromView(HashMap<Long, RenderingView> renderingViewHashMap) {
        long topTextViewYCoordinate = Long.MAX_VALUE;
        long topTextViewXCoordinate = Long.MAX_VALUE;
        String titleToReturn = Utils.EMPTY_STRING;
        for (Map.Entry<Long, RenderingView> entry : renderingViewHashMap.entrySet()) {
            RenderingView renderingView = entry.getValue();
            if (ViewUtils.isTextView(renderingView) &&
                    renderingView.getTopY() < topTextViewYCoordinate ||
                    (renderingView.getTopY() == topTextViewYCoordinate && renderingView.getLeftX() < topTextViewXCoordinate)) {
                topTextViewYCoordinate = renderingView.getTopY();
                topTextViewXCoordinate = renderingView.getLeftX();
                titleToReturn = renderingView.getText();
            }
        }
        return titleToReturn;
    }


}

