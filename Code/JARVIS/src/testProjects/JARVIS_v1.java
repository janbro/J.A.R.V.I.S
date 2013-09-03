package testProjects;

import gvjava.org.json.JSONObject;
import gvjava.org.json.XML;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.google.code.chatterbotapi.*;

//TO DO: Calendar Memory. Weighting system, delete least weighted memory in the event of no storage space available.

public class JARVIS_v1 {
	
	public static void main(String[] args) throws Exception{
		String fPath="C:\\Users\\Janbro\\Documents\\J.A.R.V.I.S\\Resources";
		String fileName="LastMessage.txt";
		String filePath = fPath+"\\"+fileName;
		FileReader file1 = new FileReader(filePath);
		BufferedReader br = new BufferedReader(file1);
		Double lastLastTime=-1.0;//Used to not resend txts at rerun
		String lastClvrBotMess=null;
		String tempStr = br.readLine();
		if(tempStr!=null){
			lastLastTime = round(Double.parseDouble(tempStr),2);
			lastClvrBotMess = br.readLine();
		}br.close();
		ArrayList<String> numbers = new ArrayList<String>();//Time, Numbers and message;
		VirtualPhone vp = new VirtualPhone();//Google Voice Phone
		JSONObject json = null;//Google Voice sms json
		String clvrBotMess=null;//Latest txt msg
		String s = null;//AI response
		String memFilePath=null;
		double lastTime=-1;
		boolean waitingForInput=false;
		int count=0;//Num of txt in mem
		while(true){
		count=findFilesInDir(fPath+"\\Memories").length;
		try {
				json = XML.toJSONObject(vp.getPhone().getSMS());
				// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(json!=null){
			String messages=json.toString();
			//System.out.println(messages);
			while(messages.indexOf("phoneNumber")!=-1||messages.indexOf("gc-message-sms-from")!=-1){
			
				if(messages.indexOf("gc-message-sms-from")!=-1){//(messages.indexOf("gc-message-sms-from")!=-1){
					messages = messages.substring(messages.indexOf("gc-message-sms-from"));
					messages = messages.substring(messages.indexOf("\\n\\r")+5);
					String time = messages.substring(messages.indexOf("gc-message-sms-time"));
					time = time.substring(time.indexOf("\\n\\r")+5);
					time = time.substring(0,time.indexOf("\\n\\r"));
					time = time.replace(" ", "");
					String message = null;
					if(messages.indexOf("gc-message-sms-text")!=-1){
						message = messages.substring(messages.indexOf("gc-message-sms-text"));
						message = message.substring(message.indexOf(">")+1);
						message = message.substring(0,message.indexOf("<\\"));
					}
					boolean temp=(message!=null)?numbers.add(time+"-"+messages.substring(0,messages.indexOf(":")).replace(" ","")+"(<#>)\""+message+"\""):numbers.add(time+"-"+messages.replace(" ","")+"(<#>)\"\"");
				}else if(messages.indexOf("phoneNumber")!=-1&&messages.indexOf("phoneNumber")<=messages.indexOf("gc-message-sms-from")){
					messages = messages.substring(json.toString().indexOf("phoneNumber"));
					String time = messages.substring(messages.indexOf("relativeStartTime"));
					time = time.substring(time.indexOf(":")+3);
					time = time.substring(0,time.indexOf("\\\""));
					//System.out.println(time);
					messages = messages.substring(messages.indexOf("+"));
					String message = messages.substring(messages.indexOf("messageText"));
					message = message.substring(message.indexOf(":")+2);
					numbers.add(time+"-"+messages.substring(0,messages.indexOf("\"")-1)+"(<#>)\""+message.substring(0,message.indexOf("\\"))+"\"");
				}
			}
			for(int i=0;i<numbers.size();i++){
				if(numbers.get(i).toLowerCase().contains("9547323823")&&numbers.get(i).contains("-")/*&&timeToInt(numbers.get(i).substring(0,numbers.get(i).indexOf("-")))>lastTime*/){
					lastTime = round(timeToInt(numbers.get(i).substring(0,numbers.get(i).indexOf("-"))),2);
					clvrBotMess=numbers.get(i).substring(numbers.get(i).lastIndexOf("(<#>)")+5);
				}
				//System.out.println(numbers.get(i));
			}
			if(lastTime!=lastLastTime&&lastClvrBotMess!=clvrBotMess){ //New message
				if(waitingForInput==true){
					if(clvrBotMess.toLowerCase().contains("yes")){
						String tempFilePath = memFilePath;
						FileReader tempFile1 = new FileReader(tempFilePath);
						BufferedReader tempBr = new BufferedReader(tempFile1);
						String mess="";
						
				        String line = tempBr.readLine();
				        
				        while(line!=null){
				        	mess+=line;
				 	       	line = tempBr.readLine();
				        }
				        
				        tempBr.close();
				        s = "Here is what you were looking for, sir:[BEGIN]\n"+mess+"\n[END]";
				        vp.getPhone().sendSMS("9547323823", s);
					}
					else if(clvrBotMess.toLowerCase().contains("no")){
						s = "Sorry, try more specific details.";
						vp.getPhone().sendSMS("9547323823", s);
					}
					waitingForInput=false;
				}
				else if(clvrBotMess.toLowerCase().contains("jarvis")||clvrBotMess.toLowerCase().contains("j.a.r.v.i.s.")){
					if(clvrBotMess.toLowerCase().contains("remember")||clvrBotMess.toLowerCase().contains("which")){
						if(clvrBotMess.toLowerCase().contains("remember this")){
							BufferedWriter bw = new BufferedWriter(new FileWriter(fPath+"\\Memories\\mem"+String.valueOf(count+1)+".txt"));
							bw.write(clvrBotMess.substring(clvrBotMess.indexOf(":")+1));
							bw.close();
							s = "I have successfully memorized that for you!";
							vp.getPhone().sendSMS("9547323823", s);
						}else if(clvrBotMess.toLowerCase().contains("do")||clvrBotMess.toLowerCase().contains("which")){
							if(clvrBotMess.toLowerCase().contains("you")||clvrBotMess.toLowerCase().contains("which")){
								s = "were in";
								String totMem="";
								double topCertainty=0.0;//Add cut off(top>.1 starting, else none found)
								String topFileName=null;
								for(File f:findFilesInDir(fPath+"\\Memories")){
									String tempFilePath = fPath+"\\Memories\\"+f.getName();
									FileReader tempFile1 = new FileReader(tempFilePath);
									BufferedReader tempBr = new BufferedReader(tempFile1);
							        String line = tempBr.readLine();
							        //boolean containsPhrase=false;
							        double correctWordCount=0;
							        double wordCount=0;
							        while(line!=null){
							        	String temp = line;
							        	for(String str:clvrBotMess.split(" ")){
							        		if(temp.toLowerCase().replace("!", " ").replace(")", " ").replace("(", " ").replace(".", " ").replace("?", " ").replace(",", " ").replace("\"", " ").replace("'", " ").replace(".", " ").contains(str.replace("!", " ").replace(")", " ").replace("(", " ").replace(".", " ").replace("?", " ").replace(",", " ").replace("\"", " ").replace("'", " ").replace(".", " "))){
							        			correctWordCount++;
							        		}wordCount++;
							        	}
							 	       	line = tempBr.readLine();
							        }if(wordCount!=0&&correctWordCount/wordCount>topCertainty){
							        	topCertainty=correctWordCount/wordCount;
							        	topFileName=f.getName();
							        }
							        tempBr.close();
								}
								//System.out.println("Are you speaking of:"+topFileName+"?");//File name placeholder(for now)
								s = "Are you speaking of "+topFileName+"?";
								vp.getPhone().sendSMS("9547323823", s);
								memFilePath=fPath+"\\Memories\\"+topFileName;
								waitingForInput=true;
							}
						}
					}else if(clvrBotMess.toLowerCase().contains("thank you")){
						if((int)(Math.random()*2)==0){
							s = "You are very welcome, monsiuer.";
							vp.getPhone().sendSMS("9547323823", s);
						}
						else{
							s = "I strive to do my best sir.";
							vp.getPhone().sendSMS("9547323823", s);
						}
					}
				}else{
					ChatterBotFactory factory = new ChatterBotFactory();
				
					ChatterBot bot2 = factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
					ChatterBotSession bot2session = bot2.createSession();
				
					s=clvrBotMess;
					s = bot2session.think(s);
					System.out.println(">>"+s);
				
					vp.getPhone().sendSMS("9547323823", s);
				}
			}
			System.out.println(lastTime+":"+clvrBotMess+"\n"+s);
			BufferedWriter out = new BufferedWriter(new FileWriter(filePath,false));
			out.write(String.valueOf(lastTime)); 
			out.newLine();
			out.write(clvrBotMess);
			out.close();
			lastLastTime=lastTime;
			lastClvrBotMess=clvrBotMess;
			}
		}
	}
	

