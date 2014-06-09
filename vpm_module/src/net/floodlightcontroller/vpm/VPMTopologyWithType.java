package net.floodlightcontroller.vpm;

import java.io.IOException;

import org.openflow.util.HexString;

import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LinkDirection;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LinkType;
import net.floodlightcontroller.routing.Link;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using=VPMTopologyWithType.class)
public class VPMTopologyWithType extends JsonSerializer<VPMTopologyWithType>{

	public long srcSwDpid;
    public String srcIp;
    public String srcPort;
    public long dstSwDpid;
    public String dstIp;
    public String dstPort;
    public LinkType type;
    public LinkDirection direction;

    
    // Do NOT delete this, it's required for the serializer
    public VPMTopologyWithType(){}
    
    
    public VPMTopologyWithType(Link link, String srcIp, String dstIp, String srcPortName, 
    		String dstPortName, LinkType type, LinkDirection direction) {
    	this.srcSwDpid = link.getSrc();
        this.srcIp = srcIp.substring(1).split(":")[0];
        this.srcPort = srcPortName+"/"+link.getSrcPort();
        this.dstSwDpid = link.getDst();
        this.dstIp = dstIp.substring(1).split(":")[0];
        this.dstPort = dstPortName+"/"+link.getDstPort();
        this.type = type;
        this.direction = direction;
    }
    
	@Override
	public void serialize(VPMTopologyWithType lwt, JsonGenerator jgen,
			SerializerProvider arg2) throws IOException,
			JsonProcessingException {
		
		// You ****MUST*** use lwt for the fields as it's actually a different object.
        jgen.writeStartObject();
//        jgen.writeStartArray();
//        
//        jgen.writeStartObject();
//        jgen.writeStringField("dpid", HexString.toHexString(lwt.srcSwDpid));
//        jgen.writeEndObject();
//        
//        jgen.writeStartObject();
        jgen.writeStringField("src-switch", HexString.toHexString(lwt.srcSwDpid));
        jgen.writeStringField("src-inet", lwt.srcIp);
        jgen.writeStringField("src-port", lwt.srcPort);
        jgen.writeStringField("dst-switch", HexString.toHexString(lwt.dstSwDpid));
        jgen.writeStringField("dst-inet", lwt.dstIp);
        jgen.writeStringField("dst-port", lwt.dstPort);
        jgen.writeStringField("type", lwt.type.toString());
        jgen.writeStringField("direction", lwt.direction.toString());
//        jgen.writeEndObject();
        
//        jgen.writeEndArray();
        jgen.writeEndObject();
		
	}
	
	@Override
    public Class<VPMTopologyWithType> handledType() {
        return VPMTopologyWithType.class;
    }

}
