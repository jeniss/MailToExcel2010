package com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author Jeniss @date 2014-1-16 上午11:23:48
 * @tags
 */
public class ReadFileFromMail {
	private String filePath = "C:\\Users\\Administrator\\Desktop\\1.txt";

//	public static void main(String[] args) {
//		ControlExcelFile controlExcelFile = new ControlExcelFile();
//		controlExcelFile.writeFile();
//	}

	public void write() {
		try {
			FileWriter fw = new FileWriter(filePath, true);
			BufferedWriter bw = new BufferedWriter(fw);

			ReciveMail reciveMail = new ReciveMail();
			ArrayList<StudentInMail> studentInMails = reciveMail.reciveInfoFormMail();

			for (int i = 0; i < studentInMails.size(); i++) {
				bw.write(studentInMails.get(i).getName() + ":");
				for (int j = 0; j < studentInMails.get(i).getHomeworks().size(); j++) {
					bw.write(studentInMails.get(i).getHomeworks().get(j) + ";");
				}
				bw.write("\r\n");
			}
			bw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<StudentInMail> read() {
		String line; // 用来保存每行读取的内容
		ArrayList<StudentInMail> students = new ArrayList<>();
		try {
			InputStream is = new FileInputStream(filePath);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			
			while ((line = reader.readLine()) != null) { // 如果 line 为空说明读完了
//				System.out.println(line);
				String[] info = line.split(":");
				StudentInMail studentInMail = new StudentInMail();
				studentInMail.setName(info[0].trim());
				String[] attchInfo = info[1].split(";");
				ArrayList<String> names = new ArrayList<>();
				for (int i = 0; i < attchInfo.length; i++) {
					names.add(attchInfo[i].trim());
				}
				studentInMail.setHomeworks(names);
				students.add(studentInMail);
			}
			reader.close();
			is.close();
//			System.out.println("————————————————————————————————");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return students;
	}
}
