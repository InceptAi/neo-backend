package services;

import models.UIScreen;

import java.util.List;

public interface DatabaseBackend {
    UIScreen getScreenById(String screenId);
    List<UIScreen> getAllScreens();
    List<UIScreen> getAllScreensForDevice(String deviceInfo);
    List<UIScreen> getAllScreensForPackage(String packageName);
    void saveScreens(List<UIScreen> screenList);
}
