package net.c_kogyo.returnvisitorv5.cloudsync;

import android.content.Context;
import android.util.Log;

import net.c_kogyo.returnvisitorv5.util.KeyStoreUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


/**
 * Created by SeijiShii on 2017/05/20.
 */

public abstract class RVWebSocketClient extends WebSocketClient{

    private final String TAG = "RVWebSocketClient";

    public RVWebSocketClient(URI uri, Context context) {
        super(uri);

        try {
            KeyStore keyStore = KeyStoreUtil.getEmptyKeyStore();
            KeyStoreUtil.loadX509Certificate(keyStore, context.getAssets().open("server.crt"));

//            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
//            keyManagerFactory.init(keyStore, new char[0]);

//            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
//            trustManagerFactory.init(keyStore);

            TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };

            SSLContext sslContext = null;
            sslContext = SSLContext.getInstance( "TLS" );
            sslContext.init( null, new TrustManager[]{trustManager}, new SecureRandom());
            // sslContext.init( null, null, null ); // will use java's default key and trust store which is sufficient unless you deal with self-signed certificates

            SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory) SSLSocketFactory.getDefault();
            this.setSocket(factory.createSocket());

        } catch (IOException
                | KeyStoreException
                | NoSuchAlgorithmException
                | CertificateException
//                | UnrecoverableKeyException
                | KeyManagementException
                e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {

    }

    @Override
    public void onClose(int i, String s, boolean b) {

    }

    @Override
    public void onError(Exception e) {

    }


}


