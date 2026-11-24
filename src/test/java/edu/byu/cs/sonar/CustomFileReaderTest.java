package edu.byu.cs.sonar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CustomFileReaderTest {
    @TempDir
    Path directory;
    private CustomFileReader sut;

    @BeforeEach
    void setUp() {
        sut = new CustomFileReader("readMe1.txt");
    }

    @Test
    void countsWordsWithoutAccumulatingAcrossCalls() throws FileNotFoundException {
        assertEquals(4, sut.howManyWordsInFile());
        assertEquals(4, sut.howManyWordsInFile());
    }

    @Test
    void readsWordsFromTheBeginningOnEveryCall() throws FileNotFoundException {
        assertEquals("I", sut.returnThatWord(1));
        assertEquals("Science!", sut.returnThatWord(4));
        assertEquals("I", sut.returnThatWord(1));
        assertEquals("", sut.returnThatWord(0));
    }

    @Test
    void rejectsInvalidWordIndexes() {
        assertThrows(IllegalArgumentException.class, () -> sut.returnThatWord(-1));
        assertThrows(NoSuchElementException.class, () -> sut.returnThatWord(5));
    }

    @Test
    void appendsMatchingWordsAndReopensFile() throws FileNotFoundException {
        sut.findNewWord("C");
        sut.findNewWord("I");
        assertEquals("Computer I ", sut.getNewSentence());
    }

    @Test
    void reportsAnUnmatchedSearchWithoutChangingSentence() {
        assertThrows(NoSuchElementException.class, () -> sut.findNewWord("missing"));
        assertEquals("", sut.getNewSentence());
    }

    @Test
    void getsAndSetsSentence() {
        assertEquals("", sut.getNewSentence());
        sut.setNewSentence("New Sentence.");
        assertEquals("New Sentence.", sut.getNewSentence());
    }

    @Test
    void rejectsNullInputs() {
        assertThrows(NullPointerException.class, () -> new CustomFileReader(null));
        assertThrows(NullPointerException.class, () -> sut.setNewSentence(null));
        assertThrows(NullPointerException.class, () -> sut.findNewWord(null));
    }

    @Test
    void propagatesMissingFilesFromEveryReadOperation() {
        CustomFileReader missing = new CustomFileReader(directory.resolve("missing.txt").toString());
        assertThrows(FileNotFoundException.class, missing::howManyWordsInFile);
        assertThrows(FileNotFoundException.class, () -> missing.returnThatWord(1));
        assertThrows(FileNotFoundException.class, () -> missing.findNewWord("x"));
    }

    @Test
    void handlesEmptyFiles() throws IOException {
        CustomFileReader empty = createReader("empty.txt", "");
        assertEquals(0, empty.howManyWordsInFile());
        assertThrows(NoSuchElementException.class, () -> empty.returnThatWord(1));
        assertThrows(NoSuchElementException.class, () -> empty.findNewWord("x"));
    }

    @Test
    void readsUtf8AndWhitespace() throws IOException {
        CustomFileReader unicode = createReader("unicode.txt", "café\tnaïve\n東京");
        assertEquals(3, unicode.howManyWordsInFile());
        assertEquals("naïve", unicode.returnThatWord(2));
        unicode.findNewWord("東");
        assertEquals("東京 ", unicode.getNewSentence());
    }

    @Test
    void closesFilesBetweenOperations() throws IOException {
        Path file = directory.resolve("replaceable.txt");
        Files.writeString(file, "first", StandardCharsets.UTF_8);
        CustomFileReader reader = new CustomFileReader(file.toString());
        assertEquals(1, reader.howManyWordsInFile());
        assertEquals("first", reader.returnThatWord(1));
        reader.findNewWord("f");
        Files.delete(file);
        Files.writeString(file, "second third", StandardCharsets.UTF_8);
        assertEquals(2, reader.howManyWordsInFile());
        assertEquals("second", reader.returnThatWord(1));
    }

    @Test
    void equalityIsSymmetricAndTransitive() {
        CustomFileReader same = new CustomFileReader("readMe1.txt");
        CustomFileReader third = new CustomFileReader("readMe1.txt");
        assertEquals(sut, same);
        assertEquals(same, sut);
        assertEquals(same, third);
        assertEquals(sut, third);
        assertEquals(sut.hashCode(), same.hashCode());
    }

    @ParameterizedTest
    @MethodSource("equalityEdgeCases")
    void checksEqualityEdgeCases(CustomFileReader first, Object second, boolean expected) {
        assertEquals(expected, first.equals(second));
    }

    private static Stream<Arguments> equalityEdgeCases() {
        CustomFileReader reader = new CustomFileReader("readMe1.txt");
        return Stream.of(
                Arguments.of(reader, reader, true),
                Arguments.of(reader, null, false),
                Arguments.of(reader, "readMe1.txt", false));
    }

    @Test
    void equalityIncludesPathCountAndSentence() throws FileNotFoundException {
        CustomFileReader differentPath = new CustomFileReader("readMe2.txt");
        CustomFileReader differentCount = new CustomFileReader("readMe1.txt");
        CustomFileReader differentSentence = new CustomFileReader("readMe1.txt");
        differentCount.howManyWordsInFile();
        differentSentence.setNewSentence("Different");
        assertNotEquals(sut, differentPath);
        assertNotEquals(sut, differentCount);
        assertNotEquals(sut, differentSentence);
    }

    @Test
    void hashCodeAcceptsOrdinarySentences() {
        sut.setNewSentence("Words are not integers");
        CustomFileReader same = new CustomFileReader("readMe1.txt");
        same.setNewSentence("Words are not integers");
        assertEquals(sut.hashCode(), same.hashCode());
    }

    @Test
    void toStringIncludesSentenceAndCount() throws FileNotFoundException {
        sut.howManyWordsInFile();
        sut.setNewSentence("Example");
        assertEquals("Example 4", sut.toString());
    }

    private CustomFileReader createReader(String fileName, String text) throws IOException {
        Path file = directory.resolve(fileName);
        Files.writeString(file, text, StandardCharsets.UTF_8);
        return new CustomFileReader(file.toString());
    }
}
