package it.unipd.scd.scdcommunication;

public interface CommInterface {

	public void onMessage(String payload);
	
	public void onCommMessage(String message);
	
}
