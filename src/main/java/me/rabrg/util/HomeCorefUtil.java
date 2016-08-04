package me.rabrg.util;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import me.rabrg.squad.dataset.Article;
import me.rabrg.squad.dataset.Dataset;
import me.rabrg.squad.dataset.Paragraph;

import java.util.*;

public class HomeCorefUtil {

    private static final List<String> SINGULAR_PRONOUNS = Arrays.asList("he", "him", "himself", "his", "she", "her",
            "herself", "hers", "it", "itself", "its", "themself");

    private static final List<String> PLURAL_PRONOUNS = Arrays.asList("they", "them", "themselves", "theirself",
            "theirselves", "their", "theirs");

    private static final LexicalizedParser parser = LexicalizedParser.loadModel();
    private static final PennTreebankLanguagePack languagePack = new PennTreebankLanguagePack();
    private static final GrammaticalStructureFactory structureFactory = languagePack.grammaticalStructureFactory();

    public static void main(final String[] args) throws Exception {
        final Dataset dataset = Dataset.loadDataset("dev-v1.0.json");
        for (final Article article : dataset.getData()) {
            Collection<String> lastSingular = null, lastPlural = null;
            for (final Paragraph paragraph : article.getParagraphs()) {
                for (final Sentence sentence : paragraph.getContextSentences()) {
                    if (sentence.text().contains("champion")) // Skips Stanford parse error
                        continue;

                    // Populate an ordered map consisting of word index keys and word values
                    final Map<Integer, String> subjectIndexWordMap = getSubjectIndexWord(sentence.text());

                    // Count the occurrences of singular and plural nouns in the subject to determine the plurality of
                    // the subject
                    int singularCount = 0, pluralCount = 0;
                    final List<String> posTags = sentence.posTags();
                    for (final Map.Entry<Integer, String> entry : subjectIndexWordMap.entrySet()) {
                        switch (posTags.get(entry.getKey() - 1)) { // The line that wasted an hour of my life
                            case "NN":
                            case "NNP":
                                singularCount++;
                                break;
                            case "NNS":
                            case "NNPS":
                                pluralCount++;
                                break;
                        }
                    }

                    // Replace the previous subject of the subject's plurality
                    if (singularCount > pluralCount)
                        lastSingular = subjectIndexWordMap.values();
                    else if (pluralCount != 0) // TODO: are all valid subjects nouns?
                        lastPlural = subjectIndexWordMap.values();

                    System.out.println("Sentence: " + sentence);

                    // Iterate through the sentence replacing pronouns with the last subject of its type
                    final List<String> words = new ArrayList<>(sentence.words());
                    for (int i = 0; i < words.size(); i++) {
                        if (SINGULAR_PRONOUNS.contains(words.get(i).toLowerCase()) && lastSingular != null) {
                            System.out.println("\tReplacing: " + words.get(i));
                            System.out.println("\tWith: " + lastSingular);
                            words.remove(i);
                            words.addAll(i, lastSingular);
                        } else if (PLURAL_PRONOUNS.contains(words.get(i).toLowerCase()) && lastPlural != null) {
                            System.out.println("\tReplacing: " + words.get(i));
                            System.out.println("\tWith: " + lastPlural);
                            words.remove(i);
                            words.addAll(i, lastPlural);
                        }
                    }
                    System.out.println();
                }
            }
        }
    }

    private static Map<Integer, String> getSubjectIndexWord(final String text) {
        final Map<Integer, String> map = new TreeMap<>();

        final List<TypedDependency> dependencies = structureFactory.newGrammaticalStructure(parser.parse(text))
                .typedDependenciesCCprocessed();
        String rootSubject = null;
        for (final TypedDependency dependency : dependencies)
            if (dependency.reln().toString().contains("root"))
                rootSubject = dependency.dep().word();

        for (int i = dependencies.size() - 1; i >= 0; i--) {
            final TypedDependency dependency = dependencies.get(i);
            if ((dependency.reln().toString().contains("subj") || dependency.reln().toString().contains("compound"))
                    && dependency.gov().word().equals(rootSubject))
                map.put(dependency.dep().index(), dependency.dep().word());
        }
        return map;
    }
}
