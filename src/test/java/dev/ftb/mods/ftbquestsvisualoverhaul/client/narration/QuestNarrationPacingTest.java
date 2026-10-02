package dev.ftb.mods.ftbquestsvisualoverhaul.client.narration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestNarrationPacingTest {

    @Test
    void revealsNothingAtZeroElapsed() {
        assertEquals(0, QuestNarrationPacing.revealedCharacterCount("Hello", 0L));
    }

    @Test
    void revealsAtFiftyCharactersPerSecond() {
        // 20ms per character at 50 CPS; 4 chars need [60ms, 80ms) of budget.
        assertEquals(3, QuestNarrationPacing.revealedCharacterCount("Hello", 60L));
        assertEquals(4, QuestNarrationPacing.revealedCharacterCount("Hello", 80L));
    }

    @Test
    void revealsFullTextOnceElapsedCoversIt() {
        assertEquals(5, QuestNarrationPacing.revealedCharacterCount("Hello", 100L));
        assertEquals(5, QuestNarrationPacing.revealedCharacterCount("Hello", 10_000L));
    }

    @Test
    void appliesCommaPauseAfterComma() {
        // 'a' and ',' each cost the 20ms base delay; 'b' additionally waits out the
        // 300ms comma pause on top of its own 20ms base delay: 20+20+(20+300)=360ms.
        assertEquals(2, QuestNarrationPacing.revealedCharacterCount("a,b", 359L));
        assertEquals(3, QuestNarrationPacing.revealedCharacterCount("a,b", 360L));
    }

    @Test
    void appliesPeriodPauseAfterPeriod() {
        assertEquals(2, QuestNarrationPacing.revealedCharacterCount("a.b", 659L));
        assertEquals(3, QuestNarrationPacing.revealedCharacterCount("a.b", 660L));
    }

    @Test
    void appliesExclamationPauseAfterExclamation() {
        assertEquals(2, QuestNarrationPacing.revealedCharacterCount("a!b", 559L));
        assertEquals(3, QuestNarrationPacing.revealedCharacterCount("a!b", 560L));
    }

    @Test
    void appliesQuestionPauseAfterQuestionMark() {
        assertEquals(2, QuestNarrationPacing.revealedCharacterCount("a?b", 659L));
        assertEquals(3, QuestNarrationPacing.revealedCharacterCount("a?b", 660L));
    }

    @Test
    void handlesNullAndEmptyText() {
        assertEquals(0, QuestNarrationPacing.revealedCharacterCount(null, 1000L));
        assertEquals(0, QuestNarrationPacing.revealedCharacterCount("", 1000L));
    }

    @Test
    void codePointWithinPrefixAllowsAsciiUpToBudget() {
        assertTrue(QuestNarrationPacing.codePointWithinPrefix(0, 'a', 1));
        assertFalse(QuestNarrationPacing.codePointWithinPrefix(1, 'b', 1));
    }

    @Test
    void codePointWithinPrefixRefusesToSplitASurrogatePair() {
        int emoji = 0x1F600; // U+1F600, charCount == 2 in UTF-16.
        assertFalse(QuestNarrationPacing.codePointWithinPrefix(0, emoji, 1));
        assertTrue(QuestNarrationPacing.codePointWithinPrefix(0, emoji, 2));
    }
}
