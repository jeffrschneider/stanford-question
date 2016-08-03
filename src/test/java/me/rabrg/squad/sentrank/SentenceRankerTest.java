package me.rabrg.squad.sentrank;

import edu.stanford.nlp.simple.Sentence;
import me.rabrg.squad.dataset.Article;
import me.rabrg.squad.dataset.Dataset;
import me.rabrg.squad.dataset.Paragraph;
import me.rabrg.squad.dataset.QuestionAnswerService;

public class SentenceRankerTest {

    public static void main(final String[] args) throws Exception {
        final Dataset dataset = Dataset.loadDataset("dev-v1.0.json");
        final SentenceRanker sentenceRanker = new QuestionSentenceRanker();

        int correct = 0, possible = 0;
        for (final Article article : dataset.getData()) {
            for (final Paragraph paragraph : article.getParagraphs()) {
                for (final QuestionAnswerService qas : paragraph.getQas()) {
                    // Who questions only
                    if (!qas.getQuestion().toLowerCase().startsWith("who"))
                        continue;
                    double bestScore = 0;
                    Sentence bestSentence = null;
                    for (final Sentence contextSentence : paragraph.getContextSentences()) {
                        final double score = sentenceRanker.rankSentence(contextSentence, qas.getQuestionSentence());
                        if (score > bestScore)
                            bestSentence = contextSentence;
                    }
                    if (bestSentence != null && bestSentence.text().contains(qas.getAnswers().get(0).getText()))
                        correct++;
                    possible++;
                }
            }
            System.out.println(correct + "/" + possible);
        }
    }
}
