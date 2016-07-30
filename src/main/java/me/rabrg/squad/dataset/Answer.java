package me.rabrg.squad.dataset;

import com.google.gson.annotations.SerializedName;
import edu.stanford.nlp.simple.Sentence;

public final class Answer {

    @SerializedName("answer_start")
    private int answerStart;
    private String text;

    private transient Sentence textSentence;

    public int getAnswerStart() {
        return answerStart;
    }

    public String getText() {
        return text;
    }

    public Sentence getTextSentence() {
        if (textSentence == null)
            textSentence = new Sentence(text);
        return textSentence;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "answerStart=" + answerStart +
                ", text='" + text + '\'' +
                '}';
    }
}
