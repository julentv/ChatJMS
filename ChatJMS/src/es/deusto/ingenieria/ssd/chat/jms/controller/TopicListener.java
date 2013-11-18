package es.deusto.ingenieria.ssd.chat.jms.controller;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import es.deusto.ingenieria.ssd.chat.jms.data.User;
import es.deusto.ingenieria.ssd.chat.jms.data.UserList;
import es.deusto.ingenieria.ssd.chat.jms.exceptions.IncorrectMessageException;

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
								
				es.deusto.ingenieria.ssd.chat.jms.data.Message mensajeParseado=generateMessage(message);
				System.out.println("Recibido de: "+ mensajeParseado.getFrom().getNick()+" mensaje: "+mensajeParseado.getMessageType());
				this.controller.proccesInputMessage(mensajeParseado);
				System.out.println("fin del onMessage");
			} catch (JMSException ex) {
				System.err.println("# TopicListener error: " + ex.getMessage());
			} catch (IncorrectMessageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	private es.deusto.ingenieria.ssd.chat.jms.data.Message generateMessage(Message message) throws JMSException{
		es.deusto.ingenieria.ssd.chat.jms.data.Message mensajeParseado= new es.deusto.ingenieria.ssd.chat.jms.data.Message();
		mensajeParseado.setMessageType(message.getIntProperty("messageType"));
		mensajeParseado.setFrom(controller.getUserList().getUserByNick(message.getStringProperty(Controller.NICK_FROM)));
		if (mensajeParseado.getFrom() == null){
			//si el usuario del que proviene el mensaje no existe en la lista se crea
			mensajeParseado.setFrom(new User(message.getStringProperty(Controller.NICK_FROM)));
		}
		if(!message.getStringProperty(Controller.NICK_FROM).equals("")){
			//hay user to --> meterlo en el mensaje
			mensajeParseado.setTo(controller.getUserList().getUserByNick(message.getStringProperty(Controller.NICK_FROM)));
		}
		if(mensajeParseado.isListUserMessage()){
			//el mensaje recibido es un ObjectMessage
			controller.setUserList((UserList)((ObjectMessage)message).getObject());
		}else{
			//el mensaje recibido es un TextMessage
			mensajeParseado.setText(((TextMessage)message).getText());
		}
		
		return mensajeParseado;
	}
}
