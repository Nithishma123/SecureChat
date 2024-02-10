import camellia.Camellia;
import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;


public class Server{

    static String getAlphaNumericString(int n) 
    { 
  
        // chose a Character random from this String 
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                    + "0123456789"
                                    + "abcdefghijklmnopqrstuvxyz"; 
  
        // create StringBuffer size of AlphaNumericString 
        StringBuilder sb = new StringBuilder(n); 
  
        for (int i = 0; i < n; i++) { 
  
            // generate a random number between 
            // 0 to AlphaNumericString variable length 
            int index 
                = (int)(AlphaNumericString.length() 
                        * Math.random()); 
  
            // add Character one by one in end of sb 
            sb.append(AlphaNumericString 
                          .charAt(index)); 
        } 
  
        return sb.toString(); 
    } 
public static void main(String[] args)
    {

        if(args.length != 1)
        {
            System.err.println("Invalid arguments.\nUsage: java Server <port S>");
            System.exit(0);
        }

        System.out.println("Beginning server...\nCtrl-C to quit.");

        try
        {
            ServerSocket s = new ServerSocket(Integer.parseInt(args[0]));

            while(true)
            {
                System.out.println("Listening...");
                Socket socket = s.accept();
                System.out.println("Connection accepted!");
                new ServerThread(socket).start();
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

class ServerThread extends Thread
{
    private DataInputStream _in;
    private DataOutputStream _out;
    private Socket _incoming;

    public ServerThread(Socket i) throws Exception
    {
        System.out.println("Creating new thread...");
        this._incoming = i;
        this._in = new DataInputStream(i.getInputStream());
        this._out = new DataOutputStream(i.getOutputStream());
        System.out.println("Created.\nBeginning communication...");
    }

    public void run()
    {
        try
        {	
            byte[][] payload=new byte[6][100];
            System.out.println("Receiving message...");
            String kab = Server.getAlphaNumericString(16);
            String kbs = "kbsfghjkloiuytre";
            String kas = "kasfghjkloiuytre";
            byte[] kab_enc=Camellia.enc(kab,kas);
            byte[] kab_enc2=Camellia.enc(kab,kbs);
            kab_enc2=Camellia.double_enc(kab_enc2,kas);
            
            ObjectInputStream ois = new ObjectInputStream(this._in);  
            String[] message = (String[])ois.readObject();   //recieving user details from A
            System.out.println("Received.");
   

            int timestamp = (int) (System.currentTimeMillis() / 1000L);
       		String t1=Integer.toString(timestamp);
                for(int i=0;i<6;i++){t1+='x';}
                
       		byte[] t=Camellia.enc(t1,kas);
       		byte[] t2=Camellia.enc(t1,kbs);
       		t2=Camellia.double_enc(t2,kas);

       		byte[] userA=Camellia.enc(message[0],kbs);


       		payload[0]=Camellia.enc(message[1],kas);
                payload[1]=kab_enc;
                payload[2]=t;
                payload[3]=kab_enc2;
                payload[4]=Camellia.double_enc(userA,kas);
                payload[5]=t2;
            
            ObjectOutputStream oos = new ObjectOutputStream(this._out);  
            oos.writeObject(payload);       //sendiing payload
           
            System.out.println("SESSION KEY SENT!!");
            this._incoming.close();
            System.out.println("Communication complete.\nConnection closed.");
        }
        catch (Exception e)
        {
            System.err.println("An exception occurred.");
            e.printStackTrace();
            System.exit(0);
        }
    }


}