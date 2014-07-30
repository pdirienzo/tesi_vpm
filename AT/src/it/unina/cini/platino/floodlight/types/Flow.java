package it.unina.cini.platino.floodlight.types;

import it.unina.cini.platino.db.Controller;
import it.unina.cini.platino.floodlight.FloodlightController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * An class representing a Floodlight flow
 * 
 * <p> 
 * Copyright (C) 2014 University of Naples. All Rights Reserved.
 * <p>
 * This program is distributed under GPL Version 2.0, WITHOUT ANY WARRANTY
 * 
 * @author <a href="mailto:p.dirienzo@studenti.unina.it">p.dirienzo@studenti.unina.it</a>, 
 * <a href="mailto:enr.demaio@studenti.unina.it">enr.demaio@studenti.unina.it</a>
 * @version 1.0
 */
@JsonSerialize(using = FlowSerializer.class)
public class Flow {
	public static short IPv4 = 0x0800;
	public static short ARP = 0x0806;
	
	@JsonProperty("switch")
	public String switchDpid;
	public String name="Test-Json";
	private final int cookie = (new Random()).nextInt();
	@JsonProperty("ether-type")
	public short etherType=-1;
	@JsonProperty("src-ip")
	public String srcIP;
	@JsonProperty("dst-ip")
	public String dstIP;
	@JsonProperty("src-mac")
	public String srcMac;
	@JsonProperty("dst-mac")
	public String dstMac;
	public int priority = 1;
	@JsonProperty("ingress-port")
	public int ingressPort;
	@JsonProperty("src-port")
	public int srcPort;
	@JsonProperty("dst-port")
	public int dstPort;
	public boolean active = true;
	public short protocol=-1;
	
	public List<String> actions = new ArrayList<String>();
	
	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		Flow f = new Flow();
		f.switchDpid = "00:00:72:5b:2d:c5:15:46";
		f.etherType = 0x0800;
		f.priority = 100;
		f.ingressPort = 2;
		f.actions.add("output=2");
		f.actions.add("set-dst-ip=192.168.1.2");
		//mapper.writeValue(System.out, f);
		FloodlightController fc = new FloodlightController(new Controller("192.168.1.181",8080));
		System.out.println(fc.addStaticFlow(new JSONObject(mapper.writeValueAsString(f))));
	}
	
}
