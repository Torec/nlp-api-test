package nl.torec.nlp;

import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPI;
import com.google.api.services.language.v1beta1.model.*;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.model.TranslationsListResponse;
import com.google.api.services.translate.model.TranslationsResource;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Torec Luik.
 */
@RestController
public class NlpController {

    private static final Logger log = LoggerFactory.getLogger(NlpController.class);

    @Resource
    private CloudNaturalLanguageAPI languageApi;

    @Resource
    private Translate translateApi;

    @Resource
    private Client client;

    @RequestMapping("/analyzeEntities")
    public List<Entity> analyzeEntities(@RequestParam String text) throws IOException {
        final Features features = new Features()
                .setExtractEntities(true);
        final String msg = "Analyzing entities for: [" + text + "]";
        AnnotateTextResponse response = callAnnotateText(text, features, msg);
        log.info(response.toPrettyString());
        return response.getEntities();
    }

    @RequestMapping("/analyzeSentiment")
    public Sentiment analyzeSentiment(@RequestParam String text) throws IOException {
        final Features features = new Features()
                .setExtractDocumentSentiment(true);
        final String msg = "Analyzing sentiment for: [" + text + "]";
        AnnotateTextResponse response = callAnnotateText(text, features, msg);
        log.info(response.toPrettyString());
        return response.getDocumentSentiment();
    }

    @RequestMapping("/analyzeSyntax")
    public List<Token> analyzeSyntax(@RequestParam String text) throws IOException {
        final Features features = new Features().setExtractSyntax(true);
        final String msg = "Analyzing syntax for: [" + text + "]";
        final AnnotateTextResponse response = callAnnotateText(text, features, msg);
        log.info(response.toPrettyString());
        return response.getTokens();
    }

    @RequestMapping("/analyze")
    public String analyze(@RequestParam String text,
                          @RequestParam(required = false, defaultValue = "true") boolean syntax,
                          @RequestParam(required = false, defaultValue = "true") boolean sentiment,
                          @RequestParam(required = false, defaultValue = "true") boolean entities,
                          @RequestParam(required = false, defaultValue = "false") boolean translate) throws IOException {
        if (!syntax && !sentiment && !entities) {
            return "Nothing to do here...";
        } else {
            // Call Google Cloud NLP service
            if (translate) {
                text = translateToEnglish(text);
            }
            final Features features = new Features()
                    .setExtractSyntax(syntax)
                    .setExtractDocumentSentiment(sentiment)
                    .setExtractEntities(entities);
            final String msg = "Analyzing syntax [" + syntax + "], sentiment [" + sentiment + "] and/or entities [" + entities
                    + "] for: [" + text + "]";
            final AnnotateTextResponse response = callAnnotateText(text, features, msg);

            // Store response in ES
            storeInES(response);

            // Show response to user
            final String responseString = response.toPrettyString();
            log.info(responseString);
            return responseString;
        }
    }

    private void storeInES(AnnotateTextResponse response) {
        final IndexRequestBuilder indexRequestBuilder = client.prepareIndex("nlp", "annotate").setSource(response);
        final IndexResponse indexResponse = indexRequestBuilder.get();
        log.info("Indexed into ES: " + indexResponse.getId());
    }

    private AnnotateTextResponse callAnnotateText(String text, Features features, String msg)
            throws IOException {
        AnnotateTextRequest request = new AnnotateTextRequest()
                .setDocument(new Document().setContent(text).setType("PLAIN_TEXT"))
                .setEncodingType("UTF8")
                .setFeatures(features);
        CloudNaturalLanguageAPI.Documents.AnnotateText analyze = languageApi.documents().annotateText(request);
        log.info(msg);
        return analyze.execute();
    }

    private String translateToEnglish(String text) throws IOException {
        final TranslationsListResponse text_en = translateApi.translations().list(Collections.singletonList(text), "en").execute();
        final TranslationsResource translationsResource = text_en.getTranslations().get(0);
        return translationsResource.getTranslatedText();
    }
}
