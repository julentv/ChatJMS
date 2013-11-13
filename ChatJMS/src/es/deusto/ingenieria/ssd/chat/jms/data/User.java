package es.deusto.ingenieria.ssd.chat.jms.data;

public class User {	
	private String nick;
	
	public User(){};
	public User(String nick){
		this.nick=nick;
	}


	public String getNick() {
		return nick;
	}
	
	public void setNick(String nick) {
		this.nick = nick;
	}
		
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass())) {			
			return this.nick.equalsIgnoreCase(((User)obj).nick);				  
		} else {
			return false;
		}
	}
	public String toString(){
		return "Nick: "+this.nick;
		
	}
}