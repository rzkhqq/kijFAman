package kij_chat_client;

import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

/** original ->http://www.dreamincode.net/forums/topic/262304-simple-client-and-server-chat-program/
 * 
 * @author santen-suru
 */

public class Client implements Runnable {

    private Socket socket;//MAKE SOCKET INSTANCE VARIABLE
    private static PublicKey public_key;
    private static PrivateKey private_key;
    private static String public_key_string;
    private static String private_key_string;

    // use arraylist -> arraylist dapat diparsing as reference
    volatile ArrayList<String> log = new ArrayList<>();

    public Client(Socket s)
    {
        socket = s;//INSTANTIATE THE INSTANCE VARIABLE
        log.add(String.valueOf("false"));
        
        Client.setKey();
    }

    private void sendKeyToServer(PrintWriter pw) throws NoSuchAlgorithmException
    {
        String message = "HELLO " + public_key_string;
        String hash = Hashing.hashString(message);

        pw.println(message + " " + hash);
        pw.flush();
    }

    @Override
    public void run()//INHERIT THE RUN METHOD FROM THE Runnable INTERFACE
    {
        try
        {
            Scanner chat = new Scanner(System.in);//GET THE INPUT FROM THE CMD
            Scanner in = new Scanner(socket.getInputStream());//GET THE CLIENTS INPUT STREAM (USED TO READ DATA SENT FROM THE SERVER)
            PrintWriter out = new PrintWriter(socket.getOutputStream());//GET THE CLIENTS OUTPUT STREAM (USED TO SEND DATA TO THE SERVER)

            Read reader = new Read(in, log);

            Thread tr = new Thread(reader);
            tr.start();

            Write writer = new Write(chat, out, log);

            sendKeyToServer(out);

            Thread tw = new Thread(writer);
            tw.start();

            while (tr.isAlive() == true) {
                if (tr.isAlive() == false && tw.isAlive() == false) {
                    socket.close();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();//MOST LIKELY WONT BE AN ERROR, GOOD PRACTICE TO CATCH THOUGH
        } 
    }

    public static void setKey() {
        KeyPair key_pair = RSAEncryption.generateKey();
        
        Client.public_key = key_pair.getPublic();
        Client.private_key = key_pair.getPrivate();
        
        Client.public_key_string = Base64.getEncoder().
                encodeToString(Client.public_key.getEncoded());
        Client.private_key_string = Base64.getEncoder().
                encodeToString(Client.private_key.getEncoded());
    }


}

