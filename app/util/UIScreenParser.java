package util;

import com.inceptai.neopojos.CrawlingInput;
import com.inceptai.neopojos.RenderingView;
import config.BackendConfiguration;
import models.*;
import services.UIScreenManager;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static com.inceptai.neopojos.CrawlingInput.FULL_SCREEN_MODE;
import static com.inceptai.neopojos.CrawlingInput.PARTIAL_SCREEN_MODE;

@Singleton
public class UIScreenParser {
    private static final String LOADING_SUBTITLE = "LOADING";
    private static final String COMPUTING_SUBTITLE = "COMPUTING";
    private UIScreenManager uiScreenManager;

    @Inject
    public UIScreenParser(UIScreenManager uiScreenManager) {
        this.uiScreenManager = uiScreenManager;
    }

    public UIScreen parseToUIScreen(CrawlingInput crawlingInput) {
        if (!shouldProcess(crawlingInput) || Utils.nullOrEmpty(crawlingInput.getRootTitle())) {
            return null;
        }

        String sanitizedSubtitle = getSanitizedSubTitle(crawlingInput.getRootSubTitle());
        UIScreen screenToBeCreated = new UIScreen(
                crawlingInput.getRootTitle(),
                !isSubtitlePlaceholderText(sanitizedSubtitle) ? sanitizedSubtitle : Utils.EMPTY_STRING,
                crawlingInput.getRootPackageName(),
                crawlingInput.getCurrentScreenType(),
                crawlingInput.getDeviceInfo());

        List<Long> sortedViewIds= new ArrayList<>(crawlingInput.getViewMap().keySet());
        Collections.sort(sortedViewIds);

        HashMap<Long, UIElement> viewIdToUIElementMap = new HashMap<>();
        for (Long viewId: sortedViewIds) {
            RenderingView renderingView = crawlingInput.getViewMap().get(viewId);
            UIElement uiElement = createUIElementFromRenderingView(renderingView);
            viewIdToUIElementMap.put(renderingView.getFlatViewId(), uiElement);
        }

        //Second pass to assign created elements
        List<UIElement> topLevelElementList = new ArrayList<>();
        List<UIElement> allActionableElementsList = new ArrayList<>();
        List<UIElement> parentOfActionableElementsList = new ArrayList<>();

        for (Long viewId: sortedViewIds) {
            RenderingView renderingView = crawlingInput.getViewMap().get(viewId);
            UIElement currentElement = viewIdToUIElementMap.get(renderingView.getFlatViewId());
            //parent info
            RenderingView parentView = crawlingInput.getViewMap().get(renderingView.getParentViewId());
            UIElement parentElement = viewIdToUIElementMap.get(renderingView.getParentViewId());
            if (parentElement != null &&
                    parentView != null &&
                    (parentView.isClickable() || parentView.isParentOfClickableView())) {
                parentElement.addChildren(currentElement);
            } else {
                topLevelElementList.add(currentElement);
            }

            //Only handle clickable elements
            if (currentElement.checkIsClickable()) {
                allActionableElementsList.add(currentElement);
                parentOfActionableElementsList.add(parentElement);
            }
        }

        for (UIElement uiElement: topLevelElementList) {
            uiElement.finalizeChildElementIds();
            screenToBeCreated.add(uiElement);
        }

        //Set semantic actions here -- we should only add for stuff that doesn't have navigational action
        for (int elementIndex = 0; elementIndex < allActionableElementsList.size(); elementIndex++) {
            UIElement uiElement = allActionableElementsList.get(elementIndex);
            UIElement parentElement = parentOfActionableElementsList.get(elementIndex);
            updateSemanticActions(screenToBeCreated, uiElement, parentElement);
        }

        //Create UIScreen / UIElement / UIAction from last stuff
        UIStep uiStep = getLastUIStep(
                screenToBeCreated,
                crawlingInput.getLastScreenTitle(),
                getSanitizedSubTitle(crawlingInput.getLastScreenSubTitle()),
                crawlingInput.getLastScreenPackageName(),
                crawlingInput.getLastScreenType(),
                crawlingInput.getLastViewClicked(),
                crawlingInput.getLastUIAction());

        processLastUIStep(screenToBeCreated, uiStep);
        return screenToBeCreated;
    }

