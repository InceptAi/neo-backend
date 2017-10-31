package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graph.SimplePathFinder;
import models.SemanticAction;
import play.mvc.Controller;
import play.mvc.Result;
import storage.SemanticActionStore;
import util.Utils;
import views.ActionResponse;
import util.ActionResponseHelper;

import java.util.Set;

public class ActionController extends Controller {
    public static final String SETTINGS_TITLE = "Settings";
    public static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    public static final String SETTINGS_SUBTITLE = "Wireless & networks";
    private static final int DEFAULT_MAX_RESULTS_FOR_ACTION_SEARCH = 10;

    public Result listActions() {
        Set<SemanticAction> result = SemanticActionStore.getInstance().getAllActions();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonData = mapper.convertValue(result, JsonNode.class);
        return ok(Utils.createResponse(jsonData, true));
    }

    public Result searchActions(String inputText, String packageName, String baseScreenTitle, String deviceInfo) {
        final boolean fuzzySearch = true;
        final String subTitle = Utils.EMPTY_STRING;
        ActionResponse actionResponse = ActionResponseHelper.createActionResponse(
                inputText,
                packageName,
                baseScreenTitle,
                subTitle,
                deviceInfo,
                new SimplePathFinder(),
                DEFAULT_MAX_RESULTS_FOR_ACTION_SEARCH,
                fuzzySearch);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonData = mapper.convertValue(actionResponse, JsonNode.class);
        return ok(jsonData);
        //return ok(Utils.createResponse(jsonData, true));
    }

    public Result searchSettingActions(String inputText, String deviceInfo) {
        final boolean fuzzySearch = false;
        ActionResponse actionResponse = ActionResponseHelper.createActionResponse(
                inputText,
                SETTINGS_PACKAGE_NAME,
                SETTINGS_TITLE,
                SETTINGS_SUBTITLE,
                deviceInfo, new SimplePathFinder(),
                DEFAULT_MAX_RESULTS_FOR_ACTION_SEARCH,
                fuzzySearch);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonData = mapper.convertValue(actionResponse, JsonNode.class);
        return ok(jsonData);
        //return ok(Utils.createResponse(jsonData, true));
    }

}