	private static double timeToInt(String time){
		if(time.contains("ago")||time.contains("now")){
			double date;
			if(time.contains("hours"))
				date = (double)Calendar.HOUR_OF_DAY-Integer.parseInt(time.substring(0,time.indexOf(" ")))+(Calendar.MINUTE/100);
			else if(time.contains("minutes"))
				date = (double)Calendar.HOUR_OF_DAY+(Calendar.MINUTE-Integer.parseInt(time.substring(0,time.indexOf(" "))))/100;
			else
				date = (double)Calendar.HOUR_OF_DAY+(Calendar.MINUTE/100);
			//System.out.println(Calendar.HOUR_OF_DAY);
			//System.out.println(date);
			return date;
		}
		double doubleTime;
		doubleTime = Integer.parseInt(time.substring(0,time.indexOf(":")));
		doubleTime=doubleTime+round(Double.parseDouble(time.substring(time.indexOf(":")+1,time.indexOf(":")+3))/100,2);
		if(time.contains("PM"))
			doubleTime+=12.0;
		return doubleTime;
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
	public static File[] findFilesInDir(String dirName){
	    File dir = new File(dirName);

	    return dir.listFiles(new FilenameFilter() { 
	             public boolean accept(File dir, String filename)
	                  { return filename.toLowerCase().endsWith(".txt"); }
	    } );

	}
}
