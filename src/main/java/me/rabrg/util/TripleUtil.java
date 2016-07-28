package me.rabrg.util;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

import java.util.List;

public class TripleUtil {

    private static final LexicalizedParser parser = LexicalizedParser.loadModel();
    private static final PennTreebankLanguagePack languagePack = new PennTreebankLanguagePack();
    private static final GrammaticalStructureFactory structureFactory = languagePack.grammaticalStructureFactory();

    public static Triple getTriple(final String text) {
        final List<TypedDependency> dependencies = structureFactory.newGrammaticalStructure(parser.parse(text))
                .typedDependenciesCCprocessed();
        return new Triple(getSubject(dependencies), getRelation(dependencies), getObject(dependencies));
    }

    private static String getSubject(final List<TypedDependency> dependencies) {
        String rootSubject = null, subject = null;
        for (int i = dependencies.size() - 1; i >= 0; i--) {
            final TypedDependency dependency = dependencies.get(i);
            if (dependency.reln().toString().contains("subj"))
                rootSubject = subject = dependency.dep().word();
            else if (dependency.reln().toString().contains("compound") && dependency.gov().word().equals(rootSubject))
                subject = dependency.dep().word() + " " + subject;
        }
        if (subject == null)
            return null;
        String lemmaSubject = "";
        for (final String lemma : new Sentence(subject).lemmas()) {
            lemmaSubject += lemma + " ";
        }
        return lemmaSubject.trim();
    }

    private static String getRelation(final List<TypedDependency> dependencies) {
        for (final TypedDependency dependency : dependencies)
            if (dependency.reln().toString().contains("root"))
                return new Sentence(dependency.dep().word()).lemmas().get(0);
        return null;
    }

    private static String getObject(final List<TypedDependency> dependencies) {
        String rootDirectObject = null, directObject = null;
        for (int i = dependencies.size() - 1; i >= 0; i--) {
            final TypedDependency dependency = dependencies.get(i);
            if (dependency.reln().toString().contains("dobj"))
                rootDirectObject = directObject = dependency.dep().word();
            else if (dependency.reln().toString().contains("compound") && dependency.gov().word().equals(rootDirectObject))
                directObject = dependency.dep().word() + " " + directObject;
        }
        if (directObject == null)
            return null;
        String lemmaDirectObject = "";
        for (final String lemma : new Sentence(directObject).lemmas()) {
            lemmaDirectObject += lemma + " ";
        }
        return lemmaDirectObject.trim();
    }

    public static class Triple {

        private final String subject;
        private final String relation;
        private final String object;

        public Triple(String subject, String relation, String object) {
            this.subject = subject;
            this.relation = relation;
            this.object = object;
        }

        public String getSubject() {
            return subject;
        }

        public String getRelation() {
            return relation;
        }

        public String getObject() {
            return object;
        }

        @Override
        public String toString() {
            return "Triple{" +
                    "subject='" + subject + '\'' +
                    ", relation='" + relation + '\'' +
                    ", object='" + object + '\'' +
                    '}';
        }
    }
}
