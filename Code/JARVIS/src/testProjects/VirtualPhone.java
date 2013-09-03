package testProjects;

import java.io.IOException;
import com.techventus.server.voice.Voice;

public class VirtualPhone {
	
	private static String user = "username";
	private static String pass = "password";
	private static String myNum = "myPhoneNumber";
	private static Voice voice;
	
	public VirtualPhone(){
		voice = null;
		try {
			voice = new Voice(user,pass);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Voice getPhone(){
		return voice;
	}
	
	//Debug Purposes only(Use Voice.method() instead, not these!)
	public static boolean call(String destinationNumber){
		if(voice!=null)
			try{
				voice.call(destinationNumber, myNum, "1");
			} catch(IOException e){
				e.printStackTrace();
			}
		else
			return false;
		return true;
	}

	//Debug Purposes only(Use Voice.method() instead, not these!)
	public static boolean sendSMS(String destinationNumber,String message){
		if(voice!=null)
			try {
				
				voice.sendSMS(destinationNumber, message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else
			return false;
		return true;
	}

}











