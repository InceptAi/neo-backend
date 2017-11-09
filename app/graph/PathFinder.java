package graph;

import models.UIPath;
import models.UIScreen;

public interface PathFinder {
    UIPath findPathBetweenScreens(UIScreen srcScreen, UIScreen dstScreen);
}
