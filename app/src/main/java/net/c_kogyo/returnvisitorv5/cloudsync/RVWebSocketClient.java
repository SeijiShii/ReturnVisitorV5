package net.c_kogyo.returnvisitorv5.cloudsync;

import android.content.Context;
import android.util.Log;

import net.c_kogyo.returnvisitorv5.util.KeyStoreUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;


/**
 * Created by SeijiShii on 2017/05/20.
 */

public class RVWebSocketClient extends WebSocketClient{

    private final String TAG = "RVWebSocketClient";
    private RVWebSocketClientCallback mCallback;

    public RVWebSocketClient(URI uri, final Context context, RVWebSocketClientCallback callback) {
        super(uri);

        mCallback = callback;

        try {

            TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    try {
                        CertificateFactory factory = CertificateFactory.getInstance("X509");
                        X509Certificate serverCrt = ( X509Certificate) factory.generateCertificate(context.getAssets().open("server.crt"));
                        if (!chain[0].equals(serverCrt)) {
                            new CertificateException();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };

            SSLContext sslContext = SSLContext.getInstance( "TLS" );
            sslContext.init( null, new TrustManager[]{trustManager}, new SecureRandom());
            // sslContext.init( null, null, null ); // will use java's default key and trust store which is sufficient unless you deal with self-signed certificates

            SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory) SSLSocketFactory.getDefault();
            this.setSocket(factory.createSocket());

        } catch (IOException
                | NoSuchAlgorithmException
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
        Log.d(TAG, s);
    }

    @Override
    public void onError(Exception e) {
        Log.d(TAG, e.getMessage());
    }

    @Override
    public void onMessage(String s) {
//        Log.d(TAG, "onMessage called!");

        if (mCallback != null) {
            mCallback.onWebSocketMessage(s);
        }
    }


    public interface RVWebSocketClientCallback {
        void onWebSocketMessage(String s);
    }
}


