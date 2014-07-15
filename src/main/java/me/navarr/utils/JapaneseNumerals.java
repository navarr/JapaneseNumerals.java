package me.navarr.utils;

import java.util.Hashtable;
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
 * <p/>
 * The main method used in this class is {@link JapaneseNumerals#to(String, String, int)}.  The last parameter of this
 * takes a flag.  The available flags are described below:
 * <p/>
 * {@link me.navarr.utils.JapaneseNumerals#FLAG_USE_FORMAL} - Returns formal symbols for 1, 2, 3, and 10.  These are
 * commonly used in the financial industry.
 * {@link me.navarr.utils.JapaneseNumerals#FLAG_USE_FORMAL_TEN_THOUSAND} - Returns the formal symbol for the 10,000
 * character.  This was used mainly in the financial sector in the past, but is no longer common.
 *
 * @author Navarr Barnier
 */
public class JapaneseNumerals {
    /**
     * Return formal symbols for 1, 2, 3, and 10 *
     */
    static final int FLAG_USE_FORMAL = 1;
    /**
     * Return formal symbol for 10,000
     */
    static final int FLAG_USE_FORMAL_TEN_THOUSAND = 2;
    /**
     * Return formal symbol for 10,000
     */
    static final int FLAG_USE_FORMAL_MAN = 2;

    /**
     * @param number
     * @return The number formatted in traditional Japanese characters
     * @see me.navarr.utils.JapaneseNumerals#to(String, String, int)
     */
    public static String to(Integer number) {
        return to(number.toString());
    }

    /**
     * @param number
     * @return The number formatted in traditional Japanese characters
     * @see me.navarr.utils.JapaneseNumerals#to(String, String, int)
     */
    public static String to(String number) {
        return to(number, 0);
    }

    /**
     * @param number
     * @param flags
     * @return The number formatted in traditional Japanese characters
     * @see me.navarr.utils.JapaneseNumerals#to(String, String, int)
     */
    public static String to(Integer number, int flags) {
        return to(number.toString(), flags);
    }

    /**
     * @param number
     * @param flags
     * @return The number formatted in traditional Japanese characters
     * @see me.navarr.utils.JapaneseNumerals#to(String, String, int)
     */
    public static String to(String number, int flags) {
        return to(number, null, flags);
    }

    /**
     * @param number
     * @return The number formatted in traditional Japanese characters
     * @see me.navarr.utils.JapaneseNumerals#to(String, String, int)
     */
    public static String to(Double number) {
        return to(number, 0);
    }

    /**
     * @param number
     * @param flags
     * @return The number formatted in traditional Japanese characters
     * @see me.navarr.utils.JapaneseNumerals#to(String, String, int)
     */
    public static String to(Double number, int flags) {
        Double right = number - number.intValue();
        return to(number.intValue(), right.toString().substring(2), flags);
    }

    /**
     * @param integral
     * @param decimal
     * @return The number formatted in traditional Japanese characters
     * @see me.navarr.utils.JapaneseNumerals#to(String, String, int)
     */
    public static String to(Integer integral, String decimal) {
        return to(integral.toString(), decimal);
    }

    /**
     * @param integral
     * @param decimal
     * @return The number formatted in traditional Japanese characters
     * @see me.navarr.utils.JapaneseNumerals#to(String, String, int)
     */
    public static String to(String integral, String decimal) {
        return to(integral, decimal, 0);
    }

    /**
     * @param integral
     * @param decimal
     * @param flags
     * @return The number formatted in traditional Japanese characters
     * @see me.navarr.utils.JapaneseNumerals#to(String, String, int)
     */
    public static String to(Integer integral, String decimal, int flags) {
        return to(integral.toString(), decimal, flags);
    }

    /**
     * @param integral The numbers to the left of the decimal point
     * @param decimal  The numbers to the right of the decimal point
     * @param flags    Convert certain numbers differently, please see {@link JapaneseNumerals} for more information.
     * @return The number formatted in traditional Japanese characters
     * @see me.navarr.utils.JapaneseNumerals
     */
    public static String to(String integral, String decimal, int flags) {
        String integralString = "";
        String decimalString = "";

        Hashtable<String, String> numbers = getNumbers(flags);
        String zero = getZero(flags);
        // Lookup table for myriad identifiers (万, 億, etc.)
        String[] myriadLookup = getMyriads(flags);

        if (decimal != null && !decimal.isEmpty()) {
            decimalString = decimal;
        }

        int loopAmount = 4 - (integral.length() % 4);
        for (int i = 0; i < loopAmount; ++i) {
            integral = "0" + integral;
        }

        /**
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

            /**
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
            Hashtable<String, String> decimalConvert = getNumbers(flags);

            Set<String> keySet = decimalConvert.keySet();
            String[] keys = new String[keySet.size()];
            keySet.toArray(keys);

            decimalString = decimalString.replace("0", getZero(flags));

            for (String key : keys) {
                decimalString = decimalString.replace(key, decimalConvert.get(key));
            }

            integralString = integralString + "・" + decimalString;
        }
        return integralString;
    }

    /**
     * @param flags Include {@link me.navarr.utils.JapaneseNumerals#FLAG_USE_FORMAL} to get formal characters for
     *              1, 2, 3, and 10
     * @return A lookup table from western numbers to japanese numbers.  Does not do myriad conversion
     */
    protected static Hashtable<String, String> getNumbers(int flags) {
        Hashtable<String, String> numbers = new Hashtable<String, String>();

        // This is blank instead of getZero as it is used for converting a western myriad string to Japanese.
        numbers.put("0", "");
        if (hasFlag(flags, FLAG_USE_FORMAL)) {
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
     * @param flags Include {@link me.navarr.utils.JapaneseNumerals#FLAG_USE_FORMAL} for "financial" zero ("rei")
     * @return Which character to use for Zero
     */
    protected static String getZero(int flags) {
        if (hasFlag(flags, FLAG_USE_FORMAL)) {
            return "零";
        }
        return "〇";
    }

    /**
     * Returns a lookup table of myriad identifiers from 10^4 to 10^68
     *
     * @param flags Include {@link me.navarr.utils.JapaneseNumerals#FLAG_USE_FORMAL_MAN} for older 10,000 character
     * @return A myriad lookup table.  Each index is the symbol for 10^(2*index)
     */
    protected static String[] getMyriads(int flags) {
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

        if (hasFlag(flags, FLAG_USE_FORMAL_MAN)) {
            myriads[1] = "萬";
        }

        return myriads;
    }

    /**
     * Private utility method for eliminating the boilerplate bitwise operator of flag checking
     *
     * @param flags Provided Flags
     * @param flag  Integer value of flag to search for
     * @return Whether or not the flags contain the flag
     */
    private static boolean hasFlag(int flags, int flag) {
        return ((flags & flag) == flag);
    }
}