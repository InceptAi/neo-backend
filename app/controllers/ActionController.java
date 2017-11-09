package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.SemanticAction;
import play.mvc.Controller;
import play.mvc.Result;
import storage.SemanticActionStore;
import util.Utils;
import views.ActionResponse;
import util.ActionResponseHelper;

import javax.inject.Inject;
import java.util.Set;

import static config.BackendConfiguration.*;

@SuppressWarnings("unused")
public class ActionController extends Controller {
    @Inject
    private ActionResponseHelper actionResponseHelper;
    @Inject
    private SemanticActionStore semanticActionStore;

    public Result listActions() {
        Set<SemanticAction> result = semanticActionStore.getAllActions();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonData = mapper.convertValue(result, JsonNode.class);
        return ok(Utils.createResponse(jsonData, true));
    }

    public Result searchActions(String inputText, String packageName, String baseScreenTitle, String deviceInfo) {

        final boolean fuzzySearch = true;
        final String subTitle = Utils.EMPTY_STRING;
        ActionResponse actionResponse = actionResponseHelper.createActionResponse(
                inputText,
                packageName,
                baseScreenTitle,
                subTitle,
                deviceInfo,
                DEFAULT_MAX_RESULTS_FOR_ACTION_SEARCH,
                fuzzySearch);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonData = mapper.convertValue(actionResponse, JsonNode.class);
        return ok(jsonData);
    }

    public Result searchSettingActions(String inputText, String deviceInfo) {
        final String SETTINGS_TITLE = "Settings";
        final String SETTINGS_PACKAGE_NAME = "com.android.settings";
        final String SETTINGS_SUBTITLE = "Wireless & networks";
        final boolean FUZZY_SEARCH = false;
        ActionResponse actionResponse = actionResponseHelper.createActionResponse(
                inputText,
                SETTINGS_PACKAGE_NAME,
                SETTINGS_TITLE,
                SETTINGS_SUBTITLE,
                deviceInfo,
                DEFAULT_MAX_RESULTS_FOR_ACTION_SEARCH,
                FUZZY_SEARCH);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonData = mapper.convertValue(actionResponse, JsonNode.class);
        return ok(jsonData);
    }

}
