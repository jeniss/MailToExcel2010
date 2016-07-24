package com;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author Jeniss @date 2014-1-17 下午12:48:25
 * @tags
 */
public class ControlExcelFile {
	private File file = new File("C:\\Users\\Administrator\\Desktop\\1.xlsx");

	public XSSFWorkbook getXSSFWorkbook() {
		XSSFWorkbook book = null;
		try {
			if (file.exists()) {
				book = new XSSFWorkbook(new FileInputStream(file));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return book;
	}

	public ArrayList<String> readFile() {
		ArrayList<String> names = new ArrayList<String>();
		XSSFWorkbook book = getXSSFWorkbook();
		// 读取第一章表格内容, 行为0,列为0开始
		XSSFSheet sheet = book.getSheetAt(0);
		int count = sheet.getPhysicalNumberOfRows();
		for (int i = 1; i < count; i++) {
			Row row = sheet.getRow(i);
			String name = row.getCell(3).toString();
			names.add(name);
		}
		return names;
	}

	public void writeFile() {
		XSSFWorkbook book = getXSSFWorkbook();
		// 读取第一章表格内容
		XSSFSheet sheet = book.getSheetAt(0);

		ReadFileFromMail test = new ReadFileFromMail();
		ArrayList<StudentInMail> studentInMails = test.read();

		ArrayList<String> studentNames = readFile();
		ArrayList<StudentInMail> matchedMails = new ArrayList<>();
		int homeworkCount = 0;
		int col = 3;
		for (int i = 0; i < studentNames.size(); i++) {
			String name = studentNames.get(i);
			for (int j = 0; j < studentInMails.size(); j++) {
				boolean isMatch = false;
				String studentInMailsName = studentInMails.get(j).getName();
				ArrayList<String> attchNames = studentInMails.get(j).getHomeworks();
				for (int k = 0; k < attchNames.size(); k++) {
					String attchName = attchNames.get(k);
					if (attchName.contains(name)) {
						char homeworkCountStr = attchName.charAt(attchName.length() - 1);
						if (homeworkCountStr > 48 && homeworkCountStr < 56) {// >0,<8
							homeworkCount = Integer.parseInt(String.valueOf(homeworkCountStr));
							createCell(i + 1, col + homeworkCount, "1", sheet);
							studentInMails.remove(j);
							isMatch = true;
						} 
					}
				}
				if (!isMatch) {
					if (studentInMailsName.contains(name)) {
						char homeworkCountStr = studentInMailsName
								.charAt(studentInMailsName.length() - 1);
						if (homeworkCountStr > 48 && homeworkCountStr < 56) {
							homeworkCount = Integer.parseInt(String.valueOf(homeworkCountStr));
							createCell(i + 1, col + homeworkCount, "1", sheet);
							studentInMails.remove(j);
						} else {
							System.out.println("mail:" + studentInMailsName);
							studentInMails.remove(j);
						}
					}
				}
			}
		}
		System.out.println("———————————no match" + studentInMails.size() + "———————————");
		for (StudentInMail mails : studentInMails) {
			System.out.println(mails.getName());
		}
		System.out.println("ok");
		try {
			// 创建文件流
			OutputStream stream = new FileOutputStream(file);
			// 写入数据
			book.write(stream);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void createCell(int row, int col, String info, XSSFSheet sheet) {
		if (sheet.getRow(row) == null) {
			sheet.createRow(row);
		}
		Cell cell = sheet.getRow(row).createCell(col);
		cell.setCellValue(info);
	}
}
