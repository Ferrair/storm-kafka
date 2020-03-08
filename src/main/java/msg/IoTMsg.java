package msg;

import java.util.Arrays;

public class IoTMsg {

    private Double humidOutDiff;
    private Double windSpeed;
    private Double humidIn;
    private Double press;
    private Double tempActual1;
    private Double tempSetting1;
    private Double tempActual2;
    private Double tempSetting2;

    public IoTMsg(Double[] array) {
        this.humidOutDiff = array[0];
        this.windSpeed = array[1];
        // array[2] 是出口水分，暂时不需要
        this.humidIn = array[3];
        this.press = array[4];
        this.tempActual1 = array[5];
        this.tempSetting1 = array[6];
        this.tempActual2 = array[7];
        this.tempSetting2 = array[8];
    }

    public IoTMsg(Double humidOutDiff,
                  Double windSpeed,
                  Double humidIn,
                  Double press,
                  Double tempActual1,
                  Double tempSetting1,
                  Double tempActual2,
                  Double tempSetting2
    ) {
        this.humidOutDiff = humidOutDiff;
        this.windSpeed = windSpeed;
        this.humidIn = humidIn;
        this.press = press;
        this.tempActual1 = tempActual1;
        this.tempSetting1 = tempSetting1;
        this.tempActual2 = tempActual2;
        this.tempSetting2 = tempSetting2;
    }


    public Double[] generate() {
        return new Double[]{
                getHumidOutDiff(),
                getWindSpeed(),
                getHumidIn(),
                getPress(),
                getTempActual1(),
                getTempSetting1(),
                getTempActual2(),
                getTempSetting2()
        };
    }

    public Double getHumidOutDiff() {
        return humidOutDiff;
    }

    public void setHumidOutDiff(Double humidOutDiff) {
        this.humidOutDiff = humidOutDiff;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Double getHumidIn() {
        return humidIn;
    }

    public void setHumidIn(Double humidIn) {
        this.humidIn = humidIn;
    }

    public Double getPress() {
        return press;
    }

    public void setPress(Double press) {
        this.press = press;
    }

    public Double getTempActual1() {
        return tempActual1;
    }

    public void setTempActual1(Double tempActual1) {
        this.tempActual1 = tempActual1;
    }

    public Double getTempSetting1() {
        return tempSetting1;
    }

    public void setTempSetting1(Double tempSetting1) {
        this.tempSetting1 = tempSetting1;
    }

    public Double getTempActual2() {
        return tempActual2;
    }

    public void setTempActual2(Double tempActual2) {
        this.tempActual2 = tempActual2;
    }

    public Double getTempSetting2() {
        return tempSetting2;
    }

    public void setTempSetting2(Double tempSetting2) {
        this.tempSetting2 = tempSetting2;
    }


    @Override
    public String toString() {
        return Arrays.toString(generate());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IoTMsg)) {
            return false;
        }
        Double[] source = generate();
        Double[] target = ((IoTMsg) obj).generate();
        if (source.length != target.length) {
            return false;
        }
        for (int i = 0; i < source.length; i++) {
            if (!source[i].equals(target[i])) {
                return false;
            }
        }
        return true;

    }
}
