package spout;

import gherkin.deps.com.google.gson.Gson;
import msg.OriginalMsg;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class TestSpout extends BaseRichSpout {
    //Create instance for SpoutOutputCollector which passes tuples to bolt.
    private SpoutOutputCollector collector;
    private boolean completed = false;
    private Gson gson;
    private InputStream is;
    private BufferedReader reader;

    private Integer idx = 0;


    //Create instance for TopologyContext which contains topology data.
    private TopologyContext context;

    @Override
    public void open(Map<String, Object> map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.context = topologyContext;
        this.collector = spoutOutputCollector;
        this.gson = new Gson();
        try {
            is = new FileInputStream("src/test/test_input.log");
            reader = new BufferedReader(new InputStreamReader(is));
        } catch (Exception e) {
        }
    }

    @Override
    public void nextTuple() {
        try {
            Thread.sleep(1000);
            if (this.idx <= 200) {
                idx++;
                String str = "";
                OriginalMsg oneMockMsg = null;
                str = reader.readLine();
                if (str != null) {
                    oneMockMsg = gson.fromJson(str, OriginalMsg.class);
                }

                if (oneMockMsg != null)
                    this.collector.emit(new Values(0,0,0, 0, str));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("mock1", "mock2", "mock3", "mock4", "mock5"));
    }
}
