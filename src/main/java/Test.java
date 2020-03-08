import core.ControlBolt;
import core.ModelBolt;
import core.FeatureComputeBolt;
import org.apache.storm.tuple.Fields;
import spout.SendSpout;
import core.WindowsBolt;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;

public class Test {


    public void run() throws Exception{
        final TopologyBuilder tp = new TopologyBuilder();
        tp.setSpout("word", new SendSpout(), 1);
        tp.setBolt("window", new WindowsBolt()).fieldsGrouping("word", new Fields("word"));
        tp.setBolt("feature_compute", new FeatureComputeBolt()).fieldsGrouping("window", new Fields("window"));
        tp.setBolt("model", new ModelBolt()).fieldsGrouping("feature_compute", new Fields("features"));
        tp.setBolt("control", new ControlBolt()).fieldsGrouping("model", new Fields("model"));
        StormTopology sp = tp.createTopology();
        LocalCluster cluster = new LocalCluster();
        Config conf = new Config();
        cluster.submitTopology("test", conf, sp);
    }
}
