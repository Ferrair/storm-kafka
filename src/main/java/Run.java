import config.AppConfig;
import core.*;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import spout.TestSpout;

public class Run {

    public static void main(String[] args) throws Exception {

        // 初始化配置
        AppConfig.initConfig();
        if ("test".equals(AppConfig.AppGlobalConfig.env)) {
            Test test = new Test();
            test.run();
        } else {
            final TopologyBuilder tp = new TopologyBuilder();

//            KafkaSpoutConfig<String, String> kafkaSpoutConfig = KafkaSpoutConfig.builder(
//                    String.format("%s:%s", AppConfig.DefaultKafkaConfig.host, AppConfig.DefaultKafkaConfig.port),
//                    AppConfig.DefaultKafkaConfig.topic)
//                    .setProp(AppConfig.DefaultKafkaConfig.kafkaProperties())
//                    .setProcessingGuarantee(KafkaSpoutConfig.ProcessingGuarantee.AT_LEAST_ONCE)
//                    .build();
//
//            KafkaSpout<String, String> kafkaSpout = new KafkaSpout<>(kafkaSpoutConfig);
//            tp.setSpout("kafka", new KafkaSpout<String, String>(kafkaSpoutConfig), 1);
            // tp.setBolt("check", new CheckBlot(), 1).shuffleGrouping("kafka");
//            tp.setBolt("window", new WindowsBolt(), 1).shuffleGrouping("kafka");
//            tp.setBolt("feature_compute", new FeatureComputeBolt()).fieldsGrouping("window", new Fields("window"));
//            tp.setBolt("model", new ModelBolt()).fieldsGrouping("feature_compute", new Fields("features"));
            tp.setSpout("test", new TestSpout());
            tp.setBolt("direct_window", new DirectWindowsBolt(), 1).shuffleGrouping("test");

            // 提交运行
            StormTopology sp = tp.createTopology();
            Config conf = new Config();
            if ("local".equals(AppConfig.AppGlobalConfig.env)) {
                LocalCluster cluster = new LocalCluster();

                cluster.submitTopology(AppConfig.AppGlobalConfig.appName, conf, sp);
            } else if ("cluster".equals(AppConfig.AppGlobalConfig.env)) {
                StormSubmitter.submitTopology(AppConfig.AppGlobalConfig.appName, conf, sp);
            }

        }
    }
}
