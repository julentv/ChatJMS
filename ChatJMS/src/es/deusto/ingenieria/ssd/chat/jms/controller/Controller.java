package es.deusto.ingenieria.ssd.chat.jms.controller;

import java.awt.Color;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.activemq.ActiveMQConnectionFactory;

import es.deusto.ingenieria.ssd.chat.jms.client.MulticastClient;
import es.deusto.ingenieria.ssd.chat.jms.data.Message;
import es.deusto.ingenieria.ssd.chat.jms.data.User;
import es.deusto.ingenieria.ssd.chat.jms.data.UserList;
import es.deusto.ingenieria.ssd.chat.jms.exceptions.IncorrectMessageException;
import es.deusto.ingenieria.ssd.chat.jms.gui.JFrameMainWindow;


public class Controller {
	public String ip;
	// private static final String DEFAULT_IP = "228.5.6.7";
	private int port;
	private InetAddress group;
	private JFrameMainWindow window;
	public MulticastSocket multicastSocket;
	public User connectedUser;
	public User chatReceiver;
	private Message message;
	private UserList userList;
	private SimpleDateFormat textFormatter = new SimpleDateFormat("HH:mm:ss");
	// indica si a llegado el primer mensaje de login
	private boolean firstArrived;
	private boolean alreadyExistsSent;
	
	private String topicName = "ChatTopic";		
	
	private TopicConnection topicConnection = null;
	private TopicSession topicSession = null;
	private TopicPublisher topicPublisher = null;
	private TopicSubscriber topicSubscriber = null;
	private String subscriberID = "SubscriberID";
	
	public static final String MESSAGE_TYPE= "messageType";
	public static final String NICK_TO= "nickTo";
	public static final String NICK_FROM= "nickFrom";
	

	// tiene a la ventana y el hilo
	public Controller(JFrameMainWindow jFrameMainWindow) {
		window = jFrameMainWindow;
	}
	public void initFlags(){
		firstArrived = false;
		alreadyExistsSent = false;
	}

	public User getConnectedUser() {
		return connectedUser;
	}

	public void setConnectedUser(User connectedUser) {
		this.connectedUser = connectedUser;
	}

	public UserList getUserList() {
		return userList;
	}

	public void setUserList(UserList userList) {
		this.userList = userList;
	}

	public void sendMessage(Message message) {
		
		publishMessage(message);
	}

