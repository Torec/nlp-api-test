package nl.torec.nlp;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPI;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPIRequestInitializer;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.TranslateRequestInitializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author Torec Luik.
 */
@Configuration
public class GoogleCloudConfig {

    @Value("${google.cloud.api.key}")
    private String apiKey;

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

    @Bean
    public Translate translate() {
        try {
            final TranslateRequestInitializer tInit = new TranslateRequestInitializer(apiKey);
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            return new Translate.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    jsonFactory,
                    null)
                    .setTranslateRequestInitializer(tInit)
                    .setApplicationName("nlp-api-test").build();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
