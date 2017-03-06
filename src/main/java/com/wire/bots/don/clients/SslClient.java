package com.wire.bots.don.clients;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.Logger;
import org.glassfish.jersey.client.ClientConfig;

import javax.net.ssl.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SslClient {
    private static Client client;
    private static TrustManager[] certs = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }
            }
    };

    static {
        try {
            SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(null, certs, new SecureRandom());

            ClientConfig config = new ClientConfig(JacksonJsonProvider.class);
            client = ClientBuilder.newBuilder()
                    .withConfig(config)
                    .hostnameVerifier(new TrustAllHostNameVerifier())
                    .sslContext(ctx)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class TrustAllHostNameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    public static boolean verifyService(Service service) {
        try {
            Response response = client.target(service.url).
                    path("/bots/status").
                    request().
                    get();
            return response.getStatus() == 200;
        } catch (Exception e) {
            Logger.info(String.format("Failed to query the service: '%s'. id: %s provider: %s url: %s. Error: %s",
                    service.name,
                    service.id,
                    service.provider,
                    service.url,
                    e.getLocalizedMessage()));
            return false;
        }
    }
}
