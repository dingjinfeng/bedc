package com;


import org.junit.Test;

import acquire.base.utils.BytesUtils;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        //3939323831343338313031343330393338345045A541
        //3939323831343338313031343330393338345045EFBFBD41
        //3939323831343338313031343330393338345045C39141
        String string = "992814381014309384PEÑA";
        byte[] bs = string.getBytes();
        System.out.println(BytesUtils.bcdToString(bs));
        System.out.println(new String(bs));
    }

}