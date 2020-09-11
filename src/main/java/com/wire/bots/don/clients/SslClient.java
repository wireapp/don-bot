package com.wire.bots.don.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.don.model.NewBotResponseModel;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.tools.Logger;
import com.wire.bots.sdk.tools.Util;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SslClient implements Closeable {
    private final Client client;
    private final PublicKey pubKey;
    private final MessageDigest md;

    public SslClient(String pubkey) throws Exception {
        this.md = MessageDigest.getInstance("SHA-1");
        this.pubKey = createPublicKey(pubkey);
        this.client = ClientBuilder
                .newBuilder()
                .sslContext(getSslContext())
                .hostnameVerifier((s1, s2) -> true)
                .build();
    }

    private PublicKey createPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pubKeyPEM = publicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\n", "");
        Logger.debug("Pubkey: %s", pubKeyPEM);
        byte[] data = Base64.getDecoder().decode(pubKeyPEM);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        return fact.generatePublic(spec);
    }

    private SSLContext getSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(null, new TrustManager[]
                {
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {

                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                                for (X509Certificate cert : chain) {
                                    String certPrint = getThumbPrint(cert.getPublicKey().getEncoded());
                                    String pubKeyPrint = getThumbPrint(pubKey.getEncoded());
                                    Logger.info("cert: %s, expect: %s", certPrint, pubKeyPrint);
                                    if (certPrint.equals(pubKeyPrint))
                                        return;
                                }
                                throw new CertificateException("Invalid RSA Public key");
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }

                        }
                }, new java.security.SecureRandom());
        return sslcontext;
    }

    private String getThumbPrint(byte[] der) {
        md.update(der);
        byte[] digest = md.digest();
        return hexify(digest);
    }

    private static String hexify(byte[] bytes) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            buf.append(hexDigits[(aByte & 0xf0) >> 4]);
            buf.append(hexDigits[aByte & 0x0f]);
        }
        return buf.toString();
    }

    public String testService(Service service) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] resource = Util.getResource("newBot.json");
        NewBot bot = objectMapper.readValue(resource, NewBot.class);

        Response response = client.target(service.url).
                path("bots").
                request(MediaType.APPLICATION_JSON).
                header("Authorization", "Bearer " + service.auth_tokens[0]).
                post(Entity.entity(bot, MediaType.APPLICATION_JSON));

        if (response.getStatus() >= 300) {
            String msg = response.readEntity(String.class);
            return String.format("Error testing your bot: Status: %d, Server error: %s",
                    response.getStatus(), msg);
        }

        if (response.getMediaType() == null || !response.getMediaType().equals(MediaType.APPLICATION_JSON_TYPE))
            return String.format("Wrong media type: %s", response.getMediaType());

        NewBotResponseModel newBot = response.readEntity(NewBotResponseModel.class);
        if (newBot.preKeys.isEmpty())
            return "No prekeys";

        if (newBot.lastPreKey == null)
            return "Last prekey is null :-s";

        return "All good!";
    }

    @Override
    public void close() {
        client.close();
    }
}
