package it.polimi.dice.tracechecking.uml2json.json;

import com.google.gson.annotations.SerializedName;

public class StormTCOutcome {
	
/*	[
	  {
	    "metric_value": "1.0",
	    "property": {
	      "designvalue": 1.0,
	      "method": "counting,
	      "name": "exclaim1",
	      "parameter": "sigma",
	      "relation": "<",
	      "timewindow": 3600.0
	    },
	    "result": false
	  }
	]*/
	@SerializedName("metric_value")
	private double metricValue;
	private TopologyNodeFormula property;
	private boolean result;
	public double getMetricValue() {
		return metricValue;
	}
	public TopologyNodeFormula getProperty() {
		return property;
	}
	public boolean isResult() {
		return result;
	}
	
	
}
