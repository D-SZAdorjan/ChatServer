import java.nio.ByteBuffer;

public class Package {
	private char type; //1byte
	private int size; //4byte
	private String from; //8byte
	private String to; //8byte/elem
	private String data; //750KB ~ 750 000byte
	
	public Package(char type, int size, String from, String to, String message) {
		this.type = type;
		this.size = size;
		this.from = from;
		this.to = to;
		this.data = message;
	}
	
	public Package() {
		this.type = '0';
		this.size = -1;
		this.from = "";
		this.to = "";
		this.data = "";
	}
	
	void setType(char Type) {this.type = Type;}
	
	void setSize(int Size) {this.size = Size;}
	
	void setFrom(String From) {this.from = From;}
	
	void setTo(String To) {this.to = To;}
	
	void setMessage(String Message) {this.data = Message;}
	
	char getType() {return this.type;}
	
	int getSize() {return this.size;}
	
	String getFrom() {return this.from;}
	
	String getTo() {return this.to;}
	
	String getMessage() {return this.data;}
	
	void printPackage() {
		System.out.println("Type: " + getType());
		System.out.println("Size: " + getSize());
		System.out.println("From: " + getFrom());
		System.out.println("To: " + getTo());
		System.out.println("Message: " + getMessage());
	}
	
	String makePackage() {
		String pack = "";
		pack += this.type + "\r\n";
		pack += this.size + "\r\n";
		pack += this.from + "\r\n";
		pack += this.to + "\r\n";
		pack += this.data + "\r\n";
		return pack;
	}
}