	public void proccesInputMessage(Message message)
			throws IncorrectMessageException {
		// el switch case con todos los mensajes aqui.
		String messageToSend;
		String warningMessage;
		String time;
		
		// Si el que envia el sms no soy yo mirar si el sms es para mi
		if (this.firstArrived){
			if (!(this.alreadyExistsSent && this.message.isNickAlreadyExistMessage())) {
				boolean logginOrExisting=this.message.isLogginMessage()||this.message.isNickAlreadyExistMessage();
				if (logginOrExisting
						|| (!logginOrExisting && connectedUser!=null&& !this.message
								.getFrom().getNick()
								.equals(connectedUser.getNick()))) {
					

					// si el sms es para mi procesar
					if (this.message.getTo() == null
							|| this.message.getTo().getNick()
									.equals(connectedUser.getNick())) {

						switch (message.getMessageType()) {

						case Message.CLIENT_MESSAGE_LOGIN:
							if (userList.getUserByNick(this.message.getFrom()
									.getNick()) == null) {
								// si no exist el ultimo de la lista envia la
								// lista de usuarios

								if (userList.getLastUser().getNick()
										.equals(connectedUser.getNick())) {
									this.userList.add(this.message.getFrom());
									Message list= new Message(Calendar.getInstance().getTimeInMillis(),null, 108, connectedUser, null);
//									
									publishMessage(list);
								} else {
									this.userList.add(this.message.getFrom());
								}
								this.window.refreshUserList();

							} else {
								// si hay nick ese usuario envia el sms de error
								// 301
								if (this.message.getFrom().getNick()
										.equals(connectedUser.getNick())) {

									messageToSend = "301&"
											+ this.connectedUser.getNick();
									this.alreadyExistsSent = true;
									sendDatagramPacket(messageToSend);
									
								}
							}

							break;
						case Message.CLIENT_MESSAGE_ESTABLISH_CONNECTION:
							
							if (this.chatReceiver == null) {
								String messageToConnect="Do you want to start a new chat session with '" + message.getFrom()
										.getNick() + "'";

								boolean acceptInvitation = this.window
										.acceptWindow(messageToConnect, "Open chat session");
								if (acceptInvitation) {
									this.chatReceiver = new User(message
											.getFrom().getNick());
									messageToSend = "103&"
											+ this.connectedUser.getNick()
											+ "&" + chatReceiver.getNick();
									sendDatagramPacket(messageToSend);
									time = textFormatter.format(new Date());
									warningMessage = " " + time
											+ ": BEGINING OF THE CONVERSATION WITH ["
											+ this.chatReceiver.getNick() + "]\n";
									this.window.appendMessageToHistory(warningMessage,
											Color.GREEN);
									
								} else {
									messageToSend = "104&"
											+ this.connectedUser.getNick()
											+ "&" + message
											.getFrom().getNick();
									sendDatagramPacket(messageToSend);
								}
							} else {
								// si ya estoy hablando mandar already chatting
								messageToSend = "303&"
										+ this.connectedUser.getNick() + "&"
										+ message.getFrom().getNick();
								sendDatagramPacket(messageToSend);

							}

							break;
						case Message.CLIENT_MESSAGE_ACCEPT_INVITATION:
							this.chatReceiver=this.message.getFrom();
							time = textFormatter.format(new Date());
							warningMessage = " " + time
									+ ": BEGINING OF THE CONVERSATION WITH ["
									+ this.chatReceiver.getNick() + "]\n";
							this.window.appendMessageToHistory(warningMessage,
									Color.GREEN);
							break;
						case Message.CLIENT_MESSAGE_REJECT_INVITATION:
							warningMessage = message.getFrom().getNick()
									+ " has rejected your invitation";
							this.window.showMessage(warningMessage);
							break;
						case Message.CLIENT_MESSAGE_CLOSE_CONVERSATION:
							time = textFormatter.format(new Date());		
							warningMessage = " " + time + ": CONVERSATION FINISHED\n";
							this.window.appendMessageToHistory(warningMessage, Color.GREEN);
							this.window.listUsers.clearSelection();
							this.chatReceiver=null;
							break;
						case Message.CLIENT_MESSAGE_CLOSE_CONNECTION:
							this.userList.deleteByNick(this.message.getFrom()
									.getNick());
							this.window.refreshUserList();
							break;
						case Message.CLIENT_MESSAGE:
							time = textFormatter.format(new Date());
							warningMessage = " " + time + " - ["
									+ this.message.getFrom().getNick() + "]: "
									+ message.getText().trim() + "\n";
							this.window.appendMessageToHistory(warningMessage,
									Color.MAGENTA);
							break;
						case Message.CLIENT_MESSAGE_USER_LIST:
							this.userList.fromString(this.message.getText());
							this.window.refreshUserList();
							break;
						case Message.ERROR_MESSAGE_EXISTING_NICK:
							if(message.getFrom().getNick().equals(this.connectedUser.getNick())){
								this.window
								.showMessage("The introduced nick already exists");
						this.connectedUser = null;
						this.userList = new UserList();
						this.window.refreshUserList();
						this.window.toDisconnectionMode();
						this.multicastSocket.close();
							}
							
							break;
						case Message.ERROR_MESSAGE_USER_ALREADY_CHATTING:
							
							this.window
							.showMessage(message.getFrom().getNick()+" is already chatting.");
						default:
							throw new IncorrectMessageException(
									"The message type code does not exist");
						}
					}
				}
			} else {
				this.alreadyExistsSent = false;
				
			}
		} else {
			this.firstArrived = true;
		}

	}

//	private void sendDatagramPacket(String message) {
//		try {
//			DatagramPacket messageOut = new DatagramPacket(message.getBytes(),
//					message.length(), group, port);
//			multicastSocket.send(messageOut);
//			System.out.println(" - Sent a message to '"
//					+ messageOut.getAddress().getHostAddress() + ":"
//					+ messageOut.getPort() + "' -> "
//					+ new String(messageOut.getData()));
//
//		} catch (SocketException e) {
//			System.err.println("# Socket Error: " + e.getMessage());
//		} catch (IOException e) {
//			System.err.println("# IO Error: " + e.getMessage());
//		}
//	}

	public boolean isConnected() {
		return this.connectedUser != null;
	}

	public boolean isChatSessionOpened() {
		return this.chatReceiver != null;
	}

