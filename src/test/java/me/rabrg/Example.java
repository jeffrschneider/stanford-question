package me.rabrg;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import de.mpii.clausie.ClausIE;
import de.mpii.clausie.Clause;
import de.mpii.clausie.DpUtils;
import de.mpii.clausie.Proposition;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.LexicalizedParserQuery;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.semgraph.SemanticGraphFactory;
import edu.stanford.nlp.trees.Tree;

public class  Example {
    public static void main(String[] args) throws IOException {
        ClausIE clausIE = new ClausIE();
        clausIE.initParser();
        clausIE.getOptions().print(System.out, "# ");

        // input sentence
        // String sentence = "There is a ghost in the room";
        // sentence = "Bell sometimes makes products";
        String sentence = "Bell, a telecommunication company, which is based in Los Angeles, makes and distributes electronic, computer and building products.";
        // sentence = "Albert Einstein remained in Princeton.";
        // sentence = "Albert Einstein is smart.";
//        String sentence = "Bell makes electronic, computer and building products.";

        System.out.println("Input sentence   : " + sentence);

        // parse tree
        System.out.print("Parse time       : ");
        long start = System.currentTimeMillis();
        clausIE.parse(sentence);
        long end = System.currentTimeMillis();
        System.out.println((end - start) / 1000. + "s");
        System.out.print("Dependency parse : ");
        System.out.println(clausIE.getDepTree().pennString()
                .replaceAll("\n", "\n                   ").trim());
        System.out.print("Semantic graph   : ");
        System.out.println(clausIE.getSemanticGraph().toFormattedString()
                .replaceAll("\n", "\n                   ").trim());

        // clause detection
        System.out.print("ClausIE time     : ");
        start = System.currentTimeMillis();
        clausIE.detectClauses();
        clausIE.generatePropositions();
        end = System.currentTimeMillis();
        System.out.println((end - start) / 1000. + "s");
        System.out.print("Clauses          : ");
        String sep = "";
        for (Clause clause : clausIE.getClauses()) {
            System.out.println(sep + clause.toString(clausIE.getOptions()));
            sep = "                   ";
        }

        // generate propositions
        System.out.print("Propositions     : ");
        sep = "";
        for (Proposition prop : clausIE.getPropositions()) {
            System.out.println(sep + prop.toString());
            sep = "                   ";
        }
    }

//    public static void main(final String[] args) {
//        LexicalizedParser lp = LexicalizedParser
//                .loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
//        TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer
//                .factory(new CoreLabelTokenFactory(), "");
//        LexicalizedParserQuery lpq = lp.lexicalizedParserQuery();
//
//        List<CoreLabel> tokenizedSentence = tokenizerFactory.getTokenizer(
//                new StringReader("The cat and dog are hot.")).tokenize();
//
//        lpq.parse(tokenizedSentence); // what about the confidence?
//        Tree depTree = lpq.getBestParse();
//        final SemanticGraph semanticGraph = SemanticGraphFactory
//                .generateUncollapsedDependencies(depTree);
//
//        for (final SemanticGraphEdge edge : semanticGraph.edgeIterable()) {
//            if (DpUtils.isCop(edge)) {
//                System.out.println("~~~~~ANY COPULA ONE~~~~~");
//            } else if (DpUtils.isCc(edge)) {
//                System.out.println("~~~~~ANY CORDINATION ONE~~~~~");
//            }
//        }
//    }
}
