package com.wdjhzw.contacts.utils;

import java.util.Locale;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class HanziToPinyin {
    private final String HANZI_TO_PINYIN_TABLE = "Unicode_Hanzi_to_Pinyin";

    private volatile static HanziToPinyin mInstance;

    private SQLiteDatabase mPinyinDB;

    private HanziToPinyin() {
        mPinyinDB = AssetsDatabaseManager.getInstance()
                .getDatabase("pinyin.db");
    }

    public static HanziToPinyin getInstance() {
        if (mInstance == null) {
            synchronized (HanziToPinyin.class) {
                if (mInstance == null) {
                    mInstance = new HanziToPinyin();
                }
            }
        }
        return mInstance;
    }

    /**
     * 转换包含汉字的字符串，汉字转换为拼音，非汉字则保留原字符
     * 
     * @param hanzi
     *            将要转换的包含汉字的字符串
     * @param keepHanzi
     *            true表示在转换后的每个拼音之后追加原汉字，用空格做分隔；false则不进行追加
     * @return 转换后的包含拼音的字符串
     */
    public String getPinyinFromHanzi(String hanzi, boolean keepHanzi) {
        if (hanzi == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < hanzi.length(); i++) {
            char ch = hanzi.charAt(i);// 取出每个字符进行处理

            if (String.valueOf(ch).matches("[\\u4E00-\\u9FA5]")) {
                String pinyin = null;
                if (i == 0) {
                    pinyin = polyphoneSurname(ch);
                } else {
                    pinyin = queryPinyin(ch);
                }

                dividedBySeparator(builder, ch);
                builder.append(pinyin).append(' ');
                if (keepHanzi) {
                    builder.append(ch).append(' ');
                }
            } else if (String.valueOf(ch).matches("[a-zA-Z]")) {
                builder.append(ch);
            } else {
                dividedBySeparator(builder, ch);
                // 为非汉字、非字母开头的名字加一个“#”前缀
                builder.append(i == 0 ? "# " : "");
                // 如果该字符是空格，则略过，避免出现连续两个空格
                builder.append((ch == ' ' ? "" : ch)).append(' ');
            }
        }

        return builder.toString().trim();
    }

    /**
     * 为特殊情况做空格分隔处理
     * <p>
     * 用于间隔该字符和之前的字母，如“Liの”->“Li の”或“Li明”->“Li Ming”这种情况
     * 
     * @param builder
     *            要被处理的字符串
     * @param index
     *            将要追加到字符串尾部的字符
     */
    private void dividedBySeparator(StringBuilder builder, char ch) {
        int index = builder.length();
        if (index > 0
                && ch != ' '
                && String.valueOf(builder.charAt(index - 1))
                        .matches("[a-zA-Z]")) {
            builder.append(' ');
        }
    }

    /**
     * 从数据库中查找汉字字符对应的拼音
     * 
     * @param ch
     * @return
     */
    private String queryPinyin(char ch) {
        String pinyin = null;

        mPinyinDB.beginTransaction();
        try {
            Cursor c = mPinyinDB.query(
                    HANZI_TO_PINYIN_TABLE,
                    new String[] { "pinyin" },
                    "hanzi=?",
                    new String[] { Integer.toHexString(ch).toUpperCase(
                            Locale.ENGLISH) }, null, null, null);

            if (c.moveToNext()) {
                pinyin = c.getString(c.getColumnIndex("pinyin"));
                c.close();
            }

            mPinyinDB.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mPinyinDB.endTransaction();
        }

        return pinyin;
    }

    /**
     * 多音字姓氏处理
     * 
     * @param surname
     * @return
     */
    private String polyphoneSurname(char surname) {
        switch (surname) {
        case '秘':
            return "Bi";

        case '重':
            return "Chong";

        case '区':
            return "Ou";
            
        case '朴':
            return "Piao";

        case '覃':
            return "Qin";
            
        case '仇':
            return "Qiu";

        case '单':
            return "Shan";

        case '折':
            return "She";

        case '洗':
            return "Xian";

        case '解':
            return "Xie";

        case '尉':
            return "Yu";

        case '曾':
            return "Zeng";

        case '查':
            return "Zha";

        case '翟':
            return "Zhai";

        default:
            return queryPinyin(surname);
        }
    }
}
