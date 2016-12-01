package server;

import java.io.Serializable;

/* Rather than send complicated time specific messages to and from the server
 * we created a type called 'datapackages' this allows you to send data via
 * the socket with a title, so rather than sending a random number: 1249124, 
 * we send it with a title: "TimeStamp", 1249124. This allows us to use simple
 * conditional logic to allow the server and client to 'understand' what it's
 * being sent
 * Within this class there are just standard setters and getters, and constructors
 */
public class DataPackages implements Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private String				type;
	private Object				message;

	public DataPackages() {
	}

	public DataPackages(String type, Object message) {
		this.type = type;
		this.message = message;
	}

	public DataPackages(String type) {
		this.type = type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public void setObject(Object message) {
		this.message = message;
	}

	public Object getObject() {
		return this.message;
	}
}