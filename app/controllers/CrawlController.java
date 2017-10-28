package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.CrawlingInputParser;
import views.CrawlingInput;
import models.SemanticAction;
import models.UIScreen;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import storage.SemanticActionStore;
import storage.UIScreenStore;
import util.Utils;

import java.util.Set;

public class CrawlController extends Controller {
    public Result create() {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return badRequest(Utils.createResponse("Expecting Json data", false));
        }
        CrawlingInput crawlingInput = Json.fromJson(json, CrawlingInput.class);
        UIScreen screen = CrawlingInputParser.parseCrawlingInput(crawlingInput);
        if (screen != null) {
            UIScreen createdScreen = UIScreenStore.getInstance().addScreen(screen);
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
        UIScreen screen = CrawlingInputParser.parseCrawlingInput(crawlingInput);
        UIScreen updatedScreen = UIScreenStore.getInstance().updateScreen(screen);
        if (updatedScreen == null) {
            return notFound(Utils.createResponse("Screen not found", false));
        }
        JsonNode jsonObject = Json.toJson(updatedScreen);
        return ok(Utils.createResponse(jsonObject, true));
    }

    public Result retrieve(String title) {
        if (UIScreenStore.getInstance().getScreen(title) == null) {
            return notFound(Utils.createResponse("Screen with title:" + title + " not found", false));
        }
        JsonNode jsonObjects = Json.toJson(UIScreenStore.getInstance().getScreen(title));
        return ok(Utils.createResponse(jsonObjects, true));
    }

    public Result listScreens() {
        Set<UIScreen> result = UIScreenStore.getInstance().getAllScreens();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonData = mapper.convertValue(result, JsonNode.class);
        return ok(Utils.createResponse(jsonData, true));
    }

    public Result delete(String packageName, String title, String deviceInfo) {
        if (!UIScreenStore.getInstance().deleteScreen(UIScreen.getScreenId(packageName, title, deviceInfo))) {
            return notFound(Utils.createResponse("Screen with title:" + title + " not found", false));
        }
        return ok(Utils.createResponse("Screen with getId:" + title + " deleted", true));
    }
}
