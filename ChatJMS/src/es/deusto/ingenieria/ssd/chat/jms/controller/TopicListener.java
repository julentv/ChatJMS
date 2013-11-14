package es.deusto.ingenieria.ssd.chat.jms.controller;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;

public class TopicListener implements MessageListener {
	private Controller controller;
	

	public TopicListener(Controller controller) {
		super();
		this.controller = controller;
	}
	@Override
	public void onMessage(Message message) {		
		if (message != null) {
			try {
				System.out.println("   - TopicListener: " + message.getClass().getSimpleName() + " received!");
				
				if (message.getClass().getCanonicalName().equals(ActiveMQTextMessage.class.getCanonicalName())) {
					System.out.println("     - TopicListener: TextMessage '" + ((TextMessage)message).getText());
				} else if (message.getClass().getCanonicalName().equals(ActiveMQMapMessage.class.getCanonicalName())) {
					System.out.println("     - TopicListener: MapMessage");				
					MapMessage mapMsg = ((MapMessage) message);
					
					@SuppressWarnings("unchecked")
					Enumeration<String> mapKeys = (Enumeration<String>)mapMsg.getMapNames();
					String key = null;
					
					while (mapKeys.hasMoreElements()) {
						key = mapKeys.nextElement();
						System.out.println("       + " + key + ": " + mapMsg.getObject(key));
					}								
				}
			
			} catch (Exception ex) {
				System.err.println("# TopicListener error: " + ex.getMessage());
			}
		}
		
	}
	private es.deusto.ingenieria.ssd.chat.jms.data.Message generateMessage(Message message) throws JMSException{
		es.deusto.ingenieria.ssd.chat.jms.data.Message mensajeParseado= new es.deusto.ingenieria.ssd.chat.jms.data.Message();
		mensajeParseado.setMessageType(message.getIntProperty("messageType"));
		//this.message.setFrom(this.userList.getUserByNick());
		
		return mensajeParseado;
	}
}
