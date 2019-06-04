package com.xinzhuxiansheng.common.utils;

import org.apache.commons.lang3.StringUtils;

public class StrUtil {

    public static String removePrefix(CharSequence str,CharSequence prefix){
        if(StringUtils.isBlank(str) || StringUtils.isBlank(prefix) ){
            return str==null?null:str.toString();
        }

        final String str2 = str.toString();
        if(str2.startsWith(prefix.toString())){
            return str2.substring(prefix.length(),str2.length());
        }
        return str2;
    }

    public static String nullToDefault(CharSequence str, String defaultStr) {
        return (str == null) ? defaultStr : str.toString();
    }

    /**
     * 如果给定字符串不是以suffix结尾的，在尾部补充suffix
     * @param str
     * @param suffix
     * @return
     */
    public static String addSuffixIfNot(CharSequence str, CharSequence suffix) {
        if(StringUtils.isBlank(str) || StringUtils.isBlank(suffix) ){
            return str==null?null:str.toString();
        }

        final String str2 = str.toString();
        final String suffix2 = suffix.toString();
        if (false == str2.endsWith(suffix2)) {
            return str2.concat(suffix2);
        }
        return str2;
    }

    /**
     * 截取分隔字符串之前的字符串，不包括分隔字符串<br>
     * 如果给定的字符串为空串（null或""）或者分隔字符串为null，返回原字符串<br>
     * 如果分隔字符串为空串""，则返回空串，如果分隔字符串未找到，返回原字符串，举例如下：
     *
     * <pre>
     * StrUtil.subBefore(null, *)      = null
     * StrUtil.subBefore("", *)        = ""
     * StrUtil.subBefore("abc", "a")   = ""
     * StrUtil.subBefore("abcba", "b") = "a"
     * StrUtil.subBefore("abc", "c")   = "ab"
     * StrUtil.subBefore("abc", "d")   = "abc"
     * StrUtil.subBefore("abc", "")    = ""
     * StrUtil.subBefore("abc", null)  = "abc"
     * </pre>
     *
     * @param string 被查找的字符串
     * @param separator 分隔字符串（不包括）
     * @param isLastSeparator 是否查找最后一个分隔字符串（多次出现分隔字符串时选取最后一个），true为选取最后一个
     * @return 切割后的字符串
     * @since 3.1.1
     */
    public static String subBefore(CharSequence string, CharSequence separator, boolean isLastSeparator) {
        if (StringUtils.isBlank(string) || separator == null) {
            return null == string ? null : string.toString();
        }

        final String str = string.toString();
        final String sep = separator.toString();
        if (sep.isEmpty()) {
            return "";
        }
        final int pos = isLastSeparator ? str.lastIndexOf(sep) : str.indexOf(sep);
        if (-1 == pos) {
            return str;
        }
        if(0 == pos) {
            return "";
        }
        return str.substring(0, pos);
    }
}
