package it.polimi.dice.tracechecking.uml2json.json;

import com.google.gson.annotations.SerializedName;

public class MonitoringStatus {
	@SerializedName("PID")
	private String pid;
	@SerializedName("Alive")
	private boolean alive;
	
	public MonitoringStatus(String pid, boolean alive) {
		this.pid = pid;
		this.alive = alive;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	
	
}
