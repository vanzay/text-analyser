package vanzay.text.language;

public enum Language {
    ENGLISH("eng"), SPANISH("spa");

    private String iso3;

    Language(String iso3) {
        this.iso3 = iso3;
    }

    public String getIso3() {
        return iso3;
    }
}
