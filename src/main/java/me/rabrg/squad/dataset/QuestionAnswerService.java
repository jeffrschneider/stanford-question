package me.rabrg.squad.dataset;

import edu.stanford.nlp.simple.Sentence;

import java.util.List;

public final class QuestionAnswerService {

    private List<Answer> answers;
    private String id;
    private String question;

    private Sentence questionSentence;

    public List<Answer> getAnswers() {
        return answers;
    }

    public String getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public Sentence getQuestionSentence() {
        if (questionSentence == null)
            questionSentence = new Sentence(question);
        return questionSentence;
    }

    @Override
    public String toString() {
        return "QuestionAnswerService{" +
                "answers=" + answers +
                ", id='" + id + '\'' +
                ", question='" + question + '\'' +
                '}';
    }
}
