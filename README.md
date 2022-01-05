# text-analyser

## Usage

### Language detection

```java
Language language = LanguageDetector.detect(DICTIONARIES_PATH, text);
```

### Index building

```java
List<Entry> entries = TextIndex.getEntries(text)
```

### Vocabulary building
```java
Dictionary dictionary = Dictionary.open(DICTIONARIES_PATH, language);
Vocabulary vocabulary = new Vocabulary(language, dictionary);
vocabulary.process(text);
dictionary.close();
```
