package vanzay.text.analyse;

import org.junit.jupiter.api.Test;
import vanzay.text.analyze.Dictionary;
import vanzay.text.analyze.Entry;
import vanzay.text.analyze.Vocabulary;
import vanzay.text.exception.UnsupportedLanguageException;
import vanzay.text.language.Language;
import vanzay.text.language.LanguageDetector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class VocabularyTests {

    private static final String DICTIONARIES_PATH = "/var/lucene_indices/";

    @Test
    void buildVocabulary() throws IOException, UnsupportedLanguageException {
        String text = Files.readString(Paths.get("/var/vio/test.txt"), StandardCharsets.UTF_8);

        System.out.println("Start vocabulary building");
        printMemoryUsage();

        long start = System.currentTimeMillis();

        Language language = LanguageDetector.detect(DICTIONARIES_PATH, text);
        Dictionary dictionary = Dictionary.open(DICTIONARIES_PATH, language);
        Vocabulary vocabulary = new Vocabulary(language, dictionary);
        vocabulary.process(text);
        dictionary.close();

        System.out.println("Vocabulary building time: " + (System.currentTimeMillis() - start));
        printMemoryUsage();

        System.out.println("--- PHRASES ----------------------------");
        printVocabularyData(vocabulary.getPhrases());
        System.out.println("--- MISTAKES ---------------------------");
        printVocabularyData(vocabulary.getUnexpectedWords());
        System.out.println("--- PROPER NOUNS -----------------------");
        printVocabularyData(vocabulary.getProperNouns());
    }

    private static void printVocabularyData(Collection<Entry> entries) {
        for (Entry entry : sort(entries)) {
            System.out.println(entry.getTerm() + " " + entry.getFrequency());
        }
    }

    private static List<Entry> sort(Collection<Entry> collection) {
        List<Entry> entries = new ArrayList<>(collection);
        entries.sort((o1, o2) -> Integer.compare(o2.getFrequency(), o1.getFrequency()));
        return entries;
    }

    private static void printMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        NumberFormat format = NumberFormat.getInstance();
        System.out.println("Free memory: " + format.format(freeMemory / 1024));
        System.out.println("Allocated memory: " + format.format(allocatedMemory / 1024));
        System.out.println("Max memory: " + format.format(maxMemory / 1024));
        System.out.println("Total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
    }
}
