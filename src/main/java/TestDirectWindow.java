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

public class TestDirectWindow {

    public static void main(String[] args) throws Exception {

        AppConfig.initConfig();
        final TopologyBuilder tp = new TopologyBuilder();

        tp.setSpout("test", new TestSpout());
        tp.setBolt("direct_window", new DirectWindowsBolt(), 1).shuffleGrouping("test");

        StormTopology sp = tp.createTopology();
        Config conf = new Config();
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("test", conf, sp);

    }

}
