package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inceptai.neopojos.ActionResponse;
import com.inceptai.neopojos.CrawlingInput;
import com.inceptai.neopojos.DeviceInfo;
import models.SemanticAction;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import storage.SemanticActionStore;
import helpers.ActionResponseHelper;
import util.Utils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

import static config.BackendConfiguration.DEFAULT_MAX_RESULTS_FOR_ACTION_SEARCH;
import static util.Utils.SETTINGS_PACKAGE_NAME;
import static util.Utils.SETTINGS_SUBTITLE;
import static util.Utils.SETTINGS_TITLE;

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

    public Result searchActions(String inputText, String packageName, String baseScreenTitle,
                                String baseScreenType, String deviceInfoString, String appVersion,
                                String versionCode) {
        final boolean fuzzySearch = true;
        final String subTitle = Utils.EMPTY_STRING;
        DeviceInfo deviceInfo = getDeviceInfoFromInputString(deviceInfoString);
        if (deviceInfo == null) {
            return badRequest(Utils.createResponse("Invalid input", false));
        }

        ActionResponse actionResponse = actionResponseHelper.createActionResponse(
                inputText,
                packageName,
                baseScreenTitle,
                subTitle,
                baseScreenType,
                deviceInfo,
                appVersion,
                versionCode,
                DEFAULT_MAX_RESULTS_FOR_ACTION_SEARCH,
                fuzzySearch);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonData = mapper.convertValue(actionResponse, JsonNode.class);
        return ok(jsonData);
    }

    public Result searchSettingActions(String inputText, String deviceInfoString) {

        final boolean FUZZY_SEARCH = true;
        DeviceInfo deviceInfo = getDeviceInfoFromInputString(deviceInfoString);
        if (deviceInfo == null) {
            return badRequest(Utils.createResponse("Invalid input", false));
        }
        ActionResponse actionResponse = actionResponseHelper.createActionResponse(
                inputText,
                SETTINGS_PACKAGE_NAME,
                SETTINGS_TITLE,
                SETTINGS_SUBTITLE,
                CrawlingInput.FULL_SCREEN_MODE,
                deviceInfo,
                deviceInfo.getRelease(),
                deviceInfo.getSdk(),
                DEFAULT_MAX_RESULTS_FOR_ACTION_SEARCH,
                FUZZY_SEARCH);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonData = mapper.convertValue(actionResponse, JsonNode.class);
        return ok(jsonData);
    }

    private static DeviceInfo getDeviceInfoFromInputString(String deviceInfoString) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode deviceInfoObj = null;
        try {
            deviceInfoObj = mapper.readTree(deviceInfoString);
        } catch (IOException e) {
            Utils.printDebug("Exception while parsing device info " + e.toString());
        }

        if (deviceInfoObj == null) {
            return null;
        }
        return Json.fromJson(deviceInfoObj, DeviceInfo.class);
    }


}
