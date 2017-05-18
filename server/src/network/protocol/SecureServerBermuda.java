package network.protocol;

import database.DatabaseManager;
import network.sockets.SecureServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class SecureServerBermuda extends SecureServer {

    private static String backup_file = "." + File.separator + "db.backup";
    private static int interval = 20;
    public SecureServerBermuda(int port) throws Exception {
        super(port);
    }

    public void start_sending_backups() {

        System.out.println("Starting to send backup file to backup server");

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                System.out.println("Sending backup file!");
                //send_database_backup();
            }

        }, 0, interval * 1000);
    }

    public void send_database_backup(){

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        Socket socket = null;

        while (true) {
            System.out.println("Waiting...");
            try {
                socket = serverSocket.accept();
                System.out.println("Accepted connection : " + socket);
                // send file
                File myFile = new File(backup_file);

                byte[] mybytearray = new byte[(int) myFile.length()];
                fis = new FileInputStream(myFile);
                bis = new BufferedInputStream(fis);
                bis.read(mybytearray, 0, mybytearray.length);
                os = socket.getOutputStream();
                System.out.println("Sending " + backup_file + "(" + mybytearray.length + " bytes)");
                os.write(mybytearray, 0, mybytearray.length);
                os.flush();
                bis.close();
                os.close();
                socket.close();
                System.out.println("Done.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}