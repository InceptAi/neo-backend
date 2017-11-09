package views;

import models.UIAction;

@SuppressWarnings("unused")
public class NavigationIdentifier {
    private ScreenIdentifier srcScreenIdentifier;
    private ScreenIdentifier dstScreenIdentifier;
    private ElementIdentifier elementIdentifier;
    private String uiActionToTake;

    public NavigationIdentifier(ScreenIdentifier srcScreenIdentifier,
                                ScreenIdentifier dstScreenIdentifier,
                                ElementIdentifier elementIdentifier) {
        this.srcScreenIdentifier = srcScreenIdentifier;
        this.dstScreenIdentifier = dstScreenIdentifier;
        this.elementIdentifier = elementIdentifier;
        this.uiActionToTake = UIAction.CLICK.id();
    }

    public ScreenIdentifier getSrcScreenIdentifier() {
        return srcScreenIdentifier;
    }

    public void setSrcScreenIdentifier(ScreenIdentifier srcScreenIdentifier) {
        this.srcScreenIdentifier = srcScreenIdentifier;
    }

    public ScreenIdentifier getDstScreenIdentifier() {
        return dstScreenIdentifier;
    }

    public void setDstScreenIdentifier(ScreenIdentifier dstScreenIdentifier) {
        this.dstScreenIdentifier = dstScreenIdentifier;
    }

    public ElementIdentifier getElementIdentifier() {
        return elementIdentifier;
    }

    public void setElementIdentifier(ElementIdentifier elementIdentifier) {
        this.elementIdentifier = elementIdentifier;
    }

    public String getUiActionToTake() {
        return uiActionToTake;
    }

    public void setUiActionToTake(String uiActionToTake) {
        this.uiActionToTake = uiActionToTake;
    }

}
