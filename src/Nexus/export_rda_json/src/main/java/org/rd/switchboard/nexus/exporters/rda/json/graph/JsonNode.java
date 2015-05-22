package org.rd.switchboard.nexus.exporters.rda.json.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * History
 * 1.0.2: Added `extra` property
 * 1.0.3: Replaced json node class with new one
 * 
 * @author dima
 *
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonNode {
	public static final String EXTRA_ROOT = "root";
	public static final String EXTRA_INCOMPLETE = "incomplete";
	
	private long id;
	private String type;
	private Set<String> extras;

	private final Map<String, Object> properties = new HashMap<String, Object>();

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public Set<String> getExtras() {
		return extras;
	}
		
	public void addExtra(String extra) {
		if (null == extras)
			extras = new HashSet<String>();
		
		for (String e : extras) 
			if (e.equals(extra))
				return;
		
		extras.add(extra);
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	public void addProperty(String name, Object value) {
		this.properties.put(name, value);
	}

	@Override
	public String toString() {
		return "Node [id=" + id 
				+ ", type=" + type 
				+ ", extras=" + extras
				+ ", properties=" + properties 
				+ "]";
	}	
}