	public boolean connect(String ip, int port, String nick) throws IOException {
		
		try {
			this.initFlags();
			this.port = port;
			//connect
			//JNDI Initial Context
			Context ctx = new InitialContext();
			 //Connection Factory
			TopicConnectionFactory topicConnectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			
			topicConnection = topicConnectionFactory.createTopicConnection();
			TopicSession topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			Topic myTopic = topicSession.createTopic(topicName);
			System.out.println("- Topic Connection created!");
			
			//Topic Listener
			topicSubscriber = topicSession.createSubscriber(myTopic, null, false);
			TopicListener topicListener = new TopicListener(this);
			topicSubscriber.setMessageListener(topicListener);
			
			//Begin message delivery
			topicConnection.start();
			
			//Wait for messages
			System.out.println("- Waiting 10 seconds for messages...");
			Thread.sleep(10000);
			
			//TOPIC PUBLISHER
			//Message Publisher
			topicPublisher = topicSession.createPublisher(myTopic);
			System.out.println("- TopicPublisher created!");
												
			userList = new UserList();
			this.connectedUser = new User(nick);
			userList.add(connectedUser);
			Message connnectMessage= new Message(Calendar.getInstance().getTimeInMillis(), null, 101, this.connectedUser, null);
			//String message = "101&" + this.connectedUser.getNick();
			publishMessage(connnectMessage);
			
			return true;
		} catch (Exception e) {
			System.err.println("# TopicPublisherTest Error: " + e.getMessage());
			return false;
		} 
		finally {
			try {
				//Close resources
				topicSubscriber.close();
				topicPublisher.close();
				topicSession.close();
				topicConnection.close();
				System.out.println("- Topic resources closed!");				
			} catch (Exception ex) {
				System.err.println("# TopicPublisherTest Error: " + ex.getMessage());
			}			
		}
		
		
		
	}
	
	public void publishMessage(Message message){
		//Text Message
		javax.jms.Message publishMessage;
		
		try {
			
			//Message Headers
			if (message.getMessageType()==108){
				publishMessage = topicSession.createObjectMessage();
				publishMessage.setJMSType("ObjectMessage");
				((ObjectMessage) publishMessage).setObject(this.userList);
			}else{
				publishMessage = topicSession.createTextMessage();
				publishMessage.setJMSType("TextMessage");
				//Message body
				if(message.getText()!=null){
					((TextMessage) publishMessage).setText(message.getText());
				}
			}
			publishMessage.setJMSTimestamp(message.getTimestamp());
			//textMessage.setJMSMessageID("ID-1");
			
			//Message Properties
			publishMessage.setIntProperty("messageType", message.getMessageType());
			publishMessage.setStringProperty("nickTo", message.getTo().getNick());
			publishMessage.setStringProperty("nickFrom", message.getFrom().getNick());
			
			//Publish the Messages
			topicPublisher.publish(publishMessage);
			System.out.println("- TextMessage published in the Topic!");	
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public boolean disconnect() {
		
		
		try {
			if (isChatSessionOpened()) {
				sendChatClosure();
			}
			Message message= new Message(Calendar.getInstance().getTimeInMillis(),null, 106, this.connectedUser, null);
			//message = "106&"+this.connectedUser.getNick();
			//sendDatagramPacket(message);
			publishMessage(message);
			//Close resources
			topicSubscriber.close();
			topicPublisher.close();
			topicSession.close();
			topicConnection.close();
			System.out.println("- Topic resources closed!");
			this.connectedUser = null;
			this.chatReceiver = null;
			
			return true;
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}

	/**
	 * This method is used to send the message of closing the chat
	 * 
	 * @return true
	 */
	public boolean sendChatClosure() {

		// ENTER YOUR CODE TO SEND A CHAT CLOSURE
		String message = "105&" + this.connectedUser.getNick()+"&"+this.chatReceiver.getNick();
		sendDatagramPacket(message);
		this.chatReceiver = null;

		return true;
	}

	public void establishConnection(String nickToConnect){
		if(this.chatReceiver==null){
			String message="102&"+this.getConnectedUser().getNick()+"&"+nickToConnect;
			sendDatagramPacket(message);
		}else{
			String messageToReject="Do you want to close the conversation?";
			boolean close=this.window.acceptWindow(messageToReject, "Close chat session");
			if(close){
				String message="105&"+this.getConnectedUser().getNick()+"&"+this.chatReceiver.getNick();
				sendDatagramPacket(message);
				this.window.listUsers.clearSelection();
				String time = textFormatter.format(new Date());		
				message = " " + time + ": CONVERSATION FINISHED\n";
				this.window.appendMessageToHistory(message, Color.GREEN);
				this.chatReceiver=null;
				this.window.setTitle("Chat main window - 'Connected'");
			}
		}
	}
}
