package views;

import util.Utils;
import util.ViewUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class CrawlingInput {
    public static final String FULL_SCREEN_MODE = "FULL";
    public static final String PARTIAL_SCREEN_MODE = "PARTIAL";
    public static final String UNDEFINED_SCREEN_MODE = "UNDEFINED";
    private int numViews;
    private String rootSubTitle = Utils.EMPTY_STRING;
    private String lastScreenSubTitle = Utils.EMPTY_STRING;
    private String currentScreenType = Utils.EMPTY_STRING;
    private String lastScreenType = Utils.EMPTY_STRING;
    private String rootTitle = Utils.EMPTY_STRING;
    private String lastScreenTitle = Utils.EMPTY_STRING;
    private String lastScreenPackageName = Utils.EMPTY_STRING;
    private String rootPackageName = Utils.EMPTY_STRING;
    private String lastUIAction = Utils.EMPTY_STRING;
    private int rootHeight;
    private int rootWidth;
    private RenderingView lastViewClicked;
    private HashMap<String, String> deviceInfo = new HashMap<>();
    private HashMap<Long, RenderingView> viewMap = new HashMap<>() ;

    public CrawlingInput() {}

    public CrawlingInput(int numViews, int rootHeight, int rootWidth, String rootTitle,
                         String lastScreenTitle, String lastScreenPackageName,
                         RenderingView lastViewClicked, String lastUIAction,
                         String rootPackageName, HashMap<String, String> deviceInfo,
                         HashMap<Long, RenderingView> viewMap) {
        this.numViews = numViews;
        this.rootHeight = rootHeight;
        this.rootWidth = rootWidth;
        this.rootTitle = rootTitle;
        this.lastScreenTitle = lastScreenTitle;
        this.lastViewClicked = lastViewClicked;
        this.lastUIAction = lastUIAction;
        this.lastScreenPackageName = lastScreenPackageName;
        this.rootPackageName = rootPackageName;
        this.deviceInfo = deviceInfo;
        this.viewMap = viewMap;
    }


    public String getRootSubTitle() {
        String subtitle = Utils.sanitizeText(rootSubTitle);
        if (ViewUtils.isTextOnOrOff(subtitle)) {
            return ViewUtils.ON_OFF_TEXT;
        } else {
            return subtitle;
        }
    }

    public String getLastScreenSubTitle() {
        String subtitle = Utils.sanitizeText(lastScreenSubTitle);
        if (ViewUtils.isTextOnOrOff(subtitle)) {
            return ViewUtils.ON_OFF_TEXT;
        } else {
            return subtitle;
        }
    }

    public String getCurrentScreenType() {
        return currentScreenType;
    }

    public String getLastScreenType() {
        return lastScreenType;
    }

    public String getRootPackageName() {
        return rootPackageName;
    }

    public String getRootTitle() {
        return Utils.sanitizeText(rootTitle);
    }

    public int getNumViews() {
        return numViews;
    }

    public int getRootHeight() {
        return rootHeight;
    }

    public int getRootWidth() {
        return rootWidth;
    }

    public HashMap<Long, RenderingView> getViewMap() {
        return viewMap;
    }

    public List<RenderingView> getRenderingViewList() {
        return new ArrayList<RenderingView>(viewMap.values());
    }

    public boolean isEmpty() {
        return viewMap.isEmpty();
    }

    public long getParentViewId(long viewId) {
        if (viewMap.get(viewId) != null) {
            return viewMap.get(viewId).getParentViewId();
        }
        return 0;
    }

    public String getLastScreenPackageName() {
        return lastScreenPackageName;
    }

    public RenderingView getLastViewClicked() {
        return lastViewClicked;
    }

    public String getLastUIAction() {
        return Utils.sanitizeText(lastUIAction);
    }

    public String getLastScreenTitle() {
        return Utils.sanitizeText(lastScreenTitle);
    }

    public HashMap<String, String> getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(HashMap<String, String> deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    @Override
    public String toString() {
        return "CrawlingInput{" +
                "numViews=" + numViews +
                ", rootSubTitle='" + rootSubTitle + '\'' +
                ", lastScreenSubTitle='" + lastScreenSubTitle + '\'' +
                ", currentScreenType='" + currentScreenType + '\'' +
                ", lastScreenType='" + lastScreenType + '\'' +
                ", rootHeight=" + rootHeight +
                ", rootWidth=" + rootWidth +
                ", rootTitle='" + rootTitle + '\'' +
                ", lastScreenTitle='" + lastScreenTitle + '\'' +
                ", lastScreenPackageName='" + lastScreenPackageName + '\'' +
                ", lastViewClicked=" + lastViewClicked +
                ", rootPackageName='" + rootPackageName + '\'' +
                ", lastUIAction='" + lastUIAction + '\'' +
                '}';
    }
}
