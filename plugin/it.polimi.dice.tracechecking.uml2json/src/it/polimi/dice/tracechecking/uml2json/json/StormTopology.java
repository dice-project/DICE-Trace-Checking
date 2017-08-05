package it.polimi.dice.tracechecking.uml2json.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import it.polimi.dice.tracechecking.uml.diagrams.classdiagram.BoltClass;
import it.polimi.dice.tracechecking.uml.diagrams.classdiagram.SpoutClass;

public class StormTopology {
	
	private List<SpoutClass> spouts;
	private List<BoltClass> bolts;
	
	@SerializedName(value="max_reboot_time")
	private int maxRebootTime;
	@SerializedName(value="min_reboot_time")
	private int minRebootTime;
	@SerializedName(value="max_idle_time")
	private double maxIdleTime;
	@SerializedName(value="queue_threshold")
	private int queueThreshold;
	@SerializedName(value="init_queues")
	private int initQueues;
	
	public StormTopology(){
		
		this.spouts = new ArrayList<>();
		this.bolts = new ArrayList<>();
		this.maxIdleTime = 0.1;
		this.minRebootTime = 10;
		this.maxRebootTime = 100;
		this.initQueues = 5;
		this.queueThreshold = 0;
	}
	/*
	 "min_reboot_time":10,
      "max_reboot_time":100,
      "max_idle_time":  0.01,
      "queue_threshold": 0,
      "init_queues":2
	 */

	public List<SpoutClass> getSpouts() {
		return spouts;
	}
	
	public List<String> getSpoutsNames(){
		List<String> names = new ArrayList<>();
		for (SpoutClass s : spouts) {
			names.add(s.getName());
		}
		return names;
	}

	public void setSpouts(List<SpoutClass> spouts) {
		this.spouts = spouts;
	}

	public List<BoltClass> getBolts() {
		return bolts;
	}
	
	public List<String> getBoltsNames(){
		List<String> names = new ArrayList<>();
		for (BoltClass b : bolts) {
			names.add(b.getName());
		}
		return names;
	}

	public void setBolts(List<BoltClass> bolts) {
		this.bolts = bolts;
	}

	public int getMaxRebootTime() {
		return maxRebootTime;
	}

	public void setMaxRebootTime(int maxRebootTime) {
		this.maxRebootTime = maxRebootTime;
	}

	public int getMinRebootTime() {
		return minRebootTime;
	}

	public void setMinRebootTime(int minRebootTime) {
		this.minRebootTime = minRebootTime;
	}

	public double getMaxIdleTime() {
		return maxIdleTime;
	}

	public void setMaxIdleTime(double maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
	}

	public int getQueueThreshold() {
		return queueThreshold;
	}

	public void setQueueThreshold(int queueThreshold) {
		this.queueThreshold = queueThreshold;
	}

	public int getInitQueues() {
		return initQueues;
	}

	public void setInitQueues(int initQueues) {
		this.initQueues = initQueues;
	}
	

}
