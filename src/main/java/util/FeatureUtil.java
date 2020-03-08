package util;

import msg.IoTMsg;

import java.util.Arrays;

@SuppressWarnings("Duplicates")
public class FeatureUtil {

    public static IoTMsg calcMean(IoTMsg[] split) {
        Double[] result = new Double[8];
        Arrays.fill(result, 0d);
        int size = split.length;

        for (IoTMsg msg : split) {
            Double[] msgArray = msg.generate();
            for (int i = 0; i < msgArray.length; i++) {
                result[i] += msgArray[i];
            }
        }
        return new IoTMsg(
                result[0] / size,
                result[1] / size,
                result[2] / size,
                result[3] / size,
                result[4] / size,
                result[5] / size,
                result[6] / size,
                result[7] / size
        );
    }

    public static IoTMsg calcStd(IoTMsg[] split, IoTMsg mean) {
        Double[] result = new Double[8];
        Arrays.fill(result, 0d);

        Double[] meanArray = mean.generate();
        int size = split.length;

        for (IoTMsg msg : split) {
            Double[] msgArray = msg.generate();
            for (int i = 0; i < msgArray.length; i++) {
                result[i] += Math.pow(msgArray[i] - meanArray[i], 2);
            }
        }
        return new IoTMsg(
                Math.sqrt(result[0] / size),
                Math.sqrt(result[1] / size),
                Math.sqrt(result[2] / size),
                Math.sqrt(result[3] / size),
                Math.sqrt(result[4] / size),
                Math.sqrt(result[5] / size),
                Math.sqrt(result[6] / size),
                Math.sqrt(result[7] / size)
        );


    }

    public static IoTMsg calcIntegral(IoTMsg[] split) {
        Double[] result = new Double[8];
        Arrays.fill(result, 0d);
        int size = split.length;

        for (IoTMsg msg : split) {
            Double[] msgArray = msg.generate();
            for (int i = 0; i < msgArray.length; i++) {
                result[i] += msgArray[i];
            }
        }
        return new IoTMsg(
                result[0] - (split[0].getHumidOutDiff() + split[size - 1].getHumidOutDiff()) / 2,
                result[1] - (split[0].getWindSpeed() + split[size - 1].getWindSpeed()) / 2,
                result[2] - (split[0].getHumidIn() + split[size - 1].getHumidIn()) / 2,
                result[3] - (split[0].getPress() + split[size - 1].getPress()) / 2,
                result[4] - (split[0].getTempActual1() + split[size - 1].getTempActual1()) / 2,
                result[5] - (split[0].getTempSetting1() + split[size - 1].getTempSetting1()) / 2,
                result[6] - (split[0].getTempActual2() + split[size - 1].getTempActual2()) / 2,
                result[7] - (split[0].getTempSetting2() + split[size - 1].getTempSetting2()) / 2
        );
    }

    public static IoTMsg calcSkew(IoTMsg[] split, IoTMsg mean) {
        // TODO https://www.cnblogs.com/jiaxin359/p/8977333.html
        Double[] numerator = new Double[8];
        Double[] denominator = new Double[8];
        Double[] meanArray = mean.generate();

        int size = split.length;
        Arrays.fill(numerator, 0d);
        Arrays.fill(denominator, 0d);

        for (IoTMsg msg : split) {
            Double[] msgArray = msg.generate();
            for (int i = 0; i < msgArray.length; i++) {
                numerator[i] += Math.pow(msgArray[i] - meanArray[i], 3);
                denominator[i] += Math.pow(msgArray[i] - meanArray[i], 2);
            }
        }

        for (int i = 0; i < numerator.length; i++) {
            numerator[i] = numerator[i] / size;
            denominator[i] = Math.pow(denominator[i] / size, 1.5);
        }
        return new IoTMsg(
                checkNAN(numerator[0] / denominator[0]),
                checkNAN(numerator[1] / denominator[1]),
                checkNAN(numerator[2] / denominator[2]),
                checkNAN(numerator[3] / denominator[3]),
                checkNAN(numerator[4] / denominator[4]),
                checkNAN(numerator[5] / denominator[5]),
                checkNAN(numerator[6] / denominator[6]),
                checkNAN(numerator[7] / denominator[7])
        );
    }

    public static IoTMsg calcKurtosis(IoTMsg[] split, IoTMsg mean) {
        // TODO https://www.cnblogs.com/jiaxin359/p/8977333.html
        Double[] numerator = new Double[8];
        Double[] denominator = new Double[8];
        Double[] meanArray = mean.generate();

        int size = split.length;
        Arrays.fill(numerator, 0d);
        Arrays.fill(denominator, 0d);

        for (IoTMsg msg : split) {
            Double[] msgArray = msg.generate();
            for (int i = 0; i < msgArray.length; i++) {
                numerator[i] += Math.pow(msgArray[i] - meanArray[i], 4);
                denominator[i] += Math.pow(msgArray[i] - meanArray[i], 2);
            }
        }

        for (int i = 0; i < numerator.length; i++) {
            numerator[i] = numerator[i] / size;
            denominator[i] = Math.pow(denominator[i] / size, 2);
        }
        return new IoTMsg(
                checkNAN(numerator[0] / denominator[0]) - 3,
                checkNAN(numerator[1] / denominator[1]) - 3,
                checkNAN(numerator[2] / denominator[2]) - 3,
                checkNAN(numerator[3] / denominator[3]) - 3,
                checkNAN(numerator[4] / denominator[4]) - 3,
                checkNAN(numerator[5] / denominator[5]) - 3,
                checkNAN(numerator[6] / denominator[6]) - 3,
                checkNAN(numerator[7] / denominator[7]) - 3
        );
    }

    public static Double checkNAN(Double num) {
        return Double.isNaN(num) ? 0d : num;
    }
}
