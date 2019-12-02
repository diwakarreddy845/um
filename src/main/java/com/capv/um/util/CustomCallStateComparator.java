package com.capv.um.util;

import java.util.Comparator;
import java.util.Date;

import com.capv.um.model.ChatHistory;
import com.capv.um.model.UserCallState;

public class CustomCallStateComparator implements Comparator<UserCallState>{

   /* @Override
    public int compare(ChatHitory o1, Person o2) {
        if (o1.getAge() < o2.getAge()){
            return 1;
        }else{
            return 0;
        }
    }*/

	@Override
	public int compare(UserCallState o1, UserCallState o2) {
		//return Date.compare(o1.getStartTime(),o2.getStartTime());
		return o1.getStartTime().compareTo(o2.getStartTime());
	}
}
