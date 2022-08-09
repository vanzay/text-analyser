package vanzay.text.analyze;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

class SimpleAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        return new TokenStreamComponents(new StandardTokenizer());
    }
}
