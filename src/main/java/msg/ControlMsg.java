package msg;

import java.io.Serializable;

public class ControlMsg implements Serializable {
    private String brand;
    private String batch;
    private float tempRegion1;
    private float tempRegion2;
    private long time;
    private String version;
    private String deviceStatus;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public float getTempRegion1() {
        return tempRegion1;
    }

    public void setTempRegion1(float tempRegion1) {
        this.tempRegion1 = tempRegion1;
    }

    public float getTempRegion2() {
        return tempRegion2;
    }

    public void setTempRegion2(float tempRegion2) {
        this.tempRegion2 = tempRegion2;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }
}
