package vanzay.text.language;

import vanzay.text.analyze.Dictionary;
import vanzay.text.analyze.Entry;
import vanzay.text.analyze.TextIndex;
import vanzay.text.exception.UnsupportedLanguageException;
import vanzay.text.normalize.Factory;
import vanzay.text.normalize.Normalizer;

import java.io.IOException;
import java.util.List;

public class LanguageDetector {

    private static final int TOP_WORDS_LIMIT = 10;
    private static final int FAILS_LIMIT = 3;
    private static final int VALUABLE_WORD_MIN_LENGTH = 4;

    public static Language detect(String indicesPath, String text) throws IOException, UnsupportedLanguageException {
        List<Entry> entries = TextIndex.getEntries(text)
                .stream()
                // TODO German language
                .filter((word) -> word.getTerm().length() >= VALUABLE_WORD_MIN_LENGTH && Character.isLowerCase(word.getTerm().charAt(0)))
                .sorted((o1, o2) -> Integer.compare(o2.getFrequency(), o1.getFrequency()))
                .toList();

        for (Language lang : Language.values()) {
            Normalizer normalizer = Factory.getNormalizer(lang);
            try (Dictionary dictionary = Dictionary.open(indicesPath, lang)) {
                int attempt = 0;
                int success = 0;
                while (attempt < TOP_WORDS_LIMIT && attempt < entries.size() && (attempt - success < FAILS_LIMIT)) {
                    List<String> normalized = normalizer.normalize(entries.get(attempt).getTerm());
                    if (dictionary.hasPhrase(normalized.get(0))) {
                        success++;
                    }
                    attempt++;
                }
                if (attempt - success < FAILS_LIMIT) {
                    return lang;
                }
            }
        }

        throw new UnsupportedLanguageException();
    }
}
