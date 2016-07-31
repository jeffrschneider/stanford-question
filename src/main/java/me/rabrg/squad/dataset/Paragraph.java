package me.rabrg.squad.dataset;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import me.rabrg.util.TypeDependencyUtil;
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
        final TypeDependencyUtil.TypeDependencyData questionData = TypeDependencyUtil.getData(sentence.text());

        Sentence first = null;
        // 1. if Q-Verb = S-verb, and Q-Dobj = S-Dobj; pick that sentence
        for (final Sentence contextSentence : getContextSentences()) {
            final TypeDependencyUtil.TypeDependencyData contextData = TypeDependencyUtil.getData(contextSentence.text());
            if (questionData.getRelation() != null && questionData.getObject() != null && questionData.getRelation().equalsIgnoreCase(contextData.getRelation()) && questionData.getObject().equalsIgnoreCase(contextData.getObject())) {
                first = contextSentence;
                break;
            }
        }

        // 2. if Q-Verb= S-verb, and Q-Dobj = S-Subj; pick that sentence
        if (first != null) {
            for (final Sentence contextSentence : getContextSentences()) {
                final TypeDependencyUtil.TypeDependencyData contextData = TypeDependencyUtil.getData(contextSentence.text());
                if (questionData.getRelation() != null && questionData.getObject() != null && questionData.getRelation().equalsIgnoreCase(contextData.getRelation()) && questionData.getObject().equalsIgnoreCase(contextData.getSubject())) {
                    first = contextSentence;
                    break;
                }
            }
        }

        // 3. if Q-Verb = S-Verb; pick that sentence
        if (first != null) {
            for (final Sentence contextSentence : getContextSentences()) {
                final TypeDependencyUtil.TypeDependencyData contextData = TypeDependencyUtil.getData(contextSentence.text());
                if (questionData.getRelation() != null && questionData.getRelation().equalsIgnoreCase(contextData.getRelation())) {
                    first = contextSentence;
                    break;
                }
            }
        }
        // TODO: 4 through 7

        // Hard detection failed, revert to term frequency
        Collections.sort(orderedContextSentences, new Comparator<Sentence>() {
            @Override
            public int compare(Sentence o1, Sentence o2) {
                double o1Frequency = WordUtil.getLemmaFrequency(o1, sentence) * WordUtil.getTripleMatchMultiplier(o1, sentence);
                double o2Frequency = WordUtil.getLemmaFrequency(o2, sentence) * WordUtil.getTripleMatchMultiplier(o2, sentence);
                if (o1Frequency > o2Frequency)
                    return -1;
                else if (o1Frequency < o2Frequency)
                    return 1;
                return 0;
            }
        });
        if (first != null) {
            orderedContextSentences.remove(first);
            orderedContextSentences.add(0, first);
        }
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
