package com.unknown.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dat.DoubleLinkedArrayTrie;

public class LinkedMain {

	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader("small.dict"));
        String line;
        List<String> words = new ArrayList<String>();
        while ((line = reader.readLine()) != null)
        {
        	System.out.println(line);
            words.add(line);
        }
        reader.close();
 
        DoubleLinkedArrayTrie dat = new DoubleLinkedArrayTrie();
        System.out.println("是否错误: " + dat.build(words));
        System.out.println(dat);
        
        List<String> searchList = dat.commonPrefixSearchList("一举成和天下知万能胶还有一部分是万能");
        for(String str: searchList){
        	System.out.println(str);
        }
        
//        System.out.println("==============");
//        
//        List<Integer> indexList = dat.commonPrefixSearch("一举成和天下知还有一部分是万能");
//        for(Integer index: indexList){
//        	System.out.println(words.get(index));
//        }
	}

}
