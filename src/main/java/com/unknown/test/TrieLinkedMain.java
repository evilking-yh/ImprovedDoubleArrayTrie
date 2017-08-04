package com.unknown.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dat.DoubleLinkedTrie;

public class TrieLinkedMain {

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
 
        DoubleLinkedTrie dat = new DoubleLinkedTrie();
        System.out.println("是否错误: " + dat.build(words));
        System.out.println(dat);
        
        int index = dat.exactMatchSearch("一举成名天下知");
        System.out.println(index);
        if(index >= 0){
        	System.out.println("========");
        }
        
        List<String> result = dat.commonPrefixSearchList("一举成名天下知还有万能啊");
        for(String str: result){
        	System.out.println(str);
        }
        
        
	}

}
