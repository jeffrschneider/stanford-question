package me.rabrg.squad.sentrank;

import edu.stanford.nlp.simple.Sentence;

public interface SentenceRanker {

    double rankSentence(final Sentence context, final Sentence question);
}
