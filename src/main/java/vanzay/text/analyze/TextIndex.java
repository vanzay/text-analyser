package vanzay.text.analyze;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextIndex {

    private static final String BODY_FIELD = "body";

    public static Directory build(String text) throws IOException {
        Directory directory = new ByteBuffersDirectory();
        // StandardAnalyzer is case-insensitive, so use custom analyzer
        IndexWriterConfig config = new IndexWriterConfig(new SimpleAnalyzer());
        try (IndexWriter indexWriter = new IndexWriter(directory, config)) {
            indexWriter.addDocument(prepareDocument(text));
        }
        return directory;
    }

    public static List<Entry> getEntries(String text) throws IOException {
        try (Directory directory = build(text)) {
            return getEntries(directory);
        }
    }

    public static List<Entry> getEntries(Directory directory) throws IOException {
        try (IndexReader indexReader = DirectoryReader.open(directory)) {
            Terms terms = indexReader.getTermVector(0, BODY_FIELD);
            if (terms != null) {
                List<Entry> result = new ArrayList<>((int) terms.size());
                TermsEnum iter = terms.iterator();
                BytesRef term;
                while ((term = iter.next()) != null) {
                    result.add(new Entry(term.utf8ToString(), (int) iter.totalTermFreq()));
                }
                return result;
            }
        }
        return Collections.emptyList();
    }

    private static Document prepareDocument(String text) {
        FieldType type = new FieldType();
        type.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        type.setStored(true);
        type.setStoreTermVectors(true);

        Document doc = new Document();
        doc.add(new Field(BODY_FIELD, text, type));
        return doc;
    }
}
