package util;

import models.*;
import storage.NavigationGraphStore;
import storage.SemanticActionStore;
import storage.UIScreenStore;
import views.CrawlingInput;
import views.RenderingView;

import javax.annotation.Nullable;
import java.util.*;

import static views.CrawlingInput.FULL_SCREEN_MODE;
import static views.CrawlingInput.PARTIAL_SCREEN_MODE;

public class CrawlingInputParser {
    public static final boolean GET_TOP_ELEMENT_IF_MULTIPLE_MATCHES = true;
    public static final String LOADING_SUBTITLE = "LOADING";
    public static final String COMPUTING_SUBTITLE = "COMPUTING";


    public static boolean isSubtitlePlaceholderText(String subTitle) {
        if (Utils.nullOrEmpty(subTitle)) {
            return false;
        }
        return (subTitle.equalsIgnoreCase(LOADING_SUBTITLE) || subTitle.equalsIgnoreCase(COMPUTING_SUBTITLE));
    }

    public static boolean shouldProcess(CrawlingInput crawlingInput) {
        if (crawlingInput.getLastUIAction() != null && crawlingInput.getLastUIAction().equalsIgnoreCase("TYPE_WINDOW_CONTENT_CHANGED")) {
            return false;
        }
        if (crawlingInput.getRootPackageName() != null && crawlingInput.getRootPackageName().equalsIgnoreCase("com.android.systemui")) {
            return false;
        }
        return true;
    }

