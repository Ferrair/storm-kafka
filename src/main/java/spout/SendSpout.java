package spout;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.Map;
import java.util.Random;

public class SendSpout extends BaseRichSpout {


    Map map;
    TopologyContext topologyContext;
    SpoutOutputCollector collector;

    public void open(Map<String, Object> map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.map = map;
        this.topologyContext = topologyContext;
        this.collector = spoutOutputCollector;
    }

    public void nextTuple() {
        try {
            Thread.sleep(100);
            final String[] words = new String[] {"{'temp': 1}", "{'wind': 2}", "{'speed': 3}"};
            final Random rand = new Random();
            final String word = words[rand.nextInt(words.length)];
            collector.emit(new Values(word));

        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("word"));
    }
}
