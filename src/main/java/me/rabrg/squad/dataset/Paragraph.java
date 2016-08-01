package me.rabrg.squad.dataset;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.ws4j.WS4J;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import me.rabrg.util.TypeDependencyUtil;
import me.rabrg.util.WordUtil;

import java.util.*;

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
    public static int method4 = 0;
    public static int method5 = 0;

    public static TypeDependencyUtil.TypeDependencyData questionData;
    public static TypeDependencyUtil.TypeDependencyData contextData;

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
//        final Sentence first = getFirstRelevancy(sentence);
//        if (first != null) {
//            orderedContextSentences.remove(first);
//            orderedContextSentences.add(0, first);
//        }
        return orderedContextSentences;
    }

    @Deprecated
    private Sentence getFirstRelevancy(final Sentence sentence) {
        questionData = null;
        final TypeDependencyUtil.TypeDependencyData questionData = TypeDependencyUtil.getData(sentence.text());
        final Set<String> questionSynonyms = getVerbSynonyms(questionData);
        final Set<String> questionAntonyms = getVerbAntonyms(questionData);

        Paragraph.questionData = questionData;
        Paragraph.contextData = null;
        method = -1;
        Sentence first = null;
        // 1. if Q-Verb = S-verb, and Q-Dobj = S-Dobj; pick that sentence
        for (final Sentence contextSentence : getContextSentences()) {
            final TypeDependencyUtil.TypeDependencyData contextData = TypeDependencyUtil.getData(contextSentence.text());
            if (contextSentence.text().startsWith("Another factor in the early 1990s")) {
                System.out.println("DEBUG OUTER: questionData=" + questionData.toString());
                System.out.println("DEBUG OUTER: contextData=" + contextData.toString());
            }
            if (questionData.getRelation() != null && questionData.getObject() != null && questionData.getRelation().equalsIgnoreCase(contextData.getRelation()) && questionData.getObject().equalsIgnoreCase(contextData.getObject())) {
                first = contextSentence;
                Paragraph.contextData = contextData;
                if (contextSentence.text().toLowerCase().startsWith("Another factor in the early 1990s")) {
                    System.out.println("DEBUG INNER: questionData=" + questionData.toString());
                    System.out.println("DEBUG INNER: contextData=" + contextData.toString());
                }
                method1++;
                method = 1;
                return contextSentence;
            }
        }

        // 2. if Q-Verb= S-verb, and Q-Dobj = S-Subj; pick that sentence
        if (first == null) {
            for (final Sentence contextSentence : getContextSentences()) {
                final TypeDependencyUtil.TypeDependencyData contextData = TypeDependencyUtil.getData(contextSentence.text());
                if (questionData.getRelation() != null && questionData.getObject() != null && questionData.getRelation().equalsIgnoreCase(contextData.getRelation()) && questionData.getObject().equalsIgnoreCase(contextData.getSubject())) {
                    first = contextSentence;
                    method2++;
                    method = 2;
                    break;
                }
            }
        }

        // 3. if Q-Verb = S-Verb; pick that sentence
        if (first == null) {
            final List<String> questionVerbs = getVerbs(sentence);
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

//        // 4. If Q-Verb = synonymOf(S-Verb), pick that sentence
//        if (first == null) {
//            for (final Sentence contextSentence : getContextSentences()) {
//                final TypeDependencyUtil.TypeDependencyData contextData = TypeDependencyUtil.getData(contextSentence.text());
//                final Set<String> contextSynonyms = getVerbSynonyms(contextData);
//                if (questionSynonyms != null && contextSynonyms != null && !Collections.disjoint(questionSynonyms, contextSynonyms)) {
//                    first = contextSentence;
//                    method4++;
//                    method = 4;
//                    break;
//                }
//            }
//        }
//
//        // 5. If Q-Verb = antonymOf(S-Verb), pick that sentence
//        if (first == null) {
//            for (final Sentence contextSentence : getContextSentences()) {
//                final TypeDependencyUtil.TypeDependencyData contextData = TypeDependencyUtil.getData(contextSentence.text());
//                final Set<String> contextAntonyms = getVerbSynonyms(contextData);
//                if (questionAntonyms != null && contextAntonyms != null && !Collections.disjoint(questionAntonyms, contextAntonyms)) {
//                    first = contextSentence;
//                    method5++;
//                    method = 5;
//                    break;
//                }
//            }
//        }
        return first;
    }

    private static List<String> getVerbs(final Sentence sentence) {
        final List<String> verbs = new ArrayList<>();
        for (int i = 0; i < sentence.words().size(); i++)
            if (sentence.posTag(i).startsWith("V"))
                verbs.add(new Sentence(sentence.word(i)).lemmas().get(0));
        return verbs;
    }

    private static Set<String> getVerbSynonyms(final TypeDependencyUtil.TypeDependencyData data) {
        if (data.getRelation() == null)
            return null;
        return WS4J.findSynonyms(data.getRelation(), POS.v);
    }

    private static Set<String> getVerbAntonyms(final TypeDependencyUtil.TypeDependencyData data) {
        if (data.getRelation() == null)
            return null;
        return WS4J.findAntonyms(data.getRelation(), POS.v);
    }

    @Override
    public String toString() {
        return "Paragraph{" +
                "context='" + context + '\'' +
                ", qas=" + qas +
                '}';
    }
}
