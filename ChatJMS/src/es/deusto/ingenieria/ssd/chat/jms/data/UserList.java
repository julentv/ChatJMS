package es.deusto.ingenieria.ssd.chat.jms.data;

import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.DefaultListModel;

import es.deusto.ingenieria.ssd.chat.jms.data.User;

public class UserList implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<User> listOfUsers;

	public UserList() {
		listOfUsers=new ArrayList<User>();
	}
	public void fromString(String users){
		listOfUsers=new ArrayList<User>();
		String[] arrUsers = users.split("&");
		if (arrUsers.length!=0) {
			for (String user : arrUsers) {
				listOfUsers.add(new User(user));
			}
		}
	}
	
	public ArrayList<User> getListOfUsers() {
		return listOfUsers;
	}

	public User getLastUser(){
		return listOfUsers.get(listOfUsers.size()-1);
	}
	/**
	 * 
	 * @param nick
	 * @return the user if exists null if not.
	 */
	public User getUserByNick (String nick){
		for(User u:listOfUsers){
			if(u.getNick().equals(nick)) return u;
		}
		return null;
	}
	
	/**
	 * Format of the string: &nick0&nick1&...nickn
	 * returns empty string if there are not Users in the list
	 */
	public String toString (){
		String listOfNicks="";
		for (User u:this.listOfUsers){
			listOfNicks=listOfNicks+'&'+u.getNick();
		}
		return listOfNicks;
	}
	public boolean deleteByNick(String nick){
		for(User u:listOfUsers){
			if (u.getNick().equals(nick)){
				listOfUsers.remove(u);
				return true;
			}
		}
		return false;
	}
	public void add(User u){
		this.listOfUsers.add(u);
	}

}
