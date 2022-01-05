package vanzay.text.normalize;

import vanzay.text.language.Language;

public class Factory {

    public static Normalizer getNormalizer(Language language) {
        if (language == Language.ENGLISH) {
            return new EnglishNormalizer();
        }
        return new Normalizer();
    }
}
