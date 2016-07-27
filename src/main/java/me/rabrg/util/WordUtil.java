package me.rabrg.util;

import edu.stanford.nlp.simple.Sentence;

public class WordUtil {

    public static int getLemmaFrequency(final Sentence target, final Sentence source) {
        int frequency = 0;
        for (final String targetLemma : target.lemmas()) {
            for (final String sourceLemma : source.lemmas()) {
                if (targetLemma.equals(sourceLemma))
                    frequency++;
            }
        }
        return frequency;
    }
}