    private void processLastUIStep(UIScreen screenToBeCreated, UIStep uiStep) {
        //Add the uiStep to lastScreen's UIPath and assign to current screen
        if (screenToBeCreated == null || uiStep == null ||
                uiStep.checkIfUndefined() || Utils.nullOrEmpty(uiStep.getSrcScreenId())) {
            return;
        }

        Utils.printDebug("Last UI Step is " + uiStep.toString());

        String lastScreenId = uiStep.getSrcScreenId();
        UIScreen lastScreen = uiScreenManager.getScreen(lastScreenId);

        if (lastScreen == null) {
            return;
        }

        if (uiStep.checkIfInterScreenStep() || uiStep.checkIfSoftStep()) { //navigational step
            Utils.printDebug("Navigational UI Step");
            UIScreen.UIElementTuple lastElementTuple = lastScreen.findElementAndTopLevelParentById(uiStep.getUiElementId());
            if (lastElementTuple == null || lastElementTuple.getUiElement() == null) {
                return;
            }
            UIElement lastElement = lastElementTuple.getUiElement();
            Utils.printDebug("Adding navigational step to element: " + lastElement.toString());
            lastElement.add(new NavigationalAction(uiStep.getUiEventId(), screenToBeCreated.getId()));
            Utils.printDebug("Adding uiStep to lastPaths");
            lastScreen.addUIStepForDestinationScreen(screenToBeCreated.getId(), uiStep);
            screenToBeCreated.addUIStepToCurrentScreen(uiStep);
            //Add subScreen
            if (screenToBeCreated.getScreenType().equalsIgnoreCase(PARTIAL_SCREEN_MODE) &&
                    lastScreen.getScreenType().equalsIgnoreCase(FULL_SCREEN_MODE)) {
                lastScreen.addChildScreen(screenToBeCreated);
                screenToBeCreated.setParentScreenId(lastScreenId);
                Utils.printDebug("Adding screen " +
                        screenToBeCreated.toString() +
                        " as child of " + lastScreen.toString());
            }
        } else if (uiStep.checkIfWithinSameScreen()) {
            Utils.printDebug("Within Screen UI Step");
            List<String> differingElementIds = MergeUtils.getDifferingUIElementIdsInCurrentScreen(
                    screenToBeCreated.getUiElements().keySet(),
                    lastScreen.getUiElements().keySet());
            for (String elementId: differingElementIds) {
                UIElement differingElement = screenToBeCreated.getUiElements().get(elementId);
                if (differingElement != null) {
                    Utils.printDebug("Adding last step to get to element: " + differingElement.toString());
                    differingElement.add(uiStep);
                }
            }
        }
    }

    private UIStep getLastUIStep(UIScreen currentScreen,
                                 String lastScreenTitle,
                                 String lastScreenSubTitle,
                                 String lastScreenPackageName,
                                 String lastScreenType,
                                 RenderingView lastViewClicked,
                                 String lastAction) {
        final boolean PRIORITIZE_ELEMENTS_BASED_ON_TITLE = true;

        //Create the last step in UIPath
        //Add navigational action to the last UI Element
        UIStep undefinedUIStep = new UIStep();
        if (lastViewClicked == null) {
            Utils.printDebug("Error in getLastUIStep: lastViewClicked Is null");
            return undefinedUIStep;
        }

        if (Utils.nullOrEmpty(lastScreenPackageName) || Utils.nullOrEmpty(lastViewClicked.getPackageName())) {
            Utils.printDebug("Error in getLastUIStep: empty lastPkg: or lastViewClickedPkg");
            return undefinedUIStep;
        }

        //If there is no text in the clicked view, we can't tell anything so its pointless
        //TODO: We should only take into account TYPE_VIEW_CLICKED events ?
        if (Utils.nullOrEmpty(lastViewClicked.getOverallText())) {
            Utils.printDebug("Error In getLastUIStep: empty text for lastViewClicked");
            return undefinedUIStep;
        }

        //Check if the text of lastViewClicked matches with
        UIScreen lastScreen =  null;
        UIElement lastElement = null;
        UIEvent lastUIEvent = UIEvent.eventStringToEnum(lastAction);
        String uiStepType;
        String lastScreenId = Utils.EMPTY_STRING;

        if (!Utils.nullOrEmpty(lastScreenTitle) && !Utils.nullOrEmpty(lastScreenPackageName)) {
            lastScreenId = UIScreen.getScreenId(
                    lastScreenPackageName,
                    lastScreenTitle,
                    lastScreenSubTitle,
                    lastScreenType,
                    currentScreen.getDeviceInfo().toString());
            //lastScreen = UIScreenManager.getInstance().getScreen(lastScreenId);
            lastScreen = uiScreenManager.getScreen(lastScreenId);
        }

        if (lastScreen == null) { // handle this case later
            Utils.printDebug("Error In getLastUIStep: lastScreenId not found with title/pkg " + lastScreenTitle + " / " + lastScreenPackageName);
            return undefinedUIStep;
        }

        //We need fuzzy matching for window state changed as text is Advanced Wi-Fi while in menu text is Advanced
        boolean isFuzzySearchNeeded = lastUIEvent.equals(UIEvent.TYPE_WINDOW_STATE_CHANGED) &&
                lastScreen.getScreenType().equalsIgnoreCase(PARTIAL_SCREEN_MODE);

        if (lastScreenId.equalsIgnoreCase(currentScreen.getId())) {
            uiStepType = UIStep.WITHIN_SAME_SCREEN;
        } else if (isFuzzySearchNeeded){
            uiStepType = UIStep.SOFT_STEP_INTER_SCREEN;
        } else {
            uiStepType = UIStep.TO_ANOTHER_SCREEN;
        }

        HashMap<String, List<UIElement>> matchingScreenAndElementsHashMap =
                lastScreen.findElementsInScreenHierarchical(
                lastViewClicked.getClassName(),
                lastViewClicked.getPackageName(),
                lastViewClicked.getOverallText(),
                true,
                isFuzzySearchNeeded,
                PRIORITIZE_ELEMENTS_BASED_ON_TITLE);

        List<UIElement> matchingElements = new ArrayList<>();
        String screenIdForMatchingElement = Utils.EMPTY_STRING;
        if (matchingScreenAndElementsHashMap.size() == 1) {
            screenIdForMatchingElement = (String)matchingScreenAndElementsHashMap.keySet().toArray()[0];
            matchingElements = matchingScreenAndElementsHashMap.get(screenIdForMatchingElement);
        }


        //If more than one, take the top one for now
        // TODO sort the elements based on level of text matching --
        // TODO more matching means higher score so we select the element that matches the most.

        if (matchingElements.size() == 1 ||
                (BackendConfiguration.GET_TOP_ELEMENT_IF_MULTIPLE_MATCHES && matchingElements.size() > 1)) {
            lastElement = matchingElements.get(0);
        }

        if (lastElement == null) {
            Utils.printDebug("Error in getLastUIStep: last element is null");
            return undefinedUIStep;
        }

        //Create a UI Step and add to the path
        return new UIStep(screenIdForMatchingElement, currentScreen.getId(), lastElement.getId(), lastUIEvent.id(), uiStepType);
    }

