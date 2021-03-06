package com.ociweb.gl.api;

import java.util.concurrent.atomic.AtomicInteger;

import com.ociweb.json.JSONExtractor;
import com.ociweb.pronghorn.network.ClientCoordinator;
import com.ociweb.pronghorn.util.TrieParser;
import com.ociweb.pronghorn.util.TrieParserReader;

public class ClientHostPortInstance {

	public static AtomicInteger sessionCounter = new AtomicInteger(0);
	
	public final String host;
	public final int hostId;
	public final byte[] hostBytes;
	public final int port;
	public final int sessionId; 
	public final TrieParser headers = null; //
	
	public final JSONExtractor extractor;
	
	//cache
	private long connectionId=-1;

	//TODO: add method to limit headers
	
	
	
	public String toString() {
		return host+":"+port;
	}
	
	public ClientHostPortInstance(String host, int port, int sessionId) {
		this(host,port,null,sessionId);
	}
	
	public ClientHostPortInstance(String host, int port) {
		this(host,port,null);
	}
	
	public ClientHostPortInstance(String host, int port, JSONExtractor extractor, int sessionId) {
		this.sessionId = sessionCounter.incrementAndGet();
		this.host = host;
		this.port = port;
		if (port<=0 || port>65535) {
			throw new UnsupportedOperationException("Invalid port "+port+" must be postive and <= 65535");
		}
		this.extractor = null;
		this.hostId = ClientCoordinator.registerDomain(host);
		this.hostBytes = host.getBytes();

	}

	
	public ClientHostPortInstance(String host, int port, JSONExtractor extractor) {
		this.sessionId = sessionCounter.incrementAndGet();
		this.host = host;
		this.hostId = ClientCoordinator.registerDomain(host);
		this.hostBytes = host.getBytes();
		this.port = port;
		if (port<=0 || port>65535) {
			throw new UnsupportedOperationException("Invalid port "+port+" must be postive and <= 65535");
		}
		this.extractor = null;
	}
	
	void setConnectionId(long id) {
		connectionId = id;		
	}
	
	long getConnectionId() {
		return connectionId;
	}

	public static int getSessionCount() {
		return sessionCounter.get();
	}
	
}
