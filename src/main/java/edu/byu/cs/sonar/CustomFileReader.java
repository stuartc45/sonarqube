package edu.byu.cs.sonar;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

/** Reads UTF-8 files and builds a sentence from matching words. */
final class CustomFileReader {
    private final String path;
    private String newSentence = "";
    private int count;

    CustomFileReader(final String fileName) {
        path = Objects.requireNonNull(fileName, "File name must not be null");
    }

    private Scanner createScanner() throws FileNotFoundException {
        return new Scanner(new InputStreamReader(
                new FileInputStream(path), StandardCharsets.UTF_8));
    }

    /** Counts from the start of the file on every call. */
    int howManyWordsInFile() throws FileNotFoundException {
        count = 0;
        try (Scanner scanner = createScanner()) {
            while (scanner.hasNext()) {
                scanner.next();
                count++;
            }
        }
        return count;
    }

    /** Returns a word using a one-based index, or an empty string for zero. */
    String returnThatWord(final int index) throws FileNotFoundException {
        if (index < 0) {
            throw new IllegalArgumentException("Word index must not be negative");
        }
        try (Scanner scanner = createScanner()) {
            String returnWord = "";
            for (int i = 0; i < index; i++) {
                returnWord = scanner.next();
            }
            return returnWord;
        }
    }

    /** Appends the first word containing the supplied, case-sensitive text. */
    void findNewWord(final CharSequence letter) throws FileNotFoundException {
        Objects.requireNonNull(letter, "Search text must not be null");
        try (Scanner scanner = createScanner()) {
            while (scanner.hasNext()) {
                String word = scanner.next();
                if (word.contains(letter)) {
                    newSentence += word + " ";
                    return;
                }
            }
        }
        throw new NoSuchElementException("No word contains: " + letter);
    }

    String getNewSentence() {
        return newSentence;
    }

    void setNewSentence(final String betterSentence) {
        newSentence = Objects.requireNonNull(betterSentence, "Sentence must not be null");
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, newSentence, count);
    }

    @Override
    public String toString() {
        return newSentence + " " + count;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CustomFileReader)) {
            return false;
        }
        CustomFileReader comparedReader = (CustomFileReader) object;
        return count == comparedReader.count
                && path.equals(comparedReader.path)
                && newSentence.equals(comparedReader.newSentence);
    }
}
