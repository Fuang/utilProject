package com.huangpf.util;

public class TestUtil {

    public static void main(String[] args) {
        /*
         * IData data = new DataMap("{\"NAME\":\"huangpf\";\"AGE\":\"27\"}");
         * System.out.println(data.getInt("AGE"));
         */

        String test = "'asdf','qwer',";
        System.out.println(test.substring(0, test.length() - 1));

    }
}
