package me.rabrg.squad.dataset;

import edu.cmu.lti.ws4j.WS4J;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import me.rabrg.util.WordUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class Paragraph {

    private String context;
    private List<QuestionAnswerService> qas;

    private transient List<Sentence> contextSentences;

    public String getContext() {
        return context;
    }

    public List<QuestionAnswerService> getQas() {
        return qas;
    }

    public List<Sentence> getContextSentences() {
        if (contextSentences == null) {
            final Document document = new Document(context);
            contextSentences = document.sentences();
//            CorefUtil.replaceContextPronouns(this);
//            context = "";
//            for (final Sentence sentence :contextSentences) {
//                context += sentence.text() + " ";
//            }
//            context = context.trim();
        }
        return contextSentences;
    }

    public List<Sentence> getOrderedRelevancyContextSentences(final Sentence sentence) {
        final List<Sentence> orderedContextSentences = new ArrayList<>(getContextSentences());
        Collections.sort(orderedContextSentences, new Comparator<Sentence>() {
            @Override
            public int compare(Sentence o1, Sentence o2) {
//                double o1Frequency = getSimilarity(o1, sentence);
//                double o1Frequency = WordUtil.getLemmaFrequency(o1, sentence);
                double o1Frequency = WordUtil.getLemmaFrequency(o1, sentence) * WordUtil.getTripleMatchMultiplier(o1, sentence);
//                double o2Frequency = getSimilarity(o2, sentence);
//                double o2Frequency = WordUtil.getLemmaFrequency(o2, sentence);
                double o2Frequency = WordUtil.getLemmaFrequency(o2, sentence) * WordUtil.getTripleMatchMultiplier(o2, sentence);
                if (o1Frequency > o2Frequency)
                    return -1;
                else if (o1Frequency < o2Frequency)
                    return 1;
                return 0;
            }
        });
        return orderedContextSentences;
    }

    private static double getSimilarity(final Sentence s, final Sentence s2) {
        double average = 0;
        for (final String word : s.words())
            for (final String word2 : s2.words())
                average += WS4J.runLIN(word, word2);
        return average / (s.words().size() * s2.words().size());
    }

    @Override
    public String toString() {
        return "Paragraph{" +
                "context='" + context + '\'' +
                ", qas=" + qas +
                '}';
    }
}