    public static UIScreen parseCrawlingInput(CrawlingInput crawlingInput) {
        if (!shouldProcess(crawlingInput)) {
            return null;
        }

        if (Utils.nullOrEmpty(crawlingInput.getRootTitle())) {
            Utils.printDebug("In parseCrawlingInput, empty root: input = " + crawlingInput.toString());
            return null;
        }

        UIScreen screenToBeCreated = new UIScreen();
        if (crawlingInput.getRootTitle() != null) {
            screenToBeCreated.setTitle(crawlingInput.getRootTitle());
            Utils.printDebug("In parseCrawlingInput currentTitle: " + crawlingInput.getRootTitle());
        }
        if (crawlingInput.getRootSubTitle() != null) {
            Utils.printDebug("In parseCrawlingInput subTitle: " + crawlingInput.getRootSubTitle());
            if (!isSubtitlePlaceholderText(crawlingInput.getRootSubTitle())) {
                screenToBeCreated.setSubTitle(crawlingInput.getRootSubTitle());
            } else {
                Utils.printDebug("Placeholder subTitle, so ignoring: ");
            }
        }

        if (crawlingInput.getRootPackageName() != null) {
            screenToBeCreated.setPackageName(crawlingInput.getRootPackageName());
            Utils.printDebug("In parseCrawlingInput currentPkg: " + crawlingInput.getRootPackageName());
        }
        if (crawlingInput.getDeviceInfo() != null) {
            screenToBeCreated.setDeviceInfo(crawlingInput.getDeviceInfo());
        }
        if (crawlingInput.getCurrentScreenType() != null) {
            screenToBeCreated.setScreenType(crawlingInput.getCurrentScreenType());
        }


        if (crawlingInput.getLastScreenTitle() != null &&
                crawlingInput.getLastScreenPackageName() != null &&
                crawlingInput.getLastUIAction() != null &&
                crawlingInput.getLastViewClicked() != null && crawlingInput.getLastScreenType() != null) {
            String lastClickedText = crawlingInput.getLastViewClicked().getText() != null ?
                    crawlingInput.getLastViewClicked().getText() : Utils.EMPTY_STRING;
            Utils.printDebug("In parseCrawlingInput lastScreenTitle: " +
                    crawlingInput.getLastScreenTitle() +
                    " lastpkg: " + crawlingInput.getLastScreenPackageName() +
                    " lastAction: " + crawlingInput.getLastUIAction() +
                    " lastViewText: " + lastClickedText +
                    " lastScreenType: " + crawlingInput.getLastScreenType());
        }


        String currentScreenId = screenToBeCreated.getId();

        List<Long> sortedViewIds= new ArrayList<>(crawlingInput.getViewMap().keySet());
        Collections.sort(sortedViewIds);

        HashMap<Long, UIElement> viewIdToUIElementMap = new HashMap<>();
        for (Long viewId: sortedViewIds) {
        //for (Map.Entry<Long, RenderingView> entry : crawlingInput.getViewMap().entrySet()) {
            //RenderingView renderingView = entry.getValue();
            //Create a UI element from the view -- no child text / no semantic / or navigational stuff for now
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
            if (currentElement.isClickable()) {
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
                crawlingInput.getLastScreenSubTitle(),
                crawlingInput.getLastScreenPackageName(),
                crawlingInput.getLastScreenType(),
                crawlingInput.getLastViewClicked(),
                crawlingInput.getLastUIAction());


        //Add the uiStep to lastScreen's UIPath and assign to current screen
        if (!uiStep.isUndefined()) {
            Utils.printDebug("Last UI Step is " + uiStep.toString());
            String lastScreenId = uiStep.getSrcScreenId();
            if (!Utils.nullOrEmpty(lastScreenId)) {
                UIScreen lastScreen = UIScreenStore.getInstance().getScreen(lastScreenId);
                if (lastScreen != null) {
                    if (uiStep.isInterScreenStep() || uiStep.isSoftStep()) { //navigational step
                        Utils.printDebug("Navigational UI Step");
                        //List<UIPath> lastScreenUiPaths = lastScreen.getUiPaths();
                        //Utils.printDebug("Last screen paths: " + lastScreenUiPaths.toString());
                        UIElement lastElement = null;
                        UIScreen.UIElementTuple lastElementTuple = lastScreen.findElementAndTopLevelParentById(uiStep.getUiElementId());
                        if (lastElementTuple != null) {
                            lastElement = lastElementTuple.getUiElement();
                        }
                        //findTopLevelElementById(uiStep.getUiElementId());
                        if (lastElement != null) {
                            Utils.printDebug("Adding navigational step to element: " + lastElement.toString());
                            lastElement.add(new NavigationalAction(uiStep.getUiEventId(), currentScreenId));
                            Utils.printDebug("Adding uiStep to lastPaths");
                            lastScreen.addUIStepForDestinationScreen(screenToBeCreated.getId(), uiStep);
                            screenToBeCreated.addUIStepToCurrentScreen(uiStep);
                            //screenToBeCreated.setUiPaths(MergeUtils.getUIPathBasedOnLastScreenPath(lastScreenUiPaths, uiStep));
                            //Utils.printDebug("New UI Path: " + screenToBeCreated.getUiPaths().toString());
                            //Add this edge to the graph
                            NavigationGraphStore.getInstance().addNavigationEdgeToGraph(uiStep);
                            //Add subScreen
                            if (screenToBeCreated.getScreenType().equalsIgnoreCase(PARTIAL_SCREEN_MODE) &&
                                    lastScreen.getScreenType().equalsIgnoreCase(FULL_SCREEN_MODE)) {
                                lastScreen.addChildScreen(screenToBeCreated);
                                screenToBeCreated.setParentScreenId(lastScreenId);
                                Utils.printDebug("Adding screen " +
                                        screenToBeCreated.toString() +
                                        " as child of " + lastScreen.toString());
                            }
                        }
                    } else if (uiStep.isWithinSameScreen()) {
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
            }
        }
        return screenToBeCreated;
    }

    private static UIStep getLastUIStep(UIScreen currentScreen,
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
        UIStep.UIStepType uiStepType = UIStep.UIStepType.UNDEFINED;
        String lastScreenId = Utils.EMPTY_STRING;

        if (!Utils.nullOrEmpty(lastScreenTitle) && !Utils.nullOrEmpty(lastScreenPackageName)) {
            lastScreenId = UIScreen.getScreenId(
                    lastScreenPackageName,
                    lastScreenTitle,
                    lastScreenSubTitle,
                    lastScreenType,
                    currentScreen.getDeviceInfo().toString());
            lastScreen = UIScreenStore.getInstance().getScreen(lastScreenId);
        }

        if (lastScreen == null) { // handle this case later
            Utils.printDebug("Error In getLastUIStep: lastScreenId not found with title/pkg " + lastScreenTitle + " / " + lastScreenPackageName);
            return undefinedUIStep;
        }

        //We need fuzzy matching for window state changed as text is Advanced Wi-Fi while in menu text is Advanced
//        boolean isFuzzySearchNeeded = lastUIEvent.equals(UIEvent.TYPE_WINDOW_STATE_CHANGED)

        boolean isFuzzySearchNeeded = lastUIEvent.equals(UIEvent.TYPE_WINDOW_STATE_CHANGED) &&
                lastScreen.getScreenType().equalsIgnoreCase(PARTIAL_SCREEN_MODE);

        if (lastScreenId.equalsIgnoreCase(currentScreen.getId())) {
            uiStepType = UIStep.UIStepType.WITHIN_SAME_SCREEN;
        } else if (isFuzzySearchNeeded){
            uiStepType = UIStep.UIStepType.SOFT_STEP_INTER_SCREEN;
        } else {
            uiStepType = UIStep.UIStepType.TO_ANOTHER_SCREEN;
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

        //TODO: Replaced getText with getOverallText to include contentDescription -- see if it works
//        List<UIElement> matchingElements = lastScreen.findElementsInScreen(
//                lastViewClicked.getClassName(),
//                lastViewClicked.getPackageName(),
//                lastViewClicked.getOverallText(),
//                true,
//                isFuzzySearchNeeded);

        //If more than one, take the top one for now
        // TODO sort the elements based on level of text matching --
        // TODO more matching means higher score so we select the element that matches the most.

        if (matchingElements.size() == 1 || (GET_TOP_ELEMENT_IF_MULTIPLE_MATCHES && matchingElements.size() > 1)) {
            lastElement = matchingElements.get(0);
        }

        if (lastElement == null) {
            Utils.printDebug("Error in getLastUIStep: last element is null");
            return undefinedUIStep;
        }

        //Create a UI Step and add to the path
        //return new UIStep(lastScreenId, currentScreen.getId(), lastElement.getId(), lastUIEvent.id(), uiStepType.id());
        return new UIStep(screenIdForMatchingElement, currentScreen.getId(), lastElement.getId(), lastUIEvent.id(), uiStepType.id());

    }

    public static UIElement createUIElementFromRenderingView(RenderingView renderingView) {
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

    private static void updateSemanticActions(UIScreen uiScreen, UIElement uiElement, @Nullable UIElement parentElement) {
        if (uiScreen == null || uiElement == null) {
            return;
        }
        HashMap<String, SemanticAction> semanticActionHashMap = uiElement.getSemanticActions();
        for (UIAction uiAction: uiElement.getUiActions()) {
            if (semanticActionHashMap.get(uiAction.id()) == null) {
                SemanticAction semanticAction = SemanticAction.create(uiScreen, uiElement, parentElement, uiAction);
                if (!SemanticAction.isUndefined(semanticAction)) {
                    semanticActionHashMap.put(uiAction.id(), semanticAction);
                    SemanticActionStore.getInstance().addSemanticAction(semanticAction);
                    //Utils.printDebug("Adding semantic action: " + semanticAction);
                }
            }
        }
    }
}
