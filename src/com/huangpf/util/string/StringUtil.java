package com.huangpf.util.string;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {
    private final static String[] EMPTY_ARRAY = new String[0];

    private static final String DEFAULT_SEPARATOR = ",";

    /**
     * 判断value是否为空
     * 
     * @param value
     * @return boolean
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }

    /**
     * 判断value是否为空
     * 
     * @param value
     * @return boolean
     */
    public static boolean isNonBlank(String value) {
        return !isBlank(value);
    }

    public static String[] split(String str, String sep) {
        return split(str, sep, false);
    }

    public static String[] split(String str, String sep, boolean needBlank) {
        int len = str.length();
        if (len == 0)
            return EMPTY_ARRAY;

        if (sep.length() == 1) {
            return split(str, sep.charAt(0), needBlank);
        }
        List<String> list = new ArrayList<String>();

        int idx = -1, lastIdx = 0, sepLen = sep.length();
        while ((idx = str.indexOf(sep, lastIdx)) >= 0) {
            if (needBlank || lastIdx != idx)
                list.add(str.substring(lastIdx, idx));
            lastIdx = idx + sepLen;
        }
        if (lastIdx != str.length())
            list.add(str.substring(lastIdx));
        return list.toArray(EMPTY_ARRAY);
    }

    public static String[] split(String str, char ch) {
        return split(str, ch, false);
    }

    public static String[] split(String str, char ch, boolean needBlank) {
        int len = str.length();
        if (len == 0)
            return EMPTY_ARRAY;

        List<String> list = new ArrayList<String>();

        int lastIdx = 0;
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c == ch) {
                if (needBlank || lastIdx != i) {
                    list.add(str.substring(lastIdx, i));
                }
                lastIdx = i + 1;
            }
        }
        if (lastIdx != len)
            list.add(str.substring(lastIdx));

        return list.toArray(EMPTY_ARRAY);
    }

    public static String replaceOnce(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, 1);
    }

    public static String replace(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, -1);
    }

    public static String replace(String text, String searchString, String replacement, int max) {
        if (isBlank(text) || (isBlank(searchString)) || (replacement == null) || (max == 0)) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(searchString, start);
        if (end == -1) {
            return text;
        }
        int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = (increase < 0) ? 0 : increase;
        increase *= ((max > 64) ? 64 : (max < 0) ? 16 : max);
        StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != -1) {
            buf.append(text.substring(start, end)).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = text.indexOf(searchString, start);
        }
        buf.append(text.substring(start));
        return buf.toString();
    }

    /**
     * arr中是否含有对应key的键值对
     * 
     * @param arr
     * @param key
     * @return
     */

    public static boolean contains(String[] arr, String key) {
        if (arr == null || arr.length == 0)
            return false;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * arr中是否含有对应key的键值对
     * 
     * @param arr
     * @param key
     * @return
     */

    public static boolean contains(String searchStr, String subStr) {
        return contains(searchStr, subStr, DEFAULT_SEPARATOR);
    }

    /**
     * 检查字符串中是否含有对应key的键值对
     * 
     * @param arr
     * @param key
     * @return
     */

    public static boolean contains(String searchStr, String subStr, String separator) {
        searchStr = separator + searchStr + separator;
        subStr = separator + subStr + separator;
        return searchStr.indexOf(subStr) >= 0;
    }

    /**
     * 根据字节长度获取子字符串
     * 
     * @param srcStr
     * @param count
     * @return
     */
    public static String getByteSubString(String srcStr, int count) {
        if (srcStr == null)
            return "";
        if (count < 0)
            return "";

        if (count > srcStr.length() * 2)
            return srcStr;

        char[] cs = srcStr.toCharArray();

        int c = 0, endPos = -1;
        for (int i = 0; i < cs.length; i++) {
            ++c;
            if (cs[i] > 255) {
                ++c;
            }
            if (c == count) {
                endPos = i + 1;
                break;
            } else if (c > count) {
                endPos = i;
                break;
            }
        }
        if (endPos == -1) {
            return srcStr;
        }

        return new String(cs, 0, endPos);
    }

}
