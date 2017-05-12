package network;


import database.DatabaseConnection;

import javax.net.ssl.*;
import javax.security.cert.CertificateException;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class Utils {

    public static SSLContext sslContext;
    public static DatabaseConnection db;

    public static void initDB(){
        db = new DatabaseConnection();
    }


    static {

        try {

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream("src/keys/truststore"), "123456".toCharArray());

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

        }catch (Exception e){
            e.printStackTrace();
        }
    }




}
