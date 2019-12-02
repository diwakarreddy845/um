package com.capv.um.util;

import java.util.Comparator;

import com.capv.um.chat.model.OfMessageArchive;
import com.capv.um.model.OfGroupArchive;

public class CustomGroupComparator implements Comparator<OfGroupArchive>{

	   /* @Override
	    public int compare(ChatHitory o1, Person o2) {
	        if (o1.getAge() < o2.getAge()){
	            return 1;
	        }else{
	            return 0;
	        }
	    }*/

		@Override
		public int compare(OfGroupArchive o1, OfGroupArchive o2) {
			return Long.compare(o1.getSentDate(),o2.getSentDate());
		}
	}
