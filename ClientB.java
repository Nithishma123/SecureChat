import camellia.Camellia;
import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class ClientB
{
    public static String finv(String message)
    {  String decryptedMessage="";char ch;
       for(int i = 0; i < message.length(); ++i){
			ch = message.charAt(i);
			if(ch=='\0')
                        ch = '\0';
			if(ch >= 'a' && ch <= 'z'){
	            ch = (char)(ch - 3);
	            
	            if(ch < 'a'){
	                ch = (char)(ch + 'z' - 'a' + 1);
	            }
	            
	            decryptedMessage += ch;
	        }
	        else if(ch >= 'A' && ch <= 'Z'){
	            ch = (char)(ch - 3);
	            
	            if(ch < 'A'){
	                ch = (char)(ch + 'Z' - 'A' + 1);
	            }
	            
	            decryptedMessage += ch;
	        }
	        else {
	        	decryptedMessage += ch;
	        }}
       return decryptedMessage;
    }

   

	public static void main(String[] args)
    {
	    if(args.length != 1 && args.length != 2)
        {
            System.err.println("Invalid arguments.\nUsage: java ClientB <port S> [delta]");
            System.exit(0);
        }

        int d = (args.length == 2) ? Integer.parseInt(args[1]) : 60;
        System.out.println("Beginning User Authentication- ClientB...\n\t[Delta: " + d + "]\nCtrl-C to quit.");

        try
        {
            ServerSocket s = new ServerSocket(Integer.parseInt(args[0]));

            while(true)
            {
                System.out.println("Listening...");
                Socket socket = s.accept();
                System.out.println("Connection accepted!");
                new ClientThread(socket, d).start();
            }
        }
        catch (Exception e)
        {
            System.err.println("An exception occurred.");
            e.printStackTrace();
            System.exit(0);
        }
    }
}

class ClientThread extends Thread
{
    private DataInputStream _in;
    private DataOutputStream _out;
    private Socket _incoming;
    private int _delta;

    public ClientThread(Socket i, int d) throws Exception
    {
        System.out.println("Creating new thread...");
        this._incoming = i;
        this._in = new DataInputStream(i.getInputStream());
        this._out = new DataOutputStream(i.getOutputStream());
        this._delta = d;
        System.out.println("Created.\nBeginning communication...");
    }

    public void run()
    {
        try
        {	
            String kbs = "kbsfghjkloiuytre";
            System.out.println("Receiving session key...");
     
            int timestamp = (int) (System.currentTimeMillis() / 1000L);
            
            ObjectInputStream ois = new ObjectInputStream(this._in);  	//receiving payload from A
            byte[][] payload = (byte[][])ois.readObject();  
            String[] pay2=new String[3];
            System.out.println("Received.");
          

             for(int i=0;i<3;i++){
                String temp=Camellia.dec(payload[i],kbs);       //decrypt payload received
                pay2[i]=temp;
            }

           if((Integer.parseInt(pay2[2].substring(0,10))) < (timestamp - this._delta) || (Integer.parseInt(pay2[2].substring(0,10))) > (timestamp + this._delta))
            {
                System.err.println("Invalid timestamp.\n\tCurrent: " + timestamp + "\n\tReceived: " + (pay2[2].substring(0,10)) + "\n\tDelta: " + this._delta);
                System.exit(0);
            }
           

            if("UserA Aayush007!".equals(pay2[1]))
            {
                System.err.println("Invalid B.\n\tExpected: UserA Aayush\n\tReceived: " + (pay2[1]));
                System.exit(0);
            }



            //Encrypt using Kab
            String kab=pay2[0].substring(0,16);ObjectOutputStream oos = new ObjectOutputStream(this._out);  
           
            System.out.print("Enter your message: ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String m = br.readLine();

            System.out.println("Sending Message for Authentication...");
            byte[] mess=Camellia.enc(m,kab);
            oos.writeObject(mess);
            System.out.println("Sent.");

 
            System.out.println("Receiving message...");
            byte[] message= (byte[])ois.readObject();
            
            String m1=Camellia.dec(message,kab); // f(m)
    
            
            System.out.println("Decrypted.");

            String aut = ClientB.finv(m1);
            String reply;
            if(m.equals(aut.substring(0,16).trim()))
            {
            System.out.println("AUTHENTICATED......");reply="AUTHENTICATED...";
            }
            else{
            System.out.println("NETWORK ATTACKED...........");reply="NETWORK ATTACKED";
            System.exit(0);
            }
            mess=Camellia.enc(reply,kab);
            oos.writeObject(mess);

            
            while((!reply.substring(0,4).equals("exit"))&&(!m.substring(0,4).equals("exit"))){
                        byte[] rep = (byte[])ois.readObject();
                        reply=Camellia.dec(rep,kab);
                        System.out.println("UserA : "+reply);
                        System.out.print("UserB : ");
                        m = br.readLine();if(m.equals("exit")){System.out.print("Connection closed.");byte[] mess1=Camellia.enc(m,kab);
                        oos.writeObject(mess1);System.exit(0);break;}
                        byte[] mess1=Camellia.enc(m,kab);
                        oos.writeObject(mess1);
            }
        }
        catch (Exception e)
        {
            
            System.out.print("Connection closed.");
            System.exit(0);

        }
    }
}
