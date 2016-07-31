package me.rabrg.squad.dataset;

import com.google.gson.Gson;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import me.rabrg.util.MapUtil;
import me.rabrg.util.TypeDependencyUtil;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatasetTest {

    public static void main(final String[] args) throws Exception {
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));
        final Dataset dataset = Dataset.loadDataset("dev-v1.0.json");
        test1WhoQuestion(dataset);
//        cacheTriples(dataset);
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
        for (final Article article : dataset.getData()) {
            for (final Paragraph paragraph : article.getParagraphs()) {
                for (final QuestionAnswerService qas : paragraph.getQas()) {
                    if (qas.getQuestion().startsWith("Who")) {
                        boolean answeredCorectly = false;
                        String answer = "";
                        String correctSentence = "INCORRECT";
                        for (final Sentence sentence : paragraph.getOrderedRelevancyContextSentences(qas.getQuestionSentence())) {
                            for (int i = 0; i < sentence.words().size(); i++) {
                                final String tag = sentence.nerTag(i);
                                if (tag.equals("PERSON") || tag.equals("ORGANIZATION")) {
                                    answer += " " + sentence.word(i);
                                } else if (answer.length() > 0 && !qas.getQuestion().contains(answer)) {
                                    break;
                                } else if (answer.length() > 0 && qas.getQuestion().contains(answer)) {
                                    answer = "";  // TODO: compound tag will continue
                                }
                            }
                            answer = answer.trim();
                            if (answer.length() > 0) {
                                if (answer.length() > 0) {
                                    if (qas.isAnswer(answer)) {
                                        correct++;
                                        answeredCorectly = true;
                                    }
                                    if (sentence.text().contains(qas.getAnswers().get(0).getText()))
                                        correctSentence = "CORRECT";
                                    break;
                                }
                            }
                        }
                        System.out.println(qas.getQuestion() + "\t" + qas.getAnswers().get(0).getText() + "\t" + answer + "\t" + (answeredCorectly ? "CORRECT" : "INCORRECT") + '\t' + correctSentence);
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
                                final TypeDependencyUtil.TypeDependencyData questionTriple = TypeDependencyUtil.getData(qas.getQuestion());
                                final TypeDependencyUtil.TypeDependencyData contextTriple = TypeDependencyUtil.getData(sentence.text());
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

    private static void cacheCoref(final Dataset dataset) throws IOException {
        Gson gson = new Gson();
        int i = 0;
        for (final Article article : dataset.getData()) {
            for (final Paragraph paragraph : article.getParagraphs()) {
                paragraph.getContextSentences();
            }
            final FileWriter writer = new FileWriter("dev-v1.0-coref-" + i++ + ".json");
            writer.write(gson.toJson(dataset));
            writer.close();
        }
    }

    private static void dumpAbb(final Dataset dataset) {
        final Map<String, String> abb = new HashMap<>();
        for (final Article article : dataset.getData()) {
            for (final Paragraph paragraph : article.getParagraphs()) {
                for (final Sentence sentence : paragraph.getContextSentences()) {
                    abb.putAll(TypeDependencyUtil.getAbbreviations(sentence.text()));
                }
            }
            System.out.println(abb);
        }
        for (final Map.Entry<String, String> entry : abb.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
    }

    private static void dumpSubjectRelation(final Dataset dataset) {
        final Map<String, Integer> map = new HashMap<>();
        for (final Article article : dataset.getData()) {
            for (final Paragraph paragraph : article.getParagraphs()) {
                for (final Sentence sentence : paragraph.getContextSentences()) {
                    try {
                        final TypeDependencyUtil.TypeDependencyData triple = TypeDependencyUtil.getData(sentence.text());
                        if (triple.getSubject() == null || triple.getRelation() == null)
                            continue;
                        final Sentence subjectSentence = new Sentence(triple.getSubject());
                        final boolean person = subjectSentence.nerTags().contains("PERSON");
                        final boolean organization = subjectSentence.nerTags().contains("ORGANIZATION");

                        if (person)
                            map.put(triple.getRelation() + ".PERSON", map.getOrDefault(triple.getRelation() + ".PERSON", 0) + 1);
                        if (organization)
                            map.put(triple.getRelation() + ".ORGANIZATION", map.getOrDefault(triple.getRelation() + ".ORGANIZATION", 0) + 1);
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        for (final Map.Entry<String, Integer> entry : MapUtil.orderValue(map).entrySet())
            System.out.println(entry.getKey() + "\t" + entry.getValue());
    }

    private static void cacheTriples(final Dataset dataset) throws IOException {
        final LexicalizedParser parser = LexicalizedParser.loadModel();
        final PennTreebankLanguagePack languagePack = new PennTreebankLanguagePack();
        final GrammaticalStructureFactory structureFactory = languagePack.grammaticalStructureFactory();
        final Map<String, List<TypedDependency>> map = new HashMap<>();
        for (final Article article : dataset.getData()) {
            for (final Paragraph paragraph : article.getParagraphs()) {
                for (final Sentence sentence : paragraph.getContextSentences()) {
                    map.put(sentence.text(), structureFactory.newGrammaticalStructure(parser.parse(sentence.text()))
                            .typedDependenciesCCprocessed());
                }
                for (final QuestionAnswerService qas : paragraph.getQas()) {
                    map.put(qas.getQuestion(), structureFactory.newGrammaticalStructure(parser.parse(qas.getQuestion()))
                            .typedDependenciesCCprocessed());
                }
            }
            final ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream("typed-dependencies.bin"));
            writer.writeObject(map);
            writer.close();
        }
    }
}
