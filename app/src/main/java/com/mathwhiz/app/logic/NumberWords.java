package com.mathwhiz.app.logic;

/** English number words 0-100, used for number-recognition answers ("7" -> "seven"). */
public final class NumberWords {
    private static final String[] ONES = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight",
            "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen",
            "eighteen", "nineteen"};
    private static final String[] TENS = {"", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy",
            "eighty", "ninety"};

    private NumberWords() {}

    public static String of(int n) {
        if (n < 0 || n > 100) return String.valueOf(n);
        if (n == 100) return "one hundred";
        if (n < 20) return ONES[n];
        int t = n / 10, o = n % 10;
        return o == 0 ? TENS[t] : TENS[t] + "-" + ONES[o];
    }
}
