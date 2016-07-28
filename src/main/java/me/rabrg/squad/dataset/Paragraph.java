package me.rabrg.squad.dataset;

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

    private List<Sentence> contextSentences;

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
        }
        return contextSentences;
    }

    public List<Sentence> getOrderedRelevancyContextSentences(final Sentence sentence) {
        final List<Sentence> orderedContextSentences = new ArrayList<>(getContextSentences());
        Collections.sort(orderedContextSentences, new Comparator<Sentence>() {
            @Override
            public int compare(Sentence o1, Sentence o2) {
                double o1Frequency = WordUtil.getLemmaFrequency(o1, sentence);
//                double o1Frequency = WordUtil.getLemmaFrequency(o1, sentence) * WordUtil.getTripleMatchMultiplier(o1, sentence);
                double o2Frequency = WordUtil.getLemmaFrequency(o2, sentence);
//                double o2Frequency = WordUtil.getLemmaFrequency(o2, sentence) * WordUtil.getTripleMatchMultiplier(o2, sentence);
                if (o1Frequency > o2Frequency)
                    return -1;
                else if (o1Frequency < o2Frequency)
                    return 1;
                return 0;
            }
        });
        return orderedContextSentences;
    }

    @Override
    public String toString() {
        return "Paragraph{" +
                "context='" + context + '\'' +
                ", qas=" + qas +
                '}';
    }
}
