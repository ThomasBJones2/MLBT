//package com.google.commons.tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

import cc.mallet.classify.*;
import cc.mallet.util.*;
import cc.mallet.types.*;

/**
 * Author: Gregor Zeitlinger <gregor@zeitlinger.de> Date: 08.09.2011
 */
public class Tree {

        private final Tree[] children;

        private final String value;
	
	private final String title;

	private final Classifier LocalBayes;

        Tree (final String value, final Classifier LocalBayes, final String title,
                        final Tree[] children) {
                this.value = value;
		this.LocalBayes = LocalBayes;
		this.children = children;
		this.title = title;
        }

        public Tree[] getChildren() {
                return children;
        }

        public String getValue() {
                return value;
        }

	public String getTitle(){
		return title;
	}

	public Classifier getClassifier(){
		return LocalBayes;
	}

	Tree (Tree T){
		this.children = T.getChildren();
		this.value = T.getValue();
		this.title = T.getTitle();
		this.LocalBayes = T.getClassifier();
	}


	public void print(int depth){
		System.out.print("Depth: " + depth + "\n");
		System.out.print("Value: " + this.value + "\n");
		System.out.print("Title: " + this.title + "\n\n");
		Tree[] lchildren = this.getChildren();
		for(int i = 0; i < lchildren.length; i++)
			lchildren[i].print(depth + 1);
	}
}
