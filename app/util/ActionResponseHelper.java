package util;

import graph.PathFinder;
import models.*;
import nlu.SimpleTextInterpreter;
import storage.SemanticActionStore;
import storage.UIScreenStore;
import views.*;

import java.util.*;

public class ActionResponseHelper {

    //Take input the semantic ids and best matching string and create action response
    public static ActionResponse createActionResponse(String inputText, String packageName,
                                                      String baseScreenTitle, String baseScreenSubTitle,
                                                      String deviceInfo, PathFinder pathFinder,
                                                      int maxResults, boolean fuzzyScreenSearch) {
        //sanitize the input
        inputText = Utils.sanitizeText(inputText);
        deviceInfo = Utils.sanitizeText(deviceInfo);
        baseScreenTitle = Utils.sanitizeText(baseScreenTitle);
        baseScreenSubTitle = Utils.sanitizeText(baseScreenSubTitle);
        //Make sure starting screen is not null
        UIScreen startingScreen;
        if (fuzzyScreenSearch) {
            startingScreen = UIScreenStore.getInstance().findTopMatchingScreenIdByKeywordAndScreenType(
                    baseScreenTitle,
                    packageName,
                    CrawlingInput.FULL_SCREEN_MODE);
        } else {
            startingScreen = UIScreenStore.getInstance().getScreen(
                    packageName,
                    baseScreenTitle,
                    baseScreenSubTitle,
                    CrawlingInput.FULL_SCREEN_MODE,
                    deviceInfo);
        }

        if (startingScreen == null) {
            return new ActionResponse();
        }
        List<ActionDetails> actionDetailsList = new ArrayList<>();
        //find top actions first
        HashMap<String, SemanticActionStore.SemanticActionMatchingTextAndScore> topMatchingActions;
        if (Utils.nullOrEmpty(deviceInfo) && Utils.nullOrEmpty(inputText)) {
            //return all semantic actions
            topMatchingActions = SemanticActionStore.getInstance().returnAllActions();
        } else {
            topMatchingActions = SemanticActionStore.getInstance().searchActions(inputText,
                    deviceInfo, new SimpleTextInterpreter(), maxResults);
        }
        for (HashMap.Entry<String, SemanticActionStore.SemanticActionMatchingTextAndScore> entry : topMatchingActions.entrySet()) {
            String actionId = entry.getKey();
            SemanticActionStore.SemanticActionMatchingTextAndScore descriptionAndScore = entry.getValue();
            SemanticAction semanticAction = SemanticActionStore.getInstance().getAction(actionId);
            UIScreen dstScreen = UIScreenStore.getInstance().getScreen(semanticAction.getUiScreenId());
            UIScreen srcScreen = UIScreenStore.getInstance().getScreen(startingScreen.getId());
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

            String keywordString = Utils.EMPTY_STRING;
            Set<String> classNamesToSearch = new HashSet<>();
            classNamesToSearch.add(ViewUtils.LINEAR_LAYOUT_CLASS_NAME);
            classNamesToSearch.add(ViewUtils.RELATIVE_LAYOUT_CLASS_NAME);
            if (ViewUtils.isLinearOrRelativeLayoutClassName(uiElementTuple.getUiElement().getClassName())) {
                keywordString = uiElementTuple.getUiElement().getAllText();
            } else {
                UIElement closestParentWithLinearLayout = UIElement.findFirstParentWithGivenClassNames(
                        semanticAction.getUiElementId(),
                        classNamesToSearch,
                        uiElementTuple.getTopLevelParent());
                if (closestParentWithLinearLayout != null) {
                    keywordString = closestParentWithLinearLayout.getAllText();
                } else {
                    keywordString = uiElementTuple.getTopLevelParent().getAllText();
                }
            }

//            ElementIdentifier elementIdentifier = createElementIdentifier(
//                    uiElementTuple.getUiElement().getClassName(), // TODO : top level element to clickable class name
//                    uiElementTuple.getUiElement().getPackageName(),
//                    uiElementTuple.getTopLevelParent().getAllText()); //TODO: UIElement gettext needs to return the text of the parent

            ElementIdentifier elementIdentifier = createElementIdentifier(
                    uiElementTuple.getUiElement().getClassName(), // TODO : top level element to clickable class name
                    uiElementTuple.getUiElement().getPackageName(),
                    keywordString); //TODO: UIElement gettext needs to return the text of the parent

            ActionIdentifier actionIdentifier = new ActionIdentifier(
                    dstScreenIdentifier,
                    elementIdentifier,
                    descriptionAndScore.getMatchingDescription(),
                    semanticAction.getSemanticActionName(),
                    descriptionAndScore.getConfidenceScore());

            //Create the condition to check for success
            Condition successCondition = create(semanticAction, descriptionAndScore.getMatchingDescription());
            ActionDetails actionDetails = new ActionDetails(successCondition, navigationIdentifierList, actionIdentifier);
            actionDetailsList.add(actionDetails);
        }

        return new ActionResponse(actionDetailsList);
    }

    public static List<NavigationIdentifier> getNavigationPathForClient(UIPath uiPath) {
        //TODO add the nested element support here too -- just like action identifier
        if (uiPath == null) {
            return null;
        }
        List<NavigationIdentifier> navigationIdentifierList = new ArrayList<>();
        for (UIStep uiStep: uiPath.getUiSteps()) {
            UIScreen srcScreen = UIScreenStore.getInstance().getScreen(uiStep.getSrcScreenId());
            UIScreen dstScreen = UIScreenStore.getInstance().getScreen(uiStep.getDstScreenId());
            //UIElement uiElement = srcScreen.getUiElements().get(uiStep.getUiElementId());
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
                    uiElement.getAllText()); //
            NavigationIdentifier navigationIdentifier = new NavigationIdentifier(srcIdentifier, dstIdentifier,
                    elementIdentifier, uiStep.getUiActionId());
            navigationIdentifierList.add(navigationIdentifier);
        }
        return navigationIdentifierList;
    }

    public static ElementIdentifier createElementIdentifier(String className, String packageName, String elementText) {
        return new ElementIdentifier(className, packageName, Utils.generateKeywordsForFindingElement(elementText));
    }


    public static Condition create(SemanticAction semanticAction, String matchingDescription) {
        Condition condition = null;
        switch (SemanticActionType.typeStringToEnum(semanticAction.getSemanticActionName())) {
            case TOGGLE:
                if (Utils.containsWord(matchingDescription, ViewUtils.ON_TEXT)) {
                    condition = new Condition(ViewUtils.ON_TEXT.toLowerCase());
                } else if (Utils.containsWord(matchingDescription, ViewUtils.OFF_TEXT)) {
                    condition = new Condition(ViewUtils.OFF_TEXT.toLowerCase());
                }
                break;
            case SEEK:
                if (Utils.containsWord(matchingDescription, ViewUtils.ON_TEXT)) {
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
