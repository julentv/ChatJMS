package es.deusto.ingenieria.ssd.chat.jms.data;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
	//Client message types
	public static final int CLIENT_MESSAGE_LOGIN=101;
	public static final int CLIENT_MESSAGE_ESTABLISH_CONNECTION=102;
	public static final int CLIENT_MESSAGE_ACCEPT_INVITATION=103;
	public static final int CLIENT_MESSAGE_REJECT_INVITATION=104;
	public static final int CLIENT_MESSAGE_CLOSE_CONVERSATION=105;
	public static final int CLIENT_MESSAGE_CLOSE_CONNECTION=106;
	public static final int CLIENT_MESSAGE=107;
	public static final int CLIENT_MESSAGE_USER_LIST=108;
	
	//ERROR MESSAGES
	public static final int ERROR_MESSAGE_EXISTING_NICK=301;
	public static final int ERROR_MESSAGE_CONNECTION_FAILED=302;
	public static final int ERROR_MESSAGE_USER_ALREADY_CHATTING=303;
	public static final int ERROR_MESSAGE_MESSAGE_ERROR=305;
	public static int [] NOTUSERTO={CLIENT_MESSAGE_LOGIN,CLIENT_MESSAGE_CLOSE_CONNECTION, CLIENT_MESSAGE_USER_LIST, ERROR_MESSAGE_EXISTING_NICK,ERROR_MESSAGE_CONNECTION_FAILED,ERROR_MESSAGE_MESSAGE_ERROR};
	//Messages that are received by the server and this only responds with a simple message
	
	private long timestamp;
	private String text;
	private int messageType;
	private User from;
	private User to;
	
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");
	
	public Message(){
		
	}
	
	public Message(long timestamp, String text, int messageType, User from,
			User to) {
		super();
		this.timestamp = timestamp;
		this.text = text;
		this.messageType = messageType;
		this.from = from;
		this.to = to;
	}

	public int getMessageType() {
		return messageType;
	}
	
	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public User getFrom() {
		return from;
	}
	
	public void setFrom(User from) {
		this.from = from;
	}
	
	public User getTo() {
		return to;
	}
	
	public void setTo(User to) {
		this.to = to;
	}
	
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass())) {
			Message otherMsg = (Message)obj;
			
			return this.timestamp == otherMsg.timestamp &&
				   this.text.equals(otherMsg.text) &&
				   this.from.equals(otherMsg.from) &&
				   this.to.equals(otherMsg.to);
		} else {
			return false;
		}
	}
	
	public String toString() {
		return "[" + dateFormatter.format(new Date(this.timestamp)) + "] '" + 
	           this.from + " -> " + this.to + " : " + this.text; 
				
	}
	public boolean hasUserTo(){
		for(int type:NOTUSERTO){
			if(type==this.messageType)
				return false;
		}
		return true;
	}
	public boolean isLogginMessage(){
		if (this.messageType==Message.CLIENT_MESSAGE_LOGIN)
			return true;
		else
			return false;
	}
	public boolean isNickAlreadyExistMessage(){
		if (this.messageType==Message.ERROR_MESSAGE_EXISTING_NICK)
			return true;
		else
			return false;
	}
}