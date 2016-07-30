package me.rabrg.squad.dataset;

import edu.stanford.nlp.simple.Sentence;
import me.rabrg.util.MapUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubjectRelationTest {

    public static void main(final String[] args) throws IOException {
        final List<String> lines = Files.readAllLines(Paths.get("context-subject-relation.tsv"));
        final Map<String, Integer> map = new HashMap<>();
        for (final String line : lines) {
            final String[] elements = line.split("\t");
            if (elements.length != 2) {
                System.out.println(line);
                continue;
            }
            final String subject = elements[0];
            final String relation = elements[1];

            final Sentence subjectSentence = new Sentence(subject);
            final boolean person = subjectSentence.nerTags().contains("PERSON");
            final boolean organization = subjectSentence.nerTags().contains("ORGANIZATION");

            if (person)
                map.put(relation + ".PERSON", map.getOrDefault(relation + ".PERSON", 0) + 1);
            if (organization)
                map.put(relation + ".ORGANIZATION", map.getOrDefault(relation + ".ORGANIZATION", 0) + 1);
        }
        for (final Map.Entry<String, Integer> entry : MapUtil.orderValue(map).entrySet())
            System.out.println(entry.getKey() + "\t" + entry.getValue());
    }
}
