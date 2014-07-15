package me.navarr.utils;

import com.sun.istack.internal.Nullable;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * JapaneseNumerals is a utility class used for converting what are popularly called 'arabic numerals' to traditional
 * Japanese characters for denoting numbers.  As arabic numerals have become increasingly popular in Japan, the
 * purposes of this class are limited, but it's nice to have around.
 * <p/>
 * That said, larger numbers may not be recognized by many people.  Numbers 10^52 and up may not be precise and come
 * from editions of the Jinkouki between 1627 and 1634.  There were several errors and variations in these old texts,
 * and as far as I'm aware they have not been corrected since.
 * <p/>
 * The source of this information is the wikipedia page on Japanese numerals.
 *
 * @author Navarr Barnier
 */
public final class JapaneseNumerals {

    private JapaneseNumerals() {
        // Do not allow class construction
    }

    /**
     * @param integral The numbers to the left of the decimal point
     * @param decimal  The numbers to the right of the decimal point
     * @param useFormalNumbers Use formal symbols for 0, 1, 2, 3, and 10
     * @param useFormalMan Use formal symbol for 10,000
     * @return The number formatted in traditional Japanese characters
     * @see me.navarr.utils.JapaneseNumerals
     */
    public static String to(String integral, @Nullable String decimal, boolean useFormalNumbers, boolean useFormalMan) {
        String integralString = "";
        String decimalString = "";

        Map<String, String> numbers = getNumbers(useFormalNumbers);
        String zero = getZero(useFormalNumbers);
        // Lookup table for myriad identifiers (万, 億, etc.)
        String[] myriadLookup = getMyriads(useFormalMan);

        if (decimal != null && !decimal.isEmpty()) {
            decimalString = decimal;
        }

        int loopAmount = 4 - (integral.length() % 4);
        for (int i = 0; i < loopAmount; ++i) {
            integral = "0" + integral;
        }

        /*
         * Japanese numbers are divided into sections coined "myriads."  These are a lot like commas in US-formatted
         * numbers (1,000,000) except there are four numbers in each group (1,0000,0000).  Each group can be read as an
         * independent number followed by the myriad "name" (excluding the lowest myriad, which does not have one).
         *
         * Much in the same way that 198,000 is "one hundred ninety-eight [thousand]"; 198,0000 (百九十八万) is "one
         * hundred ninety-eight man (10,000)"
         */

        int i = 4; // +4 each loop to jump myriads
        int myriad = 0;
        String[] myriadValues = new String[(integral.length() / 4)];
        while (i <= integral.length()) {
            int start = integral.length() - i;
            if (start < 0) {
                start = 0;
            }
            myriadValues[myriad] = integral.substring(start, start + 4);
            ++myriad;
            i += 4;
        }

        myriad = 0;
        for (String v : myriadValues) {
            // v is the myriadValue.  Left shortened for the sanity of the reader.
            if (v.equals("0000")) {
                ++myriad;
                continue;
            }

            /*
             * myriadString contains the temporary string for this individual myriad.  We then check each place and
             * move up.
             *
             * Each myriad contains 4 western digits, for simplicity we will refer to these as if they were a single
             * number.  As such, we have a ones, tens, hundreds, and thousands place.  Each place, containing a number,
             * is followed by a symbol denoting it's place name - excluding ones.
             *
             * 十 - Tens
             * 百 - Hundreds
             * 千 - Thousands
             *
             * As such, 5555 would be 五千五百五十五, where 五 is 5 (1 is a special case, noted below)
             *
             * This would then be followed by the myriad indicator (万, 億, etc.)
             *
             * You will note that each place has an if statement testing if the number is one, that's because one is
             * excluded from prefixing these three internal myriad symbols.
             */

            // Ones
            String myriadString = numbers.get(v.substring(v.length() - 1, v.length()));

            // Tens
            if (v.substring(v.length() - 2, v.length() - 1).equals("1")) {
                // if "xx1x"
                myriadString = numbers.get("10") + myriadString;
            } else if (!v.substring(v.length() - 2, v.length() - 1).equals("0")) {
                // else if not "xx0x"
                myriadString = numbers.get(v.substring(v.length() - 2, v.length() - 1)) + numbers.get("10") + myriadString;
            }

            if (v.substring(v.length() - 3, v.length() - 2).equals("1")) {
                // if "x1xx"
                myriadString = numbers.get("100") + myriadString;
            } else if (!v.substring(v.length() - 3, v.length() - 2).equals("0")) {
                // else if not "x0xx"
                myriadString = numbers.get(v.substring(v.length() - 3, v.length() - 2)) + numbers.get("100") + myriadString;
            }

            if (v.substring(v.length() - 4, v.length() - 3).equals("1")) {
                // if "1xxx"
                myriadString = numbers.get("1000") + myriadString;
            } else if (!v.substring(v.length() - 4, v.length() - 3).equals("0")) {
                // else if not "0xxx"
                myriadString = numbers.get(v.substring(v.length() - 4, v.length() - 3)) + numbers.get("1000") + myriadString;
            }

            // Add the myriad symbol and continue to the next myriad
            integralString = myriadString + myriadLookup[myriad] + integralString;
            ++myriad;
        }

        if (integralString.equals("")) {
            integralString = integralString + zero;
        }

        /**
         * Decimals are nice and simple.
         *
         * You just replace the number with it's Japanese ones-place equivalent.
         */
        if (!decimalString.equals("")) {

            Set<String> keySet = numbers.keySet();
            String[] keys = new String[keySet.size()];
            keySet.toArray(keys);

            decimalString = decimalString.replace("0", zero);

            for (String key : keys) {
                decimalString = decimalString.replace(key, numbers.get(key));
            }

            integralString = integralString + "・" + decimalString;
        }
        return integralString;
    }

