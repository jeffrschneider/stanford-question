package me.rabrg.squad.dataset;

import edu.stanford.nlp.simple.Sentence;
import me.rabrg.util.MapUtil;
import me.rabrg.util.TripleUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DatasetTest {

    public static void main(final String[] args) throws IOException {
        final Dataset dataset = Dataset.loadDataset("dev-v1.0.json");
        test1WhoQuestion(dataset);
    }

    private static void printOrderedFirstWord(final Dataset dataset) {
        final Map<String, Integer> map = new HashMap<>();
        for (final Article article : dataset.getData()) {
            for (final Paragraph paragraph : article.getParagraphs()) {
                for (final QuestionAnswerService qas : paragraph.getQas()) {
                    final String key = qas.getQuestion().split(" ")[0];
                    map.put(key, map.getOrDefault(key, 0) + 1);
                }
            }
        }
        for (final Map.Entry<String, Integer> entry : MapUtil.orderValue(map).entrySet()) {
            if (entry.getValue() >= 10)
                System.out.println(entry.getKey() + '\t' + entry.getValue());
        }
    }

    private static void printSentenceStartWith(final Dataset dataset, final String startWith) {
        for (final Article article : dataset.getData()) {
            for (final Paragraph paragraph : article.getParagraphs()) {
                for (final QuestionAnswerService qas : paragraph.getQas()) {
                    if (qas.getQuestion().startsWith(startWith))
                        System.out.println(qas.getQuestion() + '\t' + qas.getAnswers().get(0).getText());
                }
            }
        }
    }

    // TODO: make multi-tag not retarded
    private static void printSentenceAnswerNERTag(final Dataset dataset, final String... tag) {
        for (final Article article : dataset.getData()) {
            for (final Paragraph paragraph : article.getParagraphs()) {
                for (final QuestionAnswerService qas : paragraph.getQas()) {
                    boolean all = true;
                    String relevantTag = null;
                    for (final String nerTag : qas.getAnswers().get(0).getTextSentence().nerTags()) {
                        if (!nerTag.equals(tag[0]) && !nerTag.equals(tag[1])) {
                            all = false;
                            break;
                        } else {
                            relevantTag = nerTag;
                        }
                    }
                    if (all)
                        System.out.println(qas.getQuestion() + '\t' + qas.getAnswers().get(0).getText() + '\t' + relevantTag);
                }
            }
        }
    }

    private static void test1WhoQuestion(final Dataset dataset) {
        int correct = 0, total = 0;
        int articles = 0;
        for (final Article article : dataset.getData()) {
            if (articles++ == 5)
                break;
            for (final Paragraph paragraph : article.getParagraphs()) {
                for (final QuestionAnswerService qas : paragraph.getQas()) {
                    if (qas.getQuestion().startsWith("Who")) {
                        boolean answeredCorectly = false;
                        String answer = "";
                        for (final Sentence sentence : paragraph.getOrderedRelevancyContextSentences(qas.getQuestionSentence())) {
                            for (int i = 0; i < sentence.words().size(); i++) {
                                final String tag = sentence.nerTag(i);
                                if (tag.equals("PERSON") || tag.equals("ORGANIZATION")) {
                                    answer += " " + sentence.word(i);
                                } else if (answer.length() > 0 && !qas.getQuestion().contains(answer)) {
                                    break;
                                } else if (answer.length() > 0 && qas.getQuestion().contains(answer)) {
                                    answer = "";
                                }
                            }
                            answer = answer.trim();
                            if (answer.length() > 0) {
                                if (qas.isAnswer(answer)) {
                                    correct++;
                                    answeredCorectly = true;
                                }
                                break;
                            }
                        }
                        System.out.println(qas.getQuestion() + "\t" + qas.getAnswers().get(0).getText() + "\t" + answer + "\t" + answeredCorectly + "\t" + (answeredCorectly ? "CORRECT" : "INCORRECT"));
                        total++;
                    }
                }
            }
        }
        System.out.println(correct + "/" + total);
    }

    private static void printQuestionTrees(final Dataset dataset) {
        for (final Article article : dataset.getData()) {
            for (final Paragraph paragraph : article.getParagraphs()) {
                for (final QuestionAnswerService qas : paragraph.getQas()) {
                    for (final Sentence contextSentence : paragraph.getContextSentences()) {
                        if (contextSentence.text().contains(qas.getAnswers().get(0).getText())) {
                            System.out.println("Question tree:");
                            qas.getQuestionSentence().parse().pennPrint();
                            System.out.println("Context sentence tree:");
                            contextSentence.parse().pennPrint();
                            System.out.println("=========");
                            break;
                        }
                    }
                }
            }
        }
    }

    private static void printQATriples(final Dataset dataset) {
        int totalMatches = 0, total = 0;
        for (final Article article : dataset.getData()) {
            for (final Paragraph paragraph : article.getParagraphs()) {
                for (final QuestionAnswerService qas : paragraph.getQas()) {
                    if (qas.getQuestion().startsWith("Who")) {
                        for (final Sentence sentence : paragraph.getContextSentences()) {
                            if (sentence.text().contains(qas.getAnswers().get(0).getText())) {
                                final TripleUtil.Triple questionTriple = TripleUtil.getTriple(qas.getQuestion());
                                final TripleUtil.Triple contextTriple = TripleUtil.getTriple(sentence.text());
                                int matches = 0;
                                if (questionTriple.getSubject() != null && contextTriple.getSubject() != null && questionTriple.getSubject().equals(contextTriple.getSubject()))
                                    matches++;
                                if (questionTriple.getRelation() != null && contextTriple.getRelation() != null && questionTriple.getRelation().equals(contextTriple.getRelation()))
                                    matches++;
                                if (questionTriple.getObject() != null && contextTriple.getObject() != null && questionTriple.getObject().equals(contextTriple.getObject()))
                                    matches++;
                                if (matches > 0)
                                    totalMatches++;
                                total++;
                                System.out.println(qas.getQuestion() + "\t" + questionTriple.getSubject() + "\t" + questionTriple.getRelation() + "\t" + questionTriple.getObject() + "\t" + sentence.text() + "\t" + contextTriple.getSubject() + "\t" + contextTriple.getRelation() + "\t" + contextTriple.getObject() + "\t" + matches);
                            }
                        }
                    }
                }
            }
        }
        System.out.println(totalMatches + "/" + total);
    }
}
