package vanzay.text.analyze;

public class Entry {
    private int id;
    private String term;
    private int frequency;

    public Entry(String term, int frequency) {
        this.term = term;
        this.frequency = frequency;
    }

    public Entry(int id, String term, int frequency) {
        this(term, frequency);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTerm() {
        return term;
    }

    public int getFrequency() {
        return frequency;
    }

    public void incFrequency(int count) {
        frequency += count;
    }
}
