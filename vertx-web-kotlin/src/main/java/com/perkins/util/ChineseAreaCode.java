package com.perkins.util;

import java.io.UnsupportedEncodingException;

public class ChineseAreaCode {

    public class ToomuchWordException extends Exception{

        private static final long serialVersionUID = 1L;

        public ToomuchWordException(){}

        public ToomuchWordException(String message){
            super(message);
        }
    }
    /*
     * @function get the area code Chinese word
     * @param word
     * the single Chinese word to deal
     * @UnsupportedEncodingException
     * throws when word is not supported
     * @ToomuchWordException
     * throws when word is not a single word*/
    public String toAreaCode(String word) throws UnsupportedEncodingException,ToomuchWordException{
        if(word.length()!=1){
            /*word is not a single word*/
            throw new ToomuchWordException();
        }
        byte[] bs=word.getBytes("GB2312");
        String areaCode="";
        for(byte b:bs){
            int code=Integer.parseInt(Integer.toHexString(b & 0xff),16);
            areaCode += (code-0x80-0x20);       /*transfer the machine code to area code*/
        }
        return areaCode;
    }

    public static void main(String[] args) throws UnsupportedEncodingException, ToomuchWordException{
        String word="2";
        ChineseAreaCode cac = new ChineseAreaCode();
        System.out.println(cac.toAreaCode(word));
    }
}