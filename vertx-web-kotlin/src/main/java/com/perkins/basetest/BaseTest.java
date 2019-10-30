package com.perkins.basetest;

import com.perkins.util.Base64Utils;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class BaseTest {
    @Test
    public void testASCll() {
        byte[] array = "你".getBytes();
        showData(array);
        System.out.println("=====================");
        byte[] copyArray = array.clone();
        for (int i = 0; i < copyArray.length; i++) {
            byte temp = copyArray[i];
            copyArray[i] = (byte) (~temp);
        }
        showData(copyArray);
        System.out.println("=====================");
        byte[] resultArray = copyArray.clone();
        for (int i = 0; i < resultArray.length; i++) {
            byte temp = resultArray[i];
            resultArray[i] = (byte) (~temp);
        }
        showData(resultArray);

    }

    private void showData(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            byte item = array[i];
            String mbyteToString = Integer.toBinaryString((item & 0xFF) + 0x100).substring(1);
            String str2 = Integer.toBinaryString(item);
            System.out.println(item + "--->" + mbyteToString + "-->" + str2);
        }
        try {
            System.out.println(new String(array, "UTF-8"));
            String str = new Base64().encodeToString(array);
            System.out.println(str);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void testBs() throws UnsupportedEncodingException {
        String a = Base64Utils.encode("aa".getBytes("UTF-8"));
        System.out.println(a);

        final String text = "字串文字,22aS";
        final byte[] textByte = text.getBytes("UTF-8");
        final String encodedText = new Base64().encodeToString(textByte);
        System.out.println(encodedText);
        System.out.println(new String(new Base64().decode(encodedText)));
    }


    @Test
    public void testDemo1() {

        String str = "Base64算法最早是为了解决电子邮件传输的问题的，早先的邮件传输协议中只支持ASCII码传递，如果要传输二进制文件，如图片和视频，是无法传输的，而BASE64可以将二进制文件内容编码成为只包含ASCII码的内容，这样就可以传输了。\n" +
                "  Base64算法大家常常说成是加密算法，但准确的来说，Base64不是一种加密算法，只能算是一种基于64个字符的编码算法。\n" +
                "\n" +
                "  它有一个字符映射表，每个字符映射了一个十进制编码，共映射了64个字符。Base64将给定的数据经二进制转换后与字符映射表相对应，得到所谓的密文；映射表如下，映射表的最后是一个等号，是作为补位符用来补位的。";
        Base64 base64 = new Base64();
        String temp = base64.encodeToString(str.getBytes());
        System.out.println("正常base64编码数据:" + temp);
        System.out.println("正常base64解码:" + new String(base64.decode(temp)));
        StringBuffer sb = getEncrityBase64(str, base64);
        System.out.println("加密后数据:" + sb.toString());
        System.out.println("异常base64解码:" + new String(base64.decode(sb.toString())));
        StringBuffer qurySb = getEncrityBase64("进制编码", base64);
        System.out.println("搜索的数据:" + qurySb.toString());
    }

    @NotNull
    private StringBuffer getEncrityBase64(String str, Base64 base64) {
        char[] chars = str.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            byte[] tem = (chars[i] + "").getBytes();
            inverse(tem);
            String bs = base64.encodeToString(tem);
            sb.append(bs);
        }
        return sb;
    }

    private void inverse(byte[] tem) {
        for (int j = 0; j < tem.length; j++) {
            byte tempA = tem[j];
            tem[j] = (byte) (~tempA);
        }
    }
}
