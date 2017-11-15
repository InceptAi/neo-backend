package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inceptai.neopojos.CrawlingInput;
import models.UIScreen;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.UIScreenManager;
import helpers.UIScreenParser;
import util.Utils;

import javax.inject.Inject;
import java.util.Set;

@SuppressWarnings("unused")
public class CrawlController extends Controller {
    @Inject
    private UIScreenManager uiScreenManager;
    @Inject
    private UIScreenParser uiScreenParser;

    public Result create() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return badRequest(Utils.createResponse("Expecting Json data", false));
        }
        CrawlingInput crawlingInput = Json.fromJson(json, CrawlingInput.class);

        UIScreen screen = uiScreenParser.parseToUIScreen(crawlingInput);

        if (screen != null) {
            UIScreen createdScreen = uiScreenManager.addScreenAdvanced(screen);
            JsonNode jsonObject = Json.toJson(createdScreen);
            return created(Utils.createResponse(jsonObject, true));
        } else {
            return badRequest(Utils.createResponse("Invalid input", false));
        }
    }

    public Result update() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return badRequest(Utils.createResponse("Expecting Json data", false));
        }
        CrawlingInput crawlingInput = Json.fromJson(json, CrawlingInput.class);
        UIScreen screen = uiScreenParser.parseToUIScreen(crawlingInput);
        UIScreen updatedScreen = uiScreenManager.updateScreen(screen);
        if (updatedScreen == null) {
            return notFound(Utils.createResponse("Screen not found", false));
        }
        JsonNode jsonObject = Json.toJson(updatedScreen);
        return ok(Utils.createResponse(jsonObject, true));
    }

    public Result retrieve(String title) {
        if (uiScreenManager.getScreen(title) == null) {
            return notFound(Utils.createResponse("Screen with title:" + title + " not found", false));
        }
        JsonNode jsonObjects = Json.toJson(uiScreenManager.getScreen(title));
        return ok(Utils.createResponse(jsonObjects, true));
    }

    public Result listScreens() {
        Set<UIScreen> result = uiScreenManager.getAllScreens();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonData = mapper.convertValue(result, JsonNode.class);
        return ok(Utils.createResponse(jsonData, true));
    }

    public Result listDatabaseScreens() {
        Set<UIScreen> result = uiScreenManager.getAllScreensFromDatabase();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonData = mapper.convertValue(result, JsonNode.class);
        return ok(Utils.createResponse(jsonData, true));
    }

    public Result listPathlessScreens() {
        Set<UIScreen> result = uiScreenManager.getAllScreensWithoutPaths();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonData = mapper.convertValue(result, JsonNode.class);
        return ok(Utils.createResponse(jsonData, true));
    }
}
