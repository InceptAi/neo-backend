package models;

class DeviceInfo {
    private final String manufacturer;
    private final String model;
    private final String release;
    private final String sdk;
    private final String hardware;
    private final String product;

    public DeviceInfo(String manufacturer, String model, String release,
                      String sdk, String hardware, String product) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.release = release;
        this.sdk = sdk;
        this.hardware = hardware;
        this.product = product;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getRelease() {
        return release;
    }

    public String getSdk() {
        return sdk;
    }

    public String getHardware() {
        return hardware;
    }

    public String getProduct() {
        return product;
    }
}
