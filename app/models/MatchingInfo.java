package models;

import com.inceptai.neopojos.DeviceInfo;
import util.Utils;

public class MatchingInfo {
    private final String appVersion;
    private final String versionCode;
    private final String phoneManufacturer;
    private final String phoneModel;
    private final String release;
    private final String phoneSdk;
    private final String hardware;
    private final String product;

    public MatchingInfo(String appVersion,
                        String versionCode,
                        String phoneManufacturer,
                        String phoneModel,
                        String release,
                        String phoneSdk,
                        String hardware,
                        String product) {
        this.appVersion = appVersion;
        this.versionCode = versionCode;
        this.phoneManufacturer = phoneManufacturer;
        this.phoneModel = phoneModel;
        this.release = release;
        this.phoneSdk = phoneSdk;
        this.hardware = hardware;
        this.product = product;
    }

    public MatchingInfo() {
        this.appVersion = Utils.EMPTY_STRING;
        this.phoneManufacturer = Utils.EMPTY_STRING;
        this.phoneModel = Utils.EMPTY_STRING;
        this.release = Utils.EMPTY_STRING;
        this.phoneSdk = Utils.EMPTY_STRING;
        this.hardware = Utils.EMPTY_STRING;
        this.product = Utils.EMPTY_STRING;
        this.versionCode = Utils.EMPTY_STRING;
    }

    public MatchingInfo(DeviceInfo deviceInfo, String appVersion, String versionCode) {

        if (Utils.nullOrEmpty(appVersion)) {
            this.appVersion = deviceInfo.getRelease();
        } else {
            this.appVersion = appVersion;
        }

        if (Utils.nullOrEmpty(versionCode)) {
            this.versionCode = deviceInfo.getSdk();
        } else {
            this.versionCode = versionCode;
        }

        this.phoneManufacturer = deviceInfo.getManufacturer();
        this.phoneModel = deviceInfo.getModel();
        this.release = deviceInfo.getRelease();
        this.phoneSdk = deviceInfo.getSdk();
        this.hardware = deviceInfo.getHardware();
        this.product = deviceInfo.getProduct();
    }

    public MatchingInfo(DeviceInfo deviceInfo) {
        //For system apps, we set appversion to release (like Android 6.0) and version code to sdk (like 24).
        this.appVersion = deviceInfo.getRelease();
        this.versionCode = deviceInfo.getSdk();
        this.phoneManufacturer = deviceInfo.getManufacturer();
        this.phoneModel = deviceInfo.getModel();
        this.release = deviceInfo.getRelease();
        this.phoneSdk = deviceInfo.getSdk();
        this.hardware = deviceInfo.getHardware();
        this.product = deviceInfo.getProduct();
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public String getPhoneManufacturer() {
        return phoneManufacturer;
    }

    public String getPhoneModel() {
        return phoneModel;
    }

    public String getRelease() {
        return release;
    }

    public String getPhoneSdk() {
        return phoneSdk;
    }

    public String getHardware() {
        return hardware;
    }

    public String getProduct() {
        return product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchingInfo)) return false;

        MatchingInfo that = (MatchingInfo) o;

        if (appVersion != null ? !appVersion.equals(that.appVersion) : that.appVersion != null) return false;
        if (versionCode != null ? !versionCode.equals(that.versionCode) : that.versionCode != null) return false;
        if (phoneManufacturer != null ? !phoneManufacturer.equals(that.phoneManufacturer) : that.phoneManufacturer != null)
            return false;
        if (phoneModel != null ? !phoneModel.equals(that.phoneModel) : that.phoneModel != null) return false;
        if (release != null ? !release.equals(that.release) : that.release != null) return false;
        if (phoneSdk != null ? !phoneSdk.equals(that.phoneSdk) : that.phoneSdk != null) return false;
        if (hardware != null ? !hardware.equals(that.hardware) : that.hardware != null) return false;
        return product != null ? product.equals(that.product) : that.product == null;
    }

    @Override
    public int hashCode() {
        int result = appVersion != null ? appVersion.hashCode() : 0;
        result = 31 * result + (versionCode != null ? versionCode.hashCode() : 0);
        result = 31 * result + (phoneManufacturer != null ? phoneManufacturer.hashCode() : 0);
        result = 31 * result + (phoneModel != null ? phoneModel.hashCode() : 0);
        result = 31 * result + (release != null ? release.hashCode() : 0);
        result = 31 * result + (phoneSdk != null ? phoneSdk.hashCode() : 0);
        result = 31 * result + (hardware != null ? hardware.hashCode() : 0);
        result = 31 * result + (product != null ? product.hashCode() : 0);
        return result;
    }

    public boolean isSystemPackage() {
        if (Utils.nullOrEmpty(appVersion) || Utils.nullOrEmpty(versionCode) ||
                Utils.nullOrEmpty(release) || Utils.nullOrEmpty(phoneSdk)) {
            return false;
        }
        return appVersion.equalsIgnoreCase(release) && versionCode.equalsIgnoreCase(phoneSdk);
    }

    public boolean isEmpty() {
        return appVersion.isEmpty() &&
                versionCode.isEmpty() &&
                phoneManufacturer.isEmpty() &&
                phoneModel.isEmpty() &&
                release.isEmpty() &&
                phoneSdk.isEmpty() &&
                hardware.isEmpty() &&
                product.isEmpty();
    }

}
