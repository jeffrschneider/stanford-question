package me.rabrg.util;

import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import me.rabrg.squad.dataset.Paragraph;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CorefUtil {

    private static final boolean DEBUG = true;

    private static final List<String> PRONOUNS = Arrays.asList("hers", "herself", "him", "himself", "hisself", "it",
            "itself", "me", "myself", "one", "oneself", "ours", "ourselves", "ownself", "self", "she", "thee",
            "theirs", "them", "themselves", "they", "thou", "thy", "us", "her", "his", "mine", "my", "our", "ours",
            "their", "thy", "your"); // TODO: use POS tagger?

    private static final Properties props;
    private static final StanfordCoreNLP pipeline;

    static {
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        pipeline = new StanfordCoreNLP(props);
    }

    public static void replaceContextPronouns(final Paragraph paragraph) {
        final Annotation document = new Annotation(paragraph.getContext());
        pipeline.annotate(document);

        final Map<Integer, CorefChain> graph = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);
        for (Map.Entry<Integer, CorefChain> entry : graph.entrySet()) {
            CorefChain chain = entry.getValue();
            System.out.println(chain);
        }
    }
}
