package trifork.nlp;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPI;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPIRequestInitializer;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

@SpringBootApplication
public class Application {

    @Value("${google.cloud.api.key}")
    String apiKey;

    @Resource
    private Environment environment;

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);

        System.out.println("Let's inspect the beans provided by Spring Boot:");

        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }

    @Bean
    public CloudNaturalLanguageAPI cloudNaturalLanguageAPI() {
        try {
            final CloudNaturalLanguageAPIRequestInitializer rInit = new CloudNaturalLanguageAPIRequestInitializer(apiKey);
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            return new CloudNaturalLanguageAPI.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    jsonFactory,
                    null)
                    .setCloudNaturalLanguageAPIRequestInitializer(rInit)
                    .setApplicationName("nlp-api-test").build();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }
//
//    @Bean
//    public Node node() {
//        return new NodeBuilder()
//                .build().start();
//    }

    @Bean
    public Client client() throws UnknownHostException {
//        Settings settings = Settings.settingsBuilder()
//                .put("client.transport.sniff", true).build();
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "elasticsearch").build();
        return TransportClient.builder()
                .settings(settings)
                .build()
                .addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress("127.0.0.1", 9300)));
    }
}