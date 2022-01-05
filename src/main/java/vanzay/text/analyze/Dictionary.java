package vanzay.text.analyze;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import vanzay.text.language.Language;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Dictionary implements AutoCloseable {

    private Directory directory;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;

    public static Dictionary open(String indicesPath, Language lang) throws IOException {
        Dictionary dictionary = new Dictionary();
        dictionary.init(indicesPath, lang);
        return dictionary;
    }

    private void init(String indicesPath, Language lang) throws IOException {
        Path path = Paths.get(indicesPath, lang.getIso3());
        if (Files.notExists(path)) {
            throw new FileNotFoundException("Dictionary index doesn't exist: " + path);
        }
        directory = NIOFSDirectory.open(path);
        indexReader = DirectoryReader.open(directory);
        indexSearcher = new IndexSearcher(indexReader);
    }

    public boolean hasPhrase(String phrase) throws IOException {
        return getPhraseDoc(phrase) != null;
    }

    public Document getPhraseDoc(String term) throws IOException {
        Query query = new PhraseQuery("term", term);
        ScoreDoc[] hits = indexSearcher.search(query, 1).scoreDocs;
        if (hits.length > 0) {
            Document doc = indexSearcher.doc(hits[0].doc);
            if (term.equals(doc.get("term"))) {
                return doc;
            }
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        indexReader.close();
        directory.close();
    }
}
