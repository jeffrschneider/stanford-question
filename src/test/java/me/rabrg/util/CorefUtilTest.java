package me.rabrg.util;

import me.rabrg.squad.dataset.Dataset;
import me.rabrg.squad.dataset.Paragraph;

public class CorefUtilTest {

    public static void main(final String[] args) throws Exception {
        final Paragraph paragraph = Dataset.loadDataset("dev-v1.0.json").getData().get(0).getParagraphs().get(1);
        System.out.println(paragraph.getContextSentences());
        CorefUtil.replaceContextPronouns(paragraph);
//        System.out.println(paragraph.getContextSentences());
    }
}
