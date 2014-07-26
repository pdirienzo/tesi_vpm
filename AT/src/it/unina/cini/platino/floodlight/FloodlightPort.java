package it.unina.cini.platino.floodlight;

/**
 * A class representing a Floodlight port
 * 
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
public class FloodlightPort {

	public String name;
	public int number;

	public FloodlightPort(String s){
		if(!s.equals("0")){
			String[] spl = s.split("/");
			name = spl[0];
			number = Integer.parseInt(spl[1]);
		}else{
			name = "";
			number = 0;
		}
	}

	public FloodlightPort(String name, int number){
		this.name = name;
		this.number = number;
	}

	public boolean equals(Object o){
		FloodlightPort p = (FloodlightPort)o;
		return (/*p.name.equals(this.name) &&*/ (p.number == this.number));
	}
	
	@Override
	public int hashCode() {
		return (String.valueOf(this.number)).hashCode();
	}
	
	public String toString(){
		return this.name+"/"+this.number;
	}

}
