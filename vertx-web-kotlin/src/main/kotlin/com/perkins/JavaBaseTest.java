package com.perkins;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JavaBaseTest {
    @Test
    public void testSubList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new Integer(i));
        }
        list.sort((a,b )-> b -a);
        list
//                .subList(0, 0)
                .forEach(i -> System.out.println(i));
//        list.subList(0,0).forEach(i -> System.out.println(i));
    }
}
