package net.c_kogyo.returnvisitorv5.util;

/**
 * Created by SeijiShii on 2017/06/20.
 */

public class StringUtil {

    public static String replaceDoubleQuotes(String s) {

        StringBuilder builder = new StringBuilder();
        for ( int i = 0 ; i < s.length() ; i++ ) {
            if (String.valueOf(s.charAt(i)).equals("\"")) {
                if ( i == 0 ) {
                    // 最初の文字ならバックスラッシュをその前に挿入
                    builder.append("\\").append(s.charAt(i));

                } else if (String.valueOf(s.charAt(i - 1)).equals("\\")) {
                    // 直前の文字がバックスラッシュならそのまま追加
                    builder.append(s.charAt(i));
                } else {
                    builder.append("\\").append(s.charAt(i));
                }
            } else {
                builder.append(s.charAt(i));
            }
        }
        return builder.toString();
    }

}
