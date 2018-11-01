package com.example.demozookeeper.demo;

import java.util.HashSet;
import java.util.Set;

public class JavaTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Set<String> name =new HashSet<String>();
		boolean added=false;
		added=name.add("LCF");
		System.out.println("added? "+added );
		added=name.add("hjb");
		System.out.println("added? "+added );
		added=name.add("LCf");
		System.out.println("added? "+added );
		added=name.add("LCF");
		System.out.println("added? "+added );
		System.out.println(name.toString());
		Set<String> name_set =new HashSet<String>();
		name_set.add("a");
		name_set.add("d");
		name_set.add("c");
		name_set.add("b");
		name_set.add("hjb");
		System.out.println(name_set.toString());
		
		name.addAll(name_set);
		System.out.println(name.toString());
		
	}

}