    /**
     * @param formal Use formal characters for  1, 2, 3, and 10
     * @return A lookup table from western numbers to japanese numbers.  Does not do myriad conversion
     */
    protected static Map<String, String> getNumbers(boolean formal) {
        Map<String, String> numbers = new Hashtable<String, String>();

        // This is blank instead of getZero as it is used for converting a western myriad string to Japanese.
        numbers.put("0", "");
        if (formal) {
            numbers.put("1", "壱");
            numbers.put("2", "弐");
            numbers.put("3", "参");
            numbers.put("10", "拾");
        } else {
            numbers.put("1", "一");
            numbers.put("2", "二");
            numbers.put("3", "三");
            numbers.put("10", "十");
        }
        numbers.put("4", "四");
        numbers.put("5", "五");
        numbers.put("6", "六");
        numbers.put("7", "七");
        numbers.put("8", "八");
        numbers.put("9", "九");
        numbers.put("100", "百");
        numbers.put("1000", "千");
        return numbers;
    }

    /**
     * @param formal Use the "rei" (零) symbol instead of "zero" (〇)
     * @return Which character to use for Zero
     */
    protected static String getZero(boolean formal) {
        if (formal) {
            return "零";
        }
        return "〇";
    }

    /**
     * Returns a lookup table of myriad identifiers from 10^4 to 10^68
     *
     * @param formal Use a more formal version of the 10,000 chracter.
     * @return A myriad lookup table.  Each index is the symbol for 10^(2*index)
     */
    protected static String[] getMyriads(boolean formal) {
        String[] myriads = new String[18];
        myriads[0] = ""; // 10^0 - Default Myriad has no symbol
        myriads[1] = "万"; // 10^4
        myriads[2] = "億"; // 10^8
        myriads[3] = "兆"; // 10^12
        myriads[4] = "京"; // 10^16
        myriads[5] = "垓"; // 10^20
        myriads[6] = "秭"; // 10^24
        myriads[7] = "穣"; // 10^28
        myriads[8] = "溝"; // 10^32
        myriads[9] = "澗"; // 10^36
        myriads[10] = "正"; // 10^40
        myriads[11] = "載"; // 10^44
        myriads[12] = "極"; // 10^48
        myriads[13] = "恒河沙"; // 10^52
        myriads[14] = "阿僧祇"; // 10^56
        myriads[15] = "那由他"; // 10^60
        myriads[16] = "不可思議"; // 10^64
        myriads[17] = "無量大数"; // 10^68 (as high as myriads currently go?)

        if (formal) {
            myriads[1] = "萬";
        }

        return myriads;
    }
}