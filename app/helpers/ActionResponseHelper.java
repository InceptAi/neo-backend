package helpers;

import com.inceptai.neopojos.*;
import graph.PathFinder;
import models.*;
import nlu.SimpleTextInterpreter;
import services.UIScreenManager;
import storage.SemanticActionStore;
import util.Utils;
import util.ViewUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class ActionResponseHelper {
    private UIScreenManager uiScreenManager;
    private SemanticActionStore semanticActionStore;
    private PathFinder pathFinder;

    @Inject
    public ActionResponseHelper(UIScreenManager uiScreenManager,
                                SemanticActionStore semanticActionStore,
                                PathFinder pathFinder) {
        this.uiScreenManager = uiScreenManager;
        this.semanticActionStore = semanticActionStore;
        this.pathFinder = pathFinder;
    }


    //Take input the semantic ids and best matching string and create action response
    public ActionResponse createActionResponse(String inputText, String packageName,
                                               String baseScreenTitle, String baseScreenSubTitle,
                                               DeviceInfo deviceInfo,
                                               int maxResults,
                                               boolean fuzzyScreenSearch) {
        return createActionResponse(inputText, packageName, baseScreenTitle,
                baseScreenSubTitle, deviceInfo,
                util.Utils.EMPTY_STRING, util.Utils.EMPTY_STRING,
                maxResults, fuzzyScreenSearch);

    }
    //Take input the semantic ids and best matching string and create action response
    public ActionResponse createActionResponse(String inputText, String packageName,
                                               String baseScreenTitle, String baseScreenSubTitle,
                                               DeviceInfo deviceInfo,
                                               String appVersion, String versionCode,
                                               int maxResults,
                                               boolean fuzzyScreenSearch) {
        //sanitize the input
        inputText = util.Utils.sanitizeText(inputText);
        baseScreenTitle = util.Utils.sanitizeText(baseScreenTitle);
        baseScreenSubTitle = util.Utils.sanitizeText(baseScreenSubTitle);
        MatchingInfo matchingInfo = new MatchingInfo(deviceInfo, appVersion, versionCode);
        //Make sure starting screen is not null
        UIScreen startingScreen;
        if (fuzzyScreenSearch) {
            startingScreen = uiScreenManager.findTopMatchingScreenIdByKeywordAndScreenType(
                    baseScreenTitle,
                    packageName,
                    CrawlingInput.FULL_SCREEN_MODE);
        } else {
            startingScreen = uiScreenManager.getScreen(
                    packageName,
                    baseScreenTitle,
                    baseScreenSubTitle,
                    CrawlingInput.FULL_SCREEN_MODE,
                    matchingInfo);
        }

        if (startingScreen == null) {
            return new ActionResponse();
        }
        List<ActionDetails> actionDetailsList = new ArrayList<>();
        //find top actions first
        Map<String, SemanticActionMatchingTextAndScore> topMatchingActions;
        if (matchingInfo.isEmpty() && util.Utils.nullOrEmpty(inputText)) {
            //return all semantic actions
            topMatchingActions = semanticActionStore.returnAllActionsWithScores();
        } else {
            topMatchingActions = semanticActionStore.searchActions(inputText, packageName,
                    matchingInfo, new SimpleTextInterpreter(), maxResults);
        }
        for (HashMap.Entry<String, SemanticActionMatchingTextAndScore> entry : topMatchingActions.entrySet()) {
            String actionId = entry.getKey();
            SemanticActionMatchingTextAndScore descriptionAndScore = entry.getValue();
            SemanticAction semanticAction = semanticActionStore.getAction(actionId);
            UIScreen dstScreen = uiScreenManager.getScreen(semanticAction.getUiScreenId());
            UIScreen srcScreen = uiScreenManager.getScreen(startingScreen.getId());
            if (srcScreen == null || dstScreen == null) {
                continue;
            }
            //TODO: semantic action element id may not be top level id -- so handle it
            UIScreen.UIElementTuple uiElementTuple = dstScreen.findElementAndTopLevelParentById(semanticAction.getUiElementId());
            //UIElement uiElement = dstScreen.getUiElements().get(semanticAction.getUiElementId());
            if (uiElementTuple.getTopLevelParent() == null || uiElementTuple.getUiElement() == null) {
                continue;
            }
            UIPath navigationPathBetweenScreens = pathFinder.findPathBetweenScreens(srcScreen, dstScreen);
            //Create navigation list
            List<NavigationIdentifier> navigationIdentifierList = getNavigationPathForClient(navigationPathBetweenScreens);
            //Last step
            ScreenIdentifier dstScreenIdentifier = new ScreenIdentifier(
                    dstScreen.getTitle(),
                    dstScreen.getSubTitle(),
                    dstScreen.getPackageName(),
                    dstScreen.getScreenType());

            String keywordString;
            Set<String> classNamesToSearch = new HashSet<>();
            classNamesToSearch.add(ViewUtils.LINEAR_LAYOUT_CLASS_NAME);
            classNamesToSearch.add(ViewUtils.RELATIVE_LAYOUT_CLASS_NAME);
            if (ViewUtils.isLinearOrRelativeLayoutClassName(uiElementTuple.getUiElement().getClassName())) {
                keywordString = uiElementTuple.getUiElement().fetchAllText();
            } else {
                UIElement closestParentWithLinearLayout = UIElement.findFirstParentWithGivenClassNames(
                        semanticAction.getUiElementId(),
                        classNamesToSearch,
                        uiElementTuple.getTopLevelParent());
                if (closestParentWithLinearLayout != null) {
                    keywordString = closestParentWithLinearLayout.fetchAllText();
                } else {
                    keywordString = uiElementTuple.getTopLevelParent().fetchAllText();
                }
            }

            ElementIdentifier elementIdentifier = createElementIdentifier(
                    uiElementTuple.getUiElement().getClassName(), // TODO : top level element to clickable class name
                    uiElementTuple.getUiElement().getPackageName(),
                    keywordString); //TODO: UIElement get text needs to return the text of the parent

            ActionIdentifier actionIdentifier = new ActionIdentifier(
                    dstScreenIdentifier,
                    elementIdentifier,
                    descriptionAndScore.getMatchingDescription(),
                    semanticAction.getSemanticActionType(),
                    descriptionAndScore.getConfidenceScore());

            //Create the condition to check for success
            Condition successCondition = create(semanticAction, descriptionAndScore.getMatchingDescription());
            ActionDetails actionDetails = new ActionDetails(successCondition, navigationIdentifierList, actionIdentifier);
            actionDetailsList.add(actionDetails);
        }

        return new ActionResponse(actionDetailsList);
    }

    private List<NavigationIdentifier> getNavigationPathForClient(UIPath uiPath) {
        if (uiPath == null) {
            return null;
        }
        List<NavigationIdentifier> navigationIdentifierList = new ArrayList<>();
        for (UIStep uiStep: uiPath.getUiSteps()) {
            UIScreen srcScreen = uiScreenManager.getScreen(uiStep.getSrcScreenId());
            UIScreen dstScreen = uiScreenManager.getScreen(uiStep.getDstScreenId());
            UIScreen.UIElementTuple uiElementTuple = srcScreen.findElementAndTopLevelParentById(uiStep.getUiElementId());
            UIElement uiElement = uiElementTuple != null ? uiElementTuple.getUiElement() : null;
            if (srcScreen == null || dstScreen == null || uiElementTuple == null) {
                return null;
            }
            ScreenIdentifier srcIdentifier = new ScreenIdentifier(
                    srcScreen.getTitle(),
                    srcScreen.getSubTitle(),
                    srcScreen.getPackageName(),
                    srcScreen.getScreenType());
            ScreenIdentifier dstIdentifier = new ScreenIdentifier(
                    dstScreen.getTitle(),
                    dstScreen.getSubTitle(),
                    dstScreen.getPackageName(),
                    dstScreen.getScreenType());
            ElementIdentifier elementIdentifier = createElementIdentifier(
                    uiElement.getClassName(),
                    uiElement.getPackageName(),
                    uiElement.fetchAllText()); //
            NavigationIdentifier navigationIdentifier = new NavigationIdentifier(
                    srcIdentifier,
                    dstIdentifier,
                    elementIdentifier);
            navigationIdentifierList.add(navigationIdentifier);
        }
        return navigationIdentifierList;
    }

    private static ElementIdentifier createElementIdentifier(String className, String packageName, String elementText) {
        return new ElementIdentifier(className, packageName, util.Utils.generateKeywordsForFindingElement(elementText));
    }


    private static Condition create(SemanticAction semanticAction, String matchingDescription) {
        Condition condition = null;
        switch (semanticAction.getSemanticActionType()) {
            //TODO Handle checked text view separately -- use SELECT instead of toggle and create conditions accordingly
            case SemanticAction.TOGGLE:
                if (util.Utils.containsWord(matchingDescription, ViewUtils.ON_TEXT)) {
                    condition = new Condition(ViewUtils.ON_TEXT.toLowerCase());
                } else if (util.Utils.containsWord(matchingDescription, ViewUtils.OFF_TEXT)) {
                    condition = new Condition(ViewUtils.OFF_TEXT.toLowerCase());
                } else {
                    condition = new Condition(semanticAction.getSemanticActionDescription());
                }
                break;
            case SemanticAction.SEEK:
                if (util.Utils.containsWord(matchingDescription, ViewUtils.ON_TEXT)) {
                    condition = new Condition(ViewUtils.ON_TEXT.toLowerCase());
                } else if (Utils.containsWord(matchingDescription, ViewUtils.OFF_TEXT)) {
                    condition = new Condition(ViewUtils.OFF_TEXT.toLowerCase());
                }
                break;
            default:
                break;
        }
        return condition;
    }
}
