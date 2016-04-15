// Copyright 2016 Twitter. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package backtype.storm.topology;

import java.util.HashMap;
import java.util.Map;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import backtype.storm.topology.FailedException;
import backtype.storm.topology.ReportedFailedException;
import backtype.storm.task.OutputCollector;

public class BasicBoltExecutor implements IRichBolt {
    private IBasicBolt delegate;
    private transient BasicOutputCollector collector;
    
    public BasicBoltExecutor(IBasicBolt bolt) {
        this.delegate = bolt;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        delegate.declareOutputFields(declarer);
    }

    
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        delegate.prepare(stormConf, context);
        this.collector = new BasicOutputCollector(collector);
    }

    @Override
    public void execute(Tuple input) {
        this.collector.setContext(input);
        try {
            delegate.execute(input, collector);
            this.collector.getOutputter().ack(input);
        } catch(FailedException e) {
            if(e instanceof ReportedFailedException) {
                this.collector.reportError(e);
            }
            this.collector.getOutputter().fail(input);
        }
    }

    @Override
    public void cleanup() {
        delegate.cleanup();
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return delegate.getComponentConfiguration();
    }
}