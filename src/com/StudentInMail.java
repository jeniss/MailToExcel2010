package com;

import java.awt.List;
import java.util.ArrayList;

/**
 * @author Jeniss @date 2014-1-17 ����11:42:16
 * @tags 
 */
public class StudentInMail {
	private String name;
	private ArrayList<String> homeworks;//��ҵ����
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<String> getHomeworks() {
		return homeworks;
	}
	public void setHomeworks(ArrayList<String> homeworks) {
		this.homeworks = homeworks;
	}
}
