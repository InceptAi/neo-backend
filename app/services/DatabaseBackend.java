package services;

import models.UIScreen;

import java.util.List;
import java.util.function.Function;

public interface DatabaseBackend {
    UIScreen getScreenById(String screenId);
    List<UIScreen> getAllScreens();
    void saveScreensAsync(List<UIScreen> screenList);
    void deleteScreensAsync(List<UIScreen> screenList);
    <U> void loadAllScreens(Function<List<UIScreen>, U> function);
}
