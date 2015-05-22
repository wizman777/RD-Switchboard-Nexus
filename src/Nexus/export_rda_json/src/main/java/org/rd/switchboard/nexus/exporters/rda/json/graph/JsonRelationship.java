
package org.rd.switchboard.nexus.exporters.rda.json.graph;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 1.0.1: Replaced json relationship class with new one
 * @author dima
 *
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRelationship {
	private long from;
	private long to;
	private String type;
		
	public long getFrom() {
		return from;
	}

	public void setFrom(long from) {
		this.from = from;
	}

	public long getTo() {
		return to;
	}

	public void setTo(long to) {
		this.to = to;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "JsonRelationship [from=" + from + ", to=" + to + ", type="
				+ type + "]";
	}
}
