import camellia.Camellia;
import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.*; 

public class ClientA
{

    public static String f(String text){
        String result ="";
        for (int i=0; i<text.length(); i++) 
        {   if(Character.isLetter(text.charAt(i))){
            if (Character.isUpperCase(text.charAt(i))) 
            { 
                char ch = (char)(((int)text.charAt(i)+ 3 - 65) % 26 + 65); 
                result +=ch; 
            } 
            else
            { 
                char ch = (char)(((int)text.charAt(i) + 3 - 97) % 26 + 97); 
                result +=ch; 
            }
        }
        else{
            result +=text.charAt(i);
        }  
        } 
        return result; 
    }




    public static void main(String[] args) throws Exception
    {
        if(args.length != 4 && args.length != 5)
        {
            System.err.println("Invalid arguments.\nUsage: java ClientA <IP B> <port B> <IP S> <port S> [delta]");
            System.exit(0);
        }

        try
        {   
            String kas = "kasfghjkloiuytre";
            System.out.println("Initialising User Authentication Protocol transaction...");
            int delta = (args.length == 5) ? Integer.parseInt(args[4]) : 60;
            System.out.println("\t[Delta = " + delta + "]\n\t[Server At: " + args[2] + ":" + args[3] + "]\n\t[Target At: " + args[0] + ":" + args[1] + "]\nInitialising connections...");

            Socket clientb = new Socket(args[0], Integer.parseInt(args[1]));
            System.out.println("Connected to Target!");
            Socket server = new Socket(args[2], Integer.parseInt(args[3]));
            System.out.println("Connected to Server!");
  

            DataInputStream clientb_reader = new DataInputStream(clientb.getInputStream());
            DataInputStream server_reader = new DataInputStream(server.getInputStream());

            DataOutputStream clientb_writer = new DataOutputStream(clientb.getOutputStream());
            DataOutputStream server_writer = new DataOutputStream(server.getOutputStream());

            String[] message = {"UserA Aayush007!","UserB Nithishma!"};


            System.out.println("Sending message...");

            ObjectOutputStream oos = new ObjectOutputStream(server_writer);  
            oos.writeObject(message);       //sending identities
            System.out.println("Message sent.");


            System.out.println("Receiving message...");
            int timestamp = (int) (System.currentTimeMillis() / 1000L);
            
            ObjectInputStream ois = new ObjectInputStream(server_reader);  //receiving payload
            byte[][] payload = (byte[][])ois.readObject(); 
            String[] pay1=new String[3];

            server.close();
            for(int i=0;i<6;i++){
                if(i<3){
                    String temp=Camellia.dec(payload[i],kas);       //decrypt payload
                    pay1[i]=temp;
                }
                else{
                    byte[] temp=Camellia.double_dec(payload[i],kas);       //decrypt payload
                    payload[i]=temp;
                }
               
            }
            byte[][] payB={payload[3],payload[4],payload[5]};
             

            System.out.println("Received."); 
 
            System.out.println("Sending {T, A, k<A,B>} to B...");
           if((Integer.parseInt(pay1[2].substring(0,10))) < (timestamp - delta) || (Integer.parseInt(pay1[2].substring(0,10))) > (timestamp + delta))
            {
                System.err.println("Invalid timestamp.\n\tCurrent: " + timestamp + "\n\tReceived: " + (pay1[2].substring(0,10)) + "\n\tDelta: " + delta);
                System.exit(0);
            }

            if(!pay1[0].substring(0,16).equals("UserB Nithishma!"))
            {
                System.err.println("Invalid B.\n\tExpected: UserB Nithishma!\n\tReceived: " + (pay1[0]));
                System.exit(0);
            }


            ObjectOutputStream oosb = new ObjectOutputStream(clientb_writer);  
            oosb.writeObject(payB);
            System.out.println("Sent.");

            String key=pay1[1].substring(0,16);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Receiving message...");
            ObjectInputStream oisb = new ObjectInputStream(clientb_reader);  //receiving B's reply
            byte[] msg= (byte[])oisb.readObject();
            String m = Camellia.dec(msg,key); 

            
            String mess=f(m);
            byte[] mess1=Camellia.enc(mess,key);
            
            
            System.out.println("Sending function of Message to "+pay1[0].substring(0,16)+"...");
            oosb.writeObject(mess1);
            System.out.println("Sent.");

            msg= (byte[])oisb.readObject();
            String reply = Camellia.dec(msg,key);

            System.out.println(reply);
            System.out.println("Enter 1 to start a conversation or press 0 to exit:    ");
            Scanner sc=new Scanner(System.in);
            int choice=sc.nextInt();
            switch(choice){
                case 1:System.out.println("You can now begin a conversation with UserB.\nEnter 'exit' to leave.");
                    while((!reply.substring(0,4).equals("exit"))&&(!m.equals("exit"))){
                        System.out.print("UserA : ");
                        m = br.readLine();if(m.equals("exit"))break;
                        mess1=Camellia.enc(m,key);
                        oosb.writeObject(mess1);
                        byte[] rep = (byte[])oisb.readObject();
                        reply=Camellia.dec(rep,key);
                        if(reply.equals("exit")){System.out.print("Connection closed.");
                clientb.close();System.exit(0);break;}
                        System.out.println("UserB : "+reply);
            }
                
                
                case 0:
                System.out.println("Conversation closed");clientb.close();System.exit(0);break;
            }
         clientb.close();System.exit(0);
        }

        catch (Exception e)
        {
            
            System.exit(0);
        }
    }
}