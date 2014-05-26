package org.at.network.types;



public class Port {

	public String name;
	public int number;

	public Port(String s){
		if(!s.equals("0")){
			String[] spl = s.split("/");
			name = spl[0];
			number = Integer.parseInt(spl[1]);
		}else{
			name = "";
			number = 0;
		}
	}

	public Port(String name, int number){
		this.name = name;
		this.number = number;
	}

	public boolean equals(Object o){
		Port p = (Port)o;
		return (/*p.name.equals(this.name) &&*/ (p.number == this.number));
	}
	
	public String toString(){
		return this.name+"/"+this.number;
	}

}
