package trifork.nlp.nlpAPI;

import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPI;
import com.google.api.services.language.v1beta1.model.*;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;

/**
 * @author Torec Luik.
 */
@RestController
public class NlpController {

    private static final Logger log = LoggerFactory.getLogger(NlpController.class);

    @Autowired
    private CloudNaturalLanguageAPI languageApi;

    @Autowired
    private Client client;

    @RequestMapping("/analyzeEntities")
    public List<Entity> analyzeEntities(@RequestParam String text) throws IOException {
        AnalyzeEntitiesRequest request =
                new AnalyzeEntitiesRequest()
                        .setDocument(new Document().setContent(text).setType("PLAIN_TEXT"))
                        .setEncodingType("UTF16");
        CloudNaturalLanguageAPI.Documents.AnalyzeEntities analyze =
                languageApi.documents().analyzeEntities(request);

        log.info("Analyzing entities for: [" + text + "]");
        AnalyzeEntitiesResponse response = analyze.execute();
        log.info(response.toPrettyString());
        return response.getEntities();
    }

    @RequestMapping("/analyzeSentiment")
    public Sentiment analyzeSentiment(@RequestParam String text) throws IOException {
        AnalyzeSentimentRequest request =
                new AnalyzeSentimentRequest().setDocument(new Document().setContent(text).setType("PLAIN_TEXT"));
        CloudNaturalLanguageAPI.Documents.AnalyzeSentiment analyze =
                languageApi.documents().analyzeSentiment(request);

        log.info("Analyzing sentiment for: [" + text + "]");
        AnalyzeSentimentResponse response = analyze.execute();
        log.info(response.toPrettyString());
        return response.getDocumentSentiment();
    }

    @RequestMapping("/analyzeSyntax")
    public List<Token> analyzeSyntax(@RequestParam String text) throws IOException {
        AnnotateTextRequest request = new AnnotateTextRequest()
                .setDocument(new Document().setContent(text).setType("PLAIN_TEXT"))
                .setEncodingType("UTF8")
                .setFeatures(new Features().setExtractSyntax(true));
        CloudNaturalLanguageAPI.Documents.AnnotateText analyze = languageApi.documents().annotateText(request);

        log.info("Analyzing syntax for: [" + text + "]");
        final AnnotateTextResponse response = analyze.execute();
        log.info(response.toPrettyString());
        return response.getTokens();
    }

    @RequestMapping("/analyze")
    public String analyze(@RequestParam String text,
                          @RequestParam(required = false, defaultValue = "true") boolean syntax,
                          @RequestParam(required = false, defaultValue = "true") boolean sentiment,
                          @RequestParam(required = false, defaultValue = "true") boolean entities) throws IOException {
        if (!syntax && !sentiment && !entities) {
            return "Nothing to do here...";
        } else {
            AnnotateTextRequest request = new AnnotateTextRequest()
                    .setDocument(new Document().setContent(text).setType("PLAIN_TEXT"))
                    .setEncodingType("UTF8")
                    .setFeatures(new Features().setExtractSyntax(syntax).setExtractDocumentSentiment(sentiment).setExtractEntities(entities));
            CloudNaturalLanguageAPI.Documents.AnnotateText analyze = languageApi.documents().annotateText(request);

            log.info("Analyzing syntax [" + syntax + "], sentiment [" + sentiment + "] and/or entities [" + entities + "] for: [" + text + "]");
            final AnnotateTextResponse response = analyze.execute();
            final String responseString = response.toPrettyString();
            log.info(responseString);

            final IndexRequestBuilder indexRequestBuilder = client.prepareIndex("nlp", "annotate").setSource(response);
            final IndexResponse indexResponse = indexRequestBuilder.get();
            log.info("Indexed into ES: " + indexResponse.getId());

            return responseString;
        }
    }
}