    private static UIElement createUIElementFromRenderingView(RenderingView renderingView) {
        String primaryText = renderingView.getOverallText();
        String className = renderingView.getClassName();
        String packageName = renderingView.getPackageName();
        String textBasedOnClassName = ViewUtils.getTextBasedOnClass(className, primaryText);
        UIElement uiElement = new UIElement(
                className,
                packageName,
                textBasedOnClassName,
                ViewUtils.isToggleable(className));
        //Add UIActions based on view
        if (renderingView.isClickable() || renderingView.isCheckable()) {
            uiElement.add(UIAction.CLICK);
        }
        if (!Utils.nullOrEmpty(renderingView.getViewIdResourceName())) {
            uiElement.setResourceType(renderingView.getViewIdResourceName());
        }
        uiElement.setCoordinates(
                renderingView.getLeftX(),
                renderingView.getTopY(),
                renderingView.getRightX(),
                renderingView.getBottomY());
        return uiElement;
    }

    private void updateSemanticActions(UIScreen uiScreen, UIElement uiElement, @Nullable UIElement parentElement) {
        if (uiScreen == null || uiElement == null) {
            return;
        }
        HashMap<String, SemanticAction> semanticActionHashMap = uiElement.getSemanticActions();
        for (UIAction uiAction: uiElement.getUiActions()) {
            if (semanticActionHashMap.get(uiAction.id()) == null) {
                SemanticAction semanticAction = SemanticAction.create(uiScreen, uiElement, parentElement, uiAction);
                if (SemanticAction.checkIfSemanticActionIsDefined(semanticAction)) {
                    semanticActionHashMap.put(uiAction.id(), semanticAction);
                    uiScreen.addSemanticActionToScreen(semanticAction);
                    //semanticActionStore.addSemanticAction(semanticAction);
                    //Utils.printDebug("Adding semantic action: " + semanticAction);
                }
            }
        }
    }

    private List<SemanticAction> getSemanticActions(UIScreen uiScreen, UIElement uiElement, @Nullable UIElement parentElement) {
        if (uiScreen == null || uiElement == null) {
            return new ArrayList<>();
        }
        List<SemanticAction> semanticActionList = new ArrayList<>();
        HashMap<String, SemanticAction> semanticActionHashMap = uiElement.getSemanticActions();
        for (UIAction uiAction: uiElement.getUiActions()) {
            if (semanticActionHashMap.get(uiAction.id()) == null) {
                SemanticAction semanticAction = SemanticAction.create(uiScreen, uiElement, parentElement, uiAction);
                if (SemanticAction.checkIfSemanticActionIsDefined(semanticAction)) {
                    semanticActionHashMap.put(uiAction.id(), semanticAction);
                    semanticActionList.add(semanticAction);
                }
            }
        }
        return semanticActionList;
    }

    private static String getSanitizedSubTitle(String subTitle) {
        String subtitle = Utils.sanitizeText(subTitle);
        if (ViewUtils.isTextOnOrOff(subtitle)) {
            return ViewUtils.ON_OFF_TEXT;
        } else {
            return subtitle;
        }
    }

    private static boolean isSubtitlePlaceholderText(String subTitle) {
        if (Utils.nullOrEmpty(subTitle)) {
            return false;
        }
        return (subTitle.equalsIgnoreCase(LOADING_SUBTITLE) || subTitle.equalsIgnoreCase(COMPUTING_SUBTITLE));
    }

    private static boolean shouldProcess(CrawlingInput crawlingInput) {
        if (crawlingInput.getLastUIAction() != null && crawlingInput.getLastUIAction().equalsIgnoreCase("TYPE_WINDOW_CONTENT_CHANGED")) {
            return false;
        }
        return crawlingInput.getRootPackageName() == null || !crawlingInput.getRootPackageName().equalsIgnoreCase("com.android.systemui");
    }

}
