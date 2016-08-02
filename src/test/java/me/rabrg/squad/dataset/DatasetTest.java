package me.rabrg.squad.dataset;

import com.google.gson.Gson;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import me.rabrg.util.MapUtil;
import me.rabrg.util.TypeDependencyUtil;
import me.rabrg.util.WordUtil;

import java.io.*;
import java.util.*;

public class DatasetTest {

    public static void main(final String[] args) throws Exception {
//        System.out.println(TypeDependencyUtil.getData("Another factor in the early 1990s that worked to radicalize the Islamist movement was the Gulf War, which brought several hundred thousand US and allied non-Muslim military personnel to Saudi Arabian soil to put an end to Saddam Hussein's occupation of Kuwait."));
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));
        final Dataset dataset = Dataset.loadDataset("dev-v1.0.json");
        printRuleReport(dataset);
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
                        String detectedAnswer = "";
                        for (final Sentence detectedSentence : paragraph.getOrderedRelevancyContextSentences(qas.getQuestionSentence())) {
                            for (int i = 0; i < detectedSentence.words().size(); i++) {
                                final String tag = detectedSentence.nerTag(i);
                                if (tag.equals("PERSON") || tag.equals("ORGANIZATION")) {
                                    detectedAnswer += " " + detectedSentence.word(i);
                                } else if (detectedAnswer.length() > 0 && !qas.getQuestion().contains(detectedAnswer)) {
                                    break;
                                } else if (detectedAnswer.length() > 0 && qas.getQuestion().contains(detectedAnswer)) {
                                    detectedAnswer = "";  // TODO: compound tag will continue
                                }
                            }
                            detectedAnswer = detectedAnswer.trim();
                            if (detectedAnswer.length() > 0) {
                                boolean correctSentence = detectedSentence.text().contains(qas.getAnswers().get(0).getText());
                                boolean correctAnswer = qas.isAnswer(detectedAnswer);
                                if (correctAnswer) {
                                    correct++;
                                }
                                if (!correctSentence && Paragraph.priority.contains(detectedSentence)) {
                                    System.out.println(qas.getQuestion() + "\t" + qas.getAnswers().get(0).getText() + "\t" + TypeDependencyUtil.getData(qas.getQuestion()) + "\t" + detectedSentence.text() + "\t" + TypeDependencyUtil.getData(detectedSentence.text()));
                                }
                                break;
                            }
                        }
//                        System.out.println(qas.getQuestion() + "\t" + qas.getAnswers().get(0).getText() + "\t" + answer + "\t" + (answeredCorectly ? "CORRECT" : "INCORRECT") + '\t' + (correctSentence ? "CORRECT" : "INCORRECT"));
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

    private static void printQuestionVerbs(final Dataset dataset) {
        for (final Article article : dataset.getData()) {
            for (final Paragraph paragraph : article.getParagraphs()) {
                for (final QuestionAnswerService qas : paragraph.getQas()) {
                    final TypeDependencyUtil.TypeDependencyData data = TypeDependencyUtil.getData(qas.getQuestion());
                    System.out.println(qas.getQuestion() + "\t" + data.getRelation());
                }
            }
        }
    }

    private static void printCoref(final Dataset dataset) {
        for (final Article article : dataset.getData()) {
            for (final Paragraph paragraph : article.getParagraphs()) {
                final Document document = new Document(paragraph.getContext());
                final Map<Integer, CorefChain> map = document.coref();
                if (map != null) {
                    for (final Map.Entry<Integer, CorefChain> entry : map.entrySet()) {
                        System.out.println(entry);
                    }
                    break;
                }
            }
        }
    }

    private static void printRuleReport(final Dataset dataset) throws IOException {
        final List<String> report = new ArrayList<>();
        for (final Article article : dataset.getData()) {
            for (final Paragraph paragraph : article.getParagraphs()) {
                for (final QuestionAnswerService qas : paragraph.getQas()) {
                    final TypeDependencyUtil.TypeDependencyData questionData = TypeDependencyUtil.getData(qas.getQuestion());
                    final Set<String> questionRelationSynonyms = WordUtil.getVerbSynonyms(questionData);
                    final Set<String> questionRelationAntonyms = WordUtil.getVerbAntonyms(questionData);
                    for (final Sentence contextSentence : paragraph.getContextSentences()) {
                        final TypeDependencyUtil.TypeDependencyData contextData = TypeDependencyUtil.getData(contextSentence.text());
                        final Set<String> contextRelationSynonyms = WordUtil.getVerbSynonyms(contextData);
                        final Set<String> contextRelationAntonyms = WordUtil.getVerbAntonyms(contextData);

                        // Question
                        final String question = qas.getQuestion();

                        // Context sentence
                        final String context = contextSentence.text();

                        // Term frequency
                        final int rule0 = WordUtil.getLemmaFrequency(contextSentence, qas.getQuestionSentence());

                        // Relation object match
                        final int rule1 = questionData.getRelation() != null && questionData.getObject() != null
                                && questionData.getRelation().equalsIgnoreCase(contextData.getRelation())
                                && questionData.getObject().equalsIgnoreCase(contextData.getObject()) ? 3 : 0;

                        // Relation object-subject match
                        final int rule2 = questionData.getRelation() != null && questionData.getObject() != null
                                && questionData.getRelation().equalsIgnoreCase(contextData.getRelation())
                                && questionData.getObject().equalsIgnoreCase(contextData.getSubject()) ? 3 : 0;

                        // Any verb match
                        final int rule3 = !Collections.disjoint(WordUtil.getVerbs(contextSentence),
                                WordUtil.getVerbs(qas.getQuestionSentence())) ? 2 : 0;

                        // Relation synonym match
                        final int rule4 = questionRelationSynonyms != null && questionRelationSynonyms.contains(contextData.getRelation())
                                || contextRelationSynonyms != null && contextRelationSynonyms.contains(questionData.getRelation()) ? 2 : 0;

                        // Relation antonym match
                        final int rule5 = questionRelationAntonyms != null && questionRelationAntonyms.contains(contextData.getRelation())
                                || contextRelationAntonyms != null && contextRelationAntonyms.contains(questionData.getRelation()) ? 2 : 0;

                        // Total score
                        final int total = rule0 + rule1 + rule2 + rule3 + rule4 + rule5;

                        // Sentence contains answer
                        final boolean containsAnswer = context.contains(qas.getAnswers().get(0).getText());

                        // Add to report
                        report.add(question + "\t" + context + "\t" + rule0 + "\t" + rule1 + "\t" + rule2 + "\t" + rule3 + "\t" + rule4 + "\t" + rule5 + "\t" + total + "\t" + (containsAnswer ? "X" : "") + "\n");
                    }
                    report.add("\n\n");
                }
            }
        }

        // Limit report size to 20k for Google sheets
        final int reportSize = report.size();
        final int maxSize = 20000;
        if (reportSize > maxSize)
            report.subList(maxSize, reportSize).clear();

        // Write report to file
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter("rule-report.tsv"))) {
            for (final String reportLine : report)
                writer.write(reportLine);
        }
    }
}
