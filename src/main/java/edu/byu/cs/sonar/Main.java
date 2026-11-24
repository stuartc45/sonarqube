package edu.byu.cs.sonar;

import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.NoSuchElementException;

/** Builds a sentence using three input files and a requested word count. */
// Console output and concise user-facing errors are intentional (lab steps 14 and 21).
@SuppressWarnings({"java:S106", "java:S1166"})
final class Main {
    private Main() {
        // Utility class: run through main.
    }

    /** Arguments: first file, second file, dictionary file, number of words. */
    public static void main(final String[] args) {
        if (args.length != 4) {
            System.err.println("Usage: Main <first-file> <second-file> <dictionary-file> <word-count>");
            return;
        }
        try {
            final CustomFileReader reader1 = new CustomFileReader(args[0]);
            final CustomFileReader reader2 = new CustomFileReader(args[1]);
            final CustomFileReader reader3 = new CustomFileReader(args[2]);
            final int numberOfWords = Integer.parseInt(args[3]);
            if (numberOfWords < 0) {
                throw new IllegalArgumentException("Word count must not be negative");
            }
            for (int i = 0; i < numberOfWords; i++) {
                final int howManyWords = reader1.howManyWordsInFile();
                final String wordFromIndex = reader2.returnThatWord(howManyWords);
                if (wordFromIndex.isEmpty()) {
                    throw new IllegalArgumentException("The first file must contain at least one word");
                }
                final String firstLetter = wordFromIndex.substring(0, 1).toLowerCase(Locale.ROOT);
                reader3.findNewWord(firstLetter);
            }
            reader1.setNewSentence(reader3.getNewSentence());
            System.out.println(reader1.getNewSentence());
        } catch (FileNotFoundException exception) {
            System.err.println("Did not find dictionary file. " + exception.getMessage());
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            System.err.println("Could not build sentence. " + exception.getMessage());
        }
    }
}
