package vanzay.text.normalize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnglishNormalizer extends Normalizer {

    @Override
    public List<String> normalize(String word) {
        word = word
                .replaceAll("[\u002D\u2010\u2011\uFE63\uFF0D]", "-")
                .replaceAll("[\uFF07\u2019]", "'");

        return word.contains("'") ? fixApostrophe(word) : Collections.singletonList(word);
    }

    private static List<String> fixApostrophe(String word) {
        List<String> list = new ArrayList<>();

        if (word.endsWith("n't")) {
            switch (word) {
                case "won't" -> {
                    list.add("will");
                    list.add("not");
                }
                case "can't" -> {
                    list.add("can");
                    list.add("not");
                }
                default -> fixContraction(word, list, "n't", "not");
            }
        } else if (word.endsWith("'ll")) {
            // TODO shall?
            fixContraction(word, list, "'ll", "will");
        } else if (word.endsWith("'ve")) {
            fixContraction(word, list, "'ve", "have");
        } else if (word.endsWith("'re")) {
            fixContraction(word, list, "'re", "are");
        } else if (word.endsWith("in'")) {
            list.add(word.substring(0, word.lastIndexOf("'")) + "g");
        } else if (word.endsWith("'")
                // TODO 's - has/is/'s
                || word.endsWith("'s")
                // TODO 'd - had/would
                || word.endsWith("'d")) {
            String mainPart = word.substring(0, word.lastIndexOf("'"));
            if (!mainPart.isEmpty()) {
                list.add(mainPart);
            }
        } else if (word.equals("I'm")) {
            list.add("I");
            list.add("am");
        }

        return list;
    }

    private static void fixContraction(String word, List<String> list, String shortenedForm, String normalForm) {
        String mainPart = word.substring(0, word.lastIndexOf(shortenedForm));
        if (!mainPart.isEmpty()) {
            list.add(mainPart);
        }
        list.add(normalForm);
    }
}
