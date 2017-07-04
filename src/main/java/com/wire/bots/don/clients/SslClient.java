package com.wire.bots.don.clients;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wire.bots.don.model.NewBot;
import com.wire.bots.don.model.NewBotResponseModel;
import com.wire.bots.don.model.Service;
import com.wire.bots.sdk.server.model.Conversation;
import com.wire.bots.sdk.server.model.Member;
import com.wire.bots.sdk.server.model.User;
import org.glassfish.jersey.client.ClientConfig;
import sun.misc.BASE64Decoder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

public class SslClient implements Closeable {
    private Client client;
    private PublicKey pubKey;

    public SslClient(String key) throws Exception {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, getCerts(), new SecureRandom());

        ClientConfig config = new ClientConfig(JacksonJsonProvider.class);
        client = ClientBuilder.newBuilder()
                .withConfig(config)
                .hostnameVerifier((s, sslSession) -> true)
                .sslContext(ctx)
                .build();

        String pubKeyPEM = key.replace(
                "-----BEGIN PUBLIC KEY-----\n", "")
                .replace("-----END PUBLIC KEY-----", "");
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] data = decoder.decodeBuffer(pubKeyPEM);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        pubKey = fact.generatePublic(spec);
    }

    private TrustManager[] getCerts() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {

                        for (X509Certificate cert : chain) {
                            if (cert.getPublicKey().equals(pubKey))
                                return;
                        }

                        throw new CertificateException("Invalid RSA Public key");
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {

                    }
                }
        };
    }

    public String testService(Service service) {
        NewBot bot = new NewBot();
        bot.client = "f0f4028e753cb7cc";
        bot.conversation = new Conversation();
        bot.conversation.id = "f0dc6989-f862-4c53-a257-06044cd23d41";
        bot.conversation.members = new ArrayList<>();
        bot.conversation.members.add(new Member());
        bot.conversation.members.get(0).id = "fed64a84-4df0-420a-ae6f-a1ff7ff41ea9";
        bot.conversation.name = "Test";
        bot.id = "4d7e590d-1dd4-43c6-a88b-ccc49ab00efd";
        bot.locale = "en";
        bot.token = "GPq5ToXOz34QbKw4Sfs4n1sv9eOV2Y6aodZiWIE8zr3joSyvg5NjWe82ajNOMz3wIPCZFU7eBly4cT8-F3yZAg==" +
                ".v=1.k=1.d=-1.t=b.l=.p=d39b462f-7e60-4d88-82e1-44d632f94901.b=4d7e590d-1dd4-43c6-a88b-ccc49ab00efd." +
                "c=f0dc6989-f862-4c53-a257-06044cd23d41";
        bot.origin = new User();
        bot.origin.handle = "dejan";
        bot.origin.id = "fed64a84-4df0-420a-ae6f-a1ff7ff41ea9";
        bot.origin.name = "Dejan";

        Response response = client.target(service.url).
                path("bots").
                request(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
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
