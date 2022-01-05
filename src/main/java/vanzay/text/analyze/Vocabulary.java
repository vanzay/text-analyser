package vanzay.text.analyze;

import org.apache.lucene.document.Document;
import vanzay.text.language.Language;
import vanzay.text.normalize.Factory;
import vanzay.text.normalize.Normalizer;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class Vocabulary {

    private Language language;
    private int wordTotal;
    private int phrasesTotal;
    private int unexpectedWordsTotal;
    private int properNounsTotal;
    private Map<String, Entry> phraseMap = new HashMap<>();
    private Map<String, Entry> unexpectedWordMap = new HashMap<>();
    private Map<String, Entry> properNounMap = new HashMap<>();

    private Dictionary dictionary;
    private Map<String, Document> cache = new HashMap<>();
    private Set<String> properNounSet = new HashSet<>();
    private List<String[]> properNounCandidates = new ArrayList<>();

    public Vocabulary(Language language, Dictionary dictionary) {
        this.language = language;
        this.dictionary = dictionary;
    }

    public void process(String text) throws IOException {
        Normalizer normalizer = Factory.getNormalizer(language);
        StandardTokenizerExt scanner = new StandardTokenizerExt(new StringReader(text));
        List<String> wordList = new ArrayList<>();
        int delimiterPos = 0;
        while (true) {
            int tokenType = scanner.getNextToken();
            if (tokenType == -1) {
                break;
            }

            String delimiter = text.substring(delimiterPos, scanner.yychar()).trim();
            if (!delimiter.isEmpty() || tokenType != StandardTokenizerExt.WORD_TYPE) {
                processSentenceFragment(wordList, !",".equals(delimiter));
                wordTotal += wordList.size();
                wordList.clear();
            }

            String token = scanner.yytext();
            if (tokenType == StandardTokenizerExt.WORD_TYPE) {
                wordList.addAll(normalizer.normalize(token));
            }

            delimiterPos = scanner.yychar() + token.length();
        }

        fixProperNouns();
    }

    private void processSentenceFragment(List<String> wordList, boolean checkFirstWord) throws IOException {
        for (int i = 0; i < wordList.size(); i++) {
            String currentWord = wordList.get(i);

            Document phraseDocument = getDocument(currentWord);

            if (i == 0 && checkFirstWord && Character.isUpperCase(currentWord.charAt(0))) {
                if (phraseDocument == null) {
                    phraseDocument = getDocument(currentWord.toLowerCase());
                }
                if (phraseDocument != null) {
                    // TODO <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                    StringBuilder term = new StringBuilder();
                    int termSize = 0;
                    for (int j = i + 1; j < wordList.size(); j++) {
                        String word = wordList.get(j);
                        if (!Character.isUpperCase(word.charAt(0))) {
                            break;
                        }
                        if (termSize != 0) {
                            term.append(' ');
                        }
                        term.append(word);
                        termSize++;
                    }
                    properNounCandidates.add(new String[]{currentWord, termSize == 0 ? null : term.toString()});
                    i += termSize;
                    // TODO >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                    continue;
                }
            }

            if (phraseDocument == null) {
                // TODO German language
                if (Character.isUpperCase(currentWord.charAt(0))) {
                    properNounSet.add(currentWord);
                    StringBuilder term = new StringBuilder(currentWord);
                    for (int j = i + 1; j < wordList.size(); j++) {
                        String word = wordList.get(j);
                        if (!Character.isUpperCase(word.charAt(0))) {
                            break;
                        }
                        term.append(' ').append(word);
                        properNounSet.add(word);
                        i++;
                    }
                    processPhrase(term.toString(), properNounMap);
                    properNounsTotal++;
                } else {
                    boolean processed = false;
                    if (currentWord.contains("-")) {
                        // TODO merge by space
                        String[] wordParts = currentWord.split("-", -1);
                        List<Document> docs = new ArrayList<>(wordParts.length);
                        for (String part : wordParts) {
                            Document document = getDocument(part);
                            if (document == null) {
                                break;
                            }
                            docs.add(document);
                        }
                        if (wordParts.length == docs.size()) {
                            for (Document doc : docs) {
                                processPhrase(Integer.parseInt(doc.get("id")), doc.get("term"), phraseMap);
                                phrasesTotal++;
                            }
                            processed = true;
                        }
                    }

                    if (!processed) {
                        processPhrase(currentWord, unexpectedWordMap);
                        unexpectedWordsTotal++;
                    }
                }
            } else {
                StringBuilder term = new StringBuilder(currentWord);
                for (int j = i + 1; j < wordList.size(); j++) {
                    term.append(' ').append(wordList.get(j));
                    Document document = getDocument(term.toString());
                    if (document == null) {
                        break;
                    }
                    phraseDocument = document;
                    i++;
                }
                processPhrase(Integer.parseInt(phraseDocument.get("id")), phraseDocument.get("term"), phraseMap);
                phrasesTotal++;
            }
        }
    }

    private Document getDocument(String phrase) throws IOException {
        Document document = cache.get(phrase);
        if (document == null) {
            document = dictionary.getPhraseDoc(phrase);
            cache.put(phrase, document);
        }
        return document;
    }

    private void fixProperNouns() {
        for (String[] parts : properNounCandidates) {
            if (parts[1] == null) {
                fixProperNoun(parts[0]);
            } else {
                String joinedParts = parts[0] + ' ' + parts[1];
                if (properNounMap.containsKey(joinedParts) || properNounSet.contains(joinedParts)) {
                    processPhrase(joinedParts, properNounMap);
                } else {
                    fixProperNoun(parts[0]);
                    fixProperNoun(parts[1]);
                }
            }
        }
    }

    private void fixProperNoun(String candidate) {
        if (properNounMap.containsKey(candidate) || properNounSet.contains(candidate)) {
            processPhrase(candidate, properNounMap);
        } else {
            Entry phrase = phraseMap.get(candidate);
            if (phrase == null) {
                phrase = phraseMap.get(candidate.toLowerCase());
            }
            if (phrase == null) {
                processPhrase(candidate, unexpectedWordMap);
            } else {
                phrase.incFrequency(1);
            }
        }
    }

    public Language getLanguage() {
        return language;
    }

    public int getWordTotal() {
        return wordTotal;
    }

    public int getPhrasesTotal() {
        return phrasesTotal;
    }

    public int getUnexpectedWordsTotal() {
        return unexpectedWordsTotal;
    }

    public int getProperNounsTotal() {
        return properNounsTotal;
    }

    public Collection<Entry> getPhrases() {
        return Collections.unmodifiableCollection(phraseMap.values());
    }

    public Collection<Entry> getUnexpectedWords() {
        return Collections.unmodifiableCollection(unexpectedWordMap.values());
    }

    public Collection<Entry> getProperNouns() {
        return Collections.unmodifiableCollection(properNounMap.values());
    }

    private static void processPhrase(int id, String phrase, Map<String, Entry> map) {
        Entry entry = map.get(phrase);
        if (entry == null) {
            entry = new Entry(id, phrase, 1);
            map.put(phrase, entry);
        } else {
            entry.incFrequency(1);
        }
    }

    private static void processPhrase(String phrase, Map<String, Entry> map) {
        processPhrase(0, phrase, map);
    }
}
