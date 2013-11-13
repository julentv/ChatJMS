package es.deusto.ingenieria.ssd.chat.jms.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

import es.deusto.ingenieria.ssd.chat.jms.controller.Controller;
import es.deusto.ingenieria.ssd.chat.jms.exceptions.IncorrectMessageException;



public class MulticastClient extends Thread {
	
	private Controller controller;
	
	public MulticastClient(Controller controller){
		this.controller=controller;
	}
	
	public DatagramPacket receiveDatagramPacket(){
		try  {
			byte[] buffer = new byte[1024];			
			DatagramPacket messageIn =new DatagramPacket(buffer, buffer.length);
			controller.multicastSocket.receive(messageIn);
			System.out.println(" - Received a message from '" + messageIn.getAddress().getHostAddress() + ":" + messageIn.getPort() + 
		                   		   "' -> " + new String(messageIn.getData()));
			return messageIn;
		} catch (SocketException e) {
			System.err.println("# Socket Error: " + e.getMessage());
			return null;
		} catch (IOException e) {
			System.err.println("# IO Error: " + e.getMessage());
			return null;
		}
	}
	
	@Override
	public void run(){
		while(true){
			try {
			DatagramPacket receivedPacket= receiveDatagramPacket();
			String receivedMessage= new String(receivedPacket.getData());
			receivedMessage= receivedMessage.trim();
			
				controller.proccesInputMessage(receivedMessage);
			} catch (IncorrectMessageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	catch (NullPointerException np){
				System.out.println("Thread is stopped because the socket is stopped.");
				break;
			}
		}
			
		
			
	}
			
}


