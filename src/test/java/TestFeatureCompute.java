import core.FeatureComputeBolt;
import msg.IoTMsg;
import msg.ModelMsg;
import org.junit.Test;
import util.FeatureUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestFeatureCompute {

    @Test
    public void testMean() {
        IoTMsg result = FeatureUtil.calcMean(getMockData());
        System.out.println(result.toString());
    }

    @Test
    public void testStd() {
        IoTMsg mean = FeatureUtil.calcMean(getMockData());
        IoTMsg result = FeatureUtil.calcStd(getMockData(), mean);
        System.out.println(result.toString());
    }


    @Test
    public void testIntegral() {
        IoTMsg result = FeatureUtil.calcIntegral(getMockData());
        System.out.println(result.toString());
    }

    @Test
    public void testSkew() {
        IoTMsg mean = FeatureUtil.calcMean(getMockData());
        IoTMsg result = FeatureUtil.calcSkew(getMockData(), mean);
        System.out.println(result.toString());
    }

    @Test
    public void testKurtosis() {
        IoTMsg mean = FeatureUtil.calcMean(getMockData());
        IoTMsg result = FeatureUtil.calcKurtosis(getMockData(), mean);
        System.out.println(result.toString());
    }

    @Test
    public void testFeatureCompute() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        IoTMsg[] data = getMockData();

        Class<FeatureComputeBolt> class1 = FeatureComputeBolt.class;
        Object instance = class1.newInstance();
        Method method = class1.getDeclaredMethod("computeFeature", List.class, int.class, int.class);
        method.setAccessible(true);
        Object result = method.invoke(instance, Arrays.asList(data), 50, 5);
        System.out.println(((ModelMsg) result).generate());
    }


    private static IoTMsg[] getMockData() {
        List<IoTMsg> list = new ArrayList<>();
        list.add(new IoTMsg(new Double[]{-10.66, 0.390567, 19.10618, -37.24573, 131.4467, 133.5563, 118.8188, 119.0817}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3917936, 19.10294, -36.03656, 131.5355, 133.5365, 118.789, 119.0544}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.39186809999999994, 19.09534, -36.36182, 131.6285, 133.517, 118.7584, 119.0544}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3925209, 19.09828, -36.14838, 131.7247, 133.4983, 118.7219, 119.028}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3935966, 19.0913, -34.99872, 131.8171, 133.4808, 118.6879, 119.0018}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3935158, 19.08923, -34.17712, 131.9119, 133.4808, 118.6502, 118.9757}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.39336220000000005, 19.09511, -33.95856, 132.002, 133.4636, 118.6066, 118.95}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3934222, 19.09287, -37.33533, 132.0914, 133.4468, 118.5597, 118.9253}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3938643, 19.10143, -40.27594, 132.1814, 133.5018, 118.5081, 118.9253}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3950312, 19.11732, -40.57544, 132.2754, 133.5628, 118.4511, 118.902}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3962857, 19.13039, -39.43634, 132.4177, 133.6252, 118.3662, 118.8592}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3966686, 19.13431, -38.67712, 132.4684, 133.6252, 118.3662, 118.8592}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3961982, 19.13636, -37.90149, 132.5706, 133.6861, 118.3096, 118.8592}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3959692, 19.13407, -38.64209, 132.668, 133.7445, 118.208, 118.8192}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3967118, 19.131, -39.16083, 132.7705, 133.7985, 118.1487, 118.8192}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3967991, 19.12304, -39.08484, 132.8776, 133.8463, 118.0799, 118.8002}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3967046, 19.11835, -38.4118, 132.9787, 133.8904, 118.0117, 118.7809}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3974093, 19.10254, -35.63397, 133.0792, 133.8904, 117.9779, 118.7617}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3975635, 19.09483, -35.58679, 133.1754, 133.9305, 117.8748, 118.7431}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3966887, 19.09869, -38.23877, 133.2652, 133.9676, 117.8025, 118.7256}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3969766, 19.10436, -39.44427, 133.3629, 134.0015, 117.7309, 118.7256}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3983358, 19.09719, -37.98047, 133.4648, 134.0328, 117.6932, 118.7085}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3983485, 19.0858, -36.78503, 133.5604, 134.0634, 117.6129, 118.6918}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3978139, 19.09092, -32.84259, 133.6593, 134.0634, 117.5011, 118.6771}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3978643, 19.09179, -32.65704, 133.7545, 134.0947, 117.4237, 118.6553}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3988149, 19.07274, -33.69733, 133.8405, 134.125, 117.3438, 118.6335}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3998042, 19.05736, -33.63373, 133.9232, 134.1529, 117.3047, 118.6335}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.4004687, 19.05311, -36.7984, 134.0056, 134.1763, 117.1964, 118.6119}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.398584, 19.06423, -35.14539, 134.1211, 134.1963, 117.0936, 118.5678}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.3993099, 19.06591, -33.87683, 134.2014, 134.2146, 117.0603, 118.5678}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.4001607, 19.07098, -33.3526, 134.2736, 134.2312, 116.9553, 118.5471}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.4001462, 19.07479, -33.9978, 134.3081, 134.2312, 116.9553, 118.5471}));
        list.add(new IoTMsg(new Double[]{-10.67, 0.40013059999999995, 19.08786, -35.16138, 134.3788, 134.2463, 116.8491, 118.5275}));
        list.add(new IoTMsg(new Double[]{-10.67, 0.4009608, 19.10514, -38.0509, 134.473, 134.2599, 116.7448, 118.4902}));
        list.add(new IoTMsg(new Double[]{-10.67, 0.4004962, 19.10489, -38.17627, 134.5295, 134.2738, 116.6788, 118.4729}));
        list.add(new IoTMsg(new Double[]{-10.66, 0.4005843, 19.10673, -35.11194, 134.5855, 134.2875, 116.6197, 118.4729}));
        list.add(new IoTMsg(new Double[]{-10.649999999999999, 0.40053790000000006, 19.10951, -33.95453, 134.6357, 134.2995, 116.5629, 118.4558}));
        list.add(new IoTMsg(new Double[]{-10.579999999999998, 0.4005851, 19.11929, -34.87567, 134.6809, 134.3107, 116.5301, 118.4737}));
        list.add(new IoTMsg(new Double[]{-10.5, 0.40041809999999994, 19.13594, -35.61298, 134.7256, 134.3107, 116.4369, 118.5312}));
        list.add(new IoTMsg(new Double[]{-10.36, 0.4001424, 19.1466, -38.61945, 134.7633, 134.3203, 116.4095, 118.5312}));
        list.add(new IoTMsg(new Double[]{-10.229999999999999, 0.3995191, 19.15343, -41.18134, 134.799, 134.327, 116.3566, 118.59299999999999}));
        list.add(new IoTMsg(new Double[]{-9.92, 0.3988888, 19.15899, -41.29034, 134.8316, 134.3317, 116.3066, 118.6552}));
        list.add(new IoTMsg(new Double[]{-9.16, 0.3981453, 19.11916, -42.44208, 134.8687, 134.3358, 116.2307, 118.7153}));
        list.add(new IoTMsg(new Double[]{-8.889999999999999, 0.3985295, 19.10333, -43.32147, 134.881, 134.3392, 116.206, 118.7722}));
        list.add(new IoTMsg(new Double[]{-8.309999999999999, 0.3992882, 19.08613, -45.37762, 134.9033, 134.3392, 116.1595, 118.7722}));
        list.add(new IoTMsg(new Double[]{-7.819999999999999, 0.3999003, 19.06352, -45.42078, 134.9187, 134.3428, 116.1217, 118.8234}));
        list.add(new IoTMsg(new Double[]{-7.279999999999999, 0.40041609999999994, 19.05226, -41.09686, 134.9331, 134.3476, 116.091, 118.8694}));
        list.add(new IoTMsg(new Double[]{-6.799999999999999, 0.4002319, 19.06224, -38.0658, 134.9457, 134.3537, 116.0745, 118.9109}));
        list.add(new IoTMsg(new Double[]{-6.339999999999999, 0.399815, 19.07063, -39.04053, 134.9539, 134.3601, 116.055, 118.9493}));
        list.add(new IoTMsg(new Double[]{-5.909998999999999, 0.4000592, 19.07561, -41.19458, 134.9624, 134.3657, 116.0455, 118.9851}));
        return list.toArray(new IoTMsg[0]);
    }
}
