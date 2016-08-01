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

    // Verb detection debugging stats
    public static int method;
    public static int method1 = 0;
    public static int method2 = 0;
    public static int method3 = 0;
    public static int alreadyDetected1 = 0;
    public static int alreadyDetected2 = 0;
    public static int alreadyDetected3 = 0;

    public List<Sentence> getOrderedRelevancyContextSentences(final Sentence sentence) {
        final List<Sentence> orderedContextSentences = new ArrayList<>(getContextSentences());

        // Term frequency
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

        // Verb rules
        final Sentence first = getFirstRelevancy(sentence);
        if (first != null) {
            if (orderedContextSentences.indexOf(first) == 0) {
                if (method == 1)
                    alreadyDetected1++;
                else if (method == 2)
                    alreadyDetected2++;
                else if (method == 3)
                    alreadyDetected3++;
            }
            orderedContextSentences.remove(first);
            orderedContextSentences.add(0, first);
        }
        return orderedContextSentences;
    }

    private Sentence getFirstRelevancy(final Sentence sentence) {
        final TypeDependencyUtil.TypeDependencyData questionData = TypeDependencyUtil.getData(sentence.text());
        final List<String> questionVerbs = getVerbs(sentence);

        method = -1;
        Sentence first = null;
        // 1. if Q-Verb = S-verb, and Q-Dobj = S-Dobj; pick that sentence
        for (final Sentence contextSentence : getContextSentences()) {
            final List<String> contextVerbs = getVerbs(contextSentence);
            final TypeDependencyUtil.TypeDependencyData contextData = TypeDependencyUtil.getData(contextSentence.text());
            if (!Collections.disjoint(questionVerbs, contextVerbs) && questionData.getObject() != null && questionData.getObject().equalsIgnoreCase(contextData.getObject())) {
                first = contextSentence;
                method1++;
                method = 1;
                break;
            }
        }

        // 2. if Q-Verb= S-verb, and Q-Dobj = S-Subj; pick that sentence
        if (first == null) {
            for (final Sentence contextSentence : getContextSentences()) {
                final List<String> contextVerbs = getVerbs(contextSentence);
                final TypeDependencyUtil.TypeDependencyData contextData = TypeDependencyUtil.getData(contextSentence.text());
                if (!Collections.disjoint(questionVerbs, contextVerbs) && questionData.getObject() != null && questionData.getObject().equalsIgnoreCase(contextData.getSubject())) {
                    first = contextSentence;
                    method2++;
                    method = 2;
                    break;
                }
            }
        }

        // 3. if Q-Verb = S-Verb; pick that sentence
        if (first == null) {
            for (final Sentence contextSentence : getContextSentences()) {
                final List<String> contextVerbs = getVerbs(contextSentence);
                if (!Collections.disjoint(questionVerbs, contextVerbs)) {
                    first = contextSentence;
                    method3++;
                    method = 3;
                    break;
                }
            }
        }
        // TODO: 4 through 7
        return first;
    }

    private static List<String> getVerbs(final Sentence sentence) {
        final List<String> verbs = new ArrayList<>();
        for (int i = 0; i < sentence.words().size(); i++)
            if (sentence.posTag(i).startsWith("V"))
                verbs.add(new Sentence(sentence.word(i)).lemmas().get(0));
        return verbs;
    }

    @Override
    public String toString() {
        return "Paragraph{" +
                "context='" + context + '\'' +
                ", qas=" + qas +
                '}';
    }
}
