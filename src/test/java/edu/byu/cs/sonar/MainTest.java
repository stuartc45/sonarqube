package edu.byu.cs.sonar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @TempDir
    Path directory;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errors = new ByteArrayOutputStream();
    private PrintStream originalOutput;
    private PrintStream originalErrors;
    private PrintStream capturedOutput;
    private PrintStream capturedErrors;

    @BeforeEach
    void captureConsole() {
        originalOutput = System.out;
        originalErrors = System.err;
        capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8);
        capturedErrors = new PrintStream(errors, true, StandardCharsets.UTF_8);
        System.setOut(capturedOutput);
        System.setErr(capturedErrors);
    }

    @AfterEach
    void restoreConsole() {
        System.setOut(originalOutput);
        System.setErr(originalErrors);
        capturedOutput.close();
        capturedErrors.close();
    }

    @Test
    void buildsTheRequestedSentence() {
        Main.main(arguments("5"));
        assertEquals("was was was was was " + System.lineSeparator(), output.toString(StandardCharsets.UTF_8));
        assertEquals("", errors.toString(StandardCharsets.UTF_8));
    }

    @Test
    void acceptsZeroWords() {
        Main.main(arguments("0"));
        assertEquals(System.lineSeparator(), output.toString(StandardCharsets.UTF_8));
        assertEquals("", errors.toString(StandardCharsets.UTF_8));
    }

    @Test
    void explainsIncorrectArgumentCount() {
        Main.main(new String[0]);
        assertTrue(errors.toString(StandardCharsets.UTF_8).startsWith("Usage: Main"));
    }

    @Test
    void explainsInvalidNumber() {
        Main.main(arguments("invalid"));
        assertTrue(errors.toString(StandardCharsets.UTF_8).startsWith("Could not build sentence."));
    }

    @Test
    void explainsNegativeNumber() {
        Main.main(arguments("-1"));
        assertTrue(errors.toString(StandardCharsets.UTF_8).contains("Word count must not be negative"));
    }

    @Test
    void explainsMissingFile() {
        String[] args = arguments("1");
        args[0] = directory.resolve("missing.txt").toString();
        Main.main(args);
        assertTrue(errors.toString(StandardCharsets.UTF_8).startsWith("Did not find dictionary file."));
    }

    @Test
    void explainsEmptyFirstFile() throws IOException {
        String[] args = arguments("1");
        args[0] = writeFile("empty.txt", "");
        Main.main(args);
        assertTrue(errors.toString(StandardCharsets.UTF_8).contains("must contain at least one word"));
    }

    @Test
    void explainsMissingMatchingWord() throws IOException {
        String[] args = arguments("1");
        args[2] = writeFile("unmatched.txt", "xyz");
        Main.main(args);
        assertTrue(errors.toString(StandardCharsets.UTF_8).contains("No word contains:"));
    }

    @Test
    void lowercasesIndependentlyOfSystemLocale() throws IOException {
        String[] args = {writeFile("first.txt", "one"), writeFile("second.txt", "I"),
                writeFile("third.txt", "iris"), "1"};
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Main.main(args);
        } finally {
            Locale.setDefault(originalLocale);
        }
        assertEquals("iris " + System.lineSeparator(), output.toString(StandardCharsets.UTF_8));
        assertEquals("", errors.toString(StandardCharsets.UTF_8));
    }

    private String writeFile(String name, String text) throws IOException {
        Path file = directory.resolve(name);
        Files.writeString(file, text, StandardCharsets.UTF_8);
        return file.toString();
    }

    private static String[] arguments(String wordCount) {
        return new String[]{"readMe1.txt", "readMe2.txt", "readMe3.txt", wordCount};
    }
}
