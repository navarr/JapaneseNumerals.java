package me.navarr.JapaneseNumerals;

import me.navarr.utils.JapaneseNumerals;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link me.navarr.utils.JapaneseNumerals}
 *
 * @author me@navarr.me (Navarr Barnier)
 */
public class JapaneseNumeralsTest {
    @Test
    public void testBaseNumbers() {
        String[] western = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "100", "1000"};
        String[] japanese = new String[]{"〇", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "百", "千"};

        for (int i = 0, len = western.length; i < len; ++i) {
            String result = JapaneseNumerals.to(western[i], null, false, false);
            assertEquals(western[i] + " must be " + japanese[i], japanese[i], result);
        }
    }

    @Test
    public void testFormalNumbers() {
        String[] western = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String[] japanese = new String[]{"零", "壱", "弐", "参", "四", "五", "六", "七", "八", "九", "拾", "百", "千"};

        for (int i = 0, len = western.length; i < len; ++i) {
            String result = JapaneseNumerals.to(western[i], null, true, false);
            assertEquals(western[i] + " must be " + japanese[i], japanese[i], result);
        }
    }

    @Test
    public void testDecimals() {
        String westernDecimal = "0123456789";
        String japanese = "〇・〇一二三四五六七八九";

        assertEquals("0.0123456789" + " must be " + japanese, japanese, JapaneseNumerals.to("0", westernDecimal, false, false));
    }

    @Test
    public void testNumbersandDecimals() {
        assertEquals("55.55" + " must be " + "五十五・五五", "五十五・五五", JapaneseNumerals.to("55", "55", false, false));
    }
}
