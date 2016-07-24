package com;

import java.awt.List;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

public class ReciveMail {
	private MimeMessage msg = null;
	private StringBuffer bodytext = new StringBuffer();

	/**
	 * ��ȡ�����ʼ�����Ϣ
	 * 
	 * @return
	 * @throws MessagingException
	 */
	protected String getFrom() throws MessagingException {
		InternetAddress[] address = (InternetAddress[]) msg.getFrom();
		String from = address[0].getAddress();
		if (from == null) {
			from = "";
		}
		String personal = address[0].getPersonal();
		if (personal == null) {
			personal = "";
		}
		String fromaddr = personal + "<" + from + ">";
		return fromaddr;
	}

	/**
	 * ��ȡ�ʼ��ռ��ˣ����ͣ����͵ĵ�ַ����Ϣ�����������ݵĲ�����ͬ "to"-->�ռ���,"cc"-->�����˵�ַ,"bcc"-->���͵�ַ
	 * 
	 * @param type
	 * @return
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	protected String getMailAddress(String type) throws MessagingException, UnsupportedEncodingException {
		String mailaddr = "";
		String addrType = type.toUpperCase();
		InternetAddress[] address = null;

		if (addrType.equals("TO") || addrType.equals("CC") || addrType.equals("BCC")) {
			if (addrType.equals("TO")) {
				address = (InternetAddress[]) msg.getRecipients(Message.RecipientType.TO);
			}
			if (addrType.equals("CC")) {
				address = (InternetAddress[]) msg.getRecipients(Message.RecipientType.CC);
			}
			if (addrType.equals("BCC")) {
				address = (InternetAddress[]) msg.getRecipients(Message.RecipientType.BCC);
			}

			if (address != null) {
				for (int i = 0; i < address.length; i++) {
					String mail = address[i].getAddress();
					if (mail == null) {
						mail = "";
					} else {
						mail = MimeUtility.decodeText(mail);
					}
					String personal = address[i].getPersonal();
					if (personal == null) {
						personal = "";
					} else {
						personal = MimeUtility.decodeText(personal);
					}
					String compositeto = personal + "<" + mail + ">";
					mailaddr += "," + compositeto;
				}
				mailaddr = mailaddr.substring(1);
			}
		} else {
			throw new RuntimeException("Error email Type!");
		}
		return mailaddr;
	}

	/**
	 * ��ȡ�ʼ�����
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	protected String getSubject() throws UnsupportedEncodingException, MessagingException {
		String subject = "";
		subject = MimeUtility.decodeText(msg.getSubject());
		if (subject == null) {
			subject = "";
		}
		return subject;
	}

	/**
	 * ��ȡ�ʼ���������
	 * 
	 * @return
	 */
	protected String getBodyText() {

		return bodytext.toString();
	}

	/**
	 * �����ʼ������õ����ʼ����ݱ��浽һ��stringBuffer�����У������ʼ� ��Ҫ����MimeType�Ĳ�ִͬ�в�ͬ�Ĳ�����һ��һ���Ľ���
	 * 
	 * @param part
	 * @throws MessagingException
	 * @throws IOException
	 */
	protected void getMailContent(Part part) throws MessagingException, IOException {

		String contentType = part.getContentType();
		int nameindex = contentType.indexOf("name");
		boolean conname = false;
		if (nameindex != -1) {
			conname = true;
		}
		// System.out.println("CONTENTTYPE:" + contentType);
		if (part.isMimeType("text/plain") && !conname) {
			bodytext.append((String) part.getContent());
		} else if (part.isMimeType("text/html") && !conname) {
			bodytext.append((String) part.getContent());
		} else if (part.isMimeType("multipart/*")) {
			Multipart multipart = (Multipart) part.getContent();
			int count = multipart.getCount();
			for (int i = 0; i < count; i++) {
				getMailContent(multipart.getBodyPart(i));
			}
		} else if (part.isMimeType("message/rfc822")) {
			getMailContent((Part) part.getContent());
		}

	}

	/**
	 * �ж��ʼ��Ƿ���Ҫ��ִ�������ִ����true�����򷵻�false
	 * 
	 * @return
	 * @throws MessagingException
	 */
	protected boolean getReplySign() throws MessagingException {
		boolean replySign = false;
		String needreply[] = msg.getHeader("Disposition-Notification-TO");
		if (needreply != null) {
			replySign = true;
		}
		return replySign;
	}

	/**
	 * ��ȡ���ʼ���message-id
	 * 
	 * @return
	 * @throws MessagingException
	 */
	protected String getMessageId() throws MessagingException {
		return msg.getMessageID();
	}

	/**
	 * �жϴ��ʼ��Ƿ��Ѷ������δ���򷵻�false���Ѷ�����true
	 * 
	 * @return
	 * @throws MessagingException
	 */
	protected boolean isNew() throws MessagingException {
		boolean isnew = false;
		Flags flags = ((Message) msg).getFlags();
		Flags.Flag[] flag = flags.getSystemFlags();
		System.out.println("flags's length:" + flag.length);
		for (int i = 0; i < flag.length; i++) {
			if (flag[i] == Flags.Flag.SEEN) {
				isnew = true;
				System.out.println("seen message .......");
				break;
			}
		}

		return isnew;
	}

	/**
	 * �ж����Ƿ��������
	 * 
	 * @param part
	 * @return
	 * @throws MessagingException
	 * @throws IOException
	 */
	protected String isContainAttch(Part part) throws MessagingException, IOException {
		String flag = "";

		String contentType = part.getContentType();
		if (part.isMimeType("multipart/*")) {
			Multipart multipart = (Multipart) part.getContent();
			int count = multipart.getCount();
			for (int i = 0; i < count; i++) {
				BodyPart bodypart = multipart.getBodyPart(i);
				String dispostion = bodypart.getDisposition();
				if ((dispostion != null)
						&& (dispostion.equals(Part.ATTACHMENT) || dispostion.equals(Part.INLINE))) {
					flag += MimeUtility.decodeText(bodypart.getFileName()) + ";";
				} /*
				 * else if (bodypart.isMimeType("multipart/*")) { flag +=
				 * isContainAttch(bodypart); }
				 */else {
					String conType = bodypart.getContentType();
					if (conType.toLowerCase().indexOf("appliaction") != -1) {
						flag += MimeUtility.decodeText(bodypart.getFileName()) + ";";
					}
					if (conType.toLowerCase().indexOf("name") != -1) {
						flag += MimeUtility.decodeText(bodypart.getFileName()) + ";";
					}
				}
			}
		} else if (part.isMimeType("message/rfc822")) {
			flag += isContainAttch((Part) part.getContent());
		}

		return flag;
	}

	protected String[] recive(Part part, int i) throws MessagingException, IOException {
		// System.out.println("------------------START-----------------------");
		// System.out.println(i + " subject:" + getSubject());
		// System.out.println("Message" + i + " from:" + getFrom());
		// System.out.println("Message" + i + " isNew:" + isNew());
		String flag = isContainAttch(part);
		// System.out.println(i + " Attch:" + flag);
		// System.out.println("Message" + i + " replySign:" + getReplySign());
		// getMailContent(part);
		// String content = getBodyText();
		// System.out.println("Message" + i + " content:" + getBodyText());
		// if (flag) {
		// saveAttchMent(part);
		// }

		String info[] = { getSubject(), flag };
		return info;
	}

	protected Message[] getMessages() {
		Message[] messages = null;
		Properties props = new Properties();
		// �洢�����ʼ�������ʹ�õ�Э�飬������POP3Ϊ��
		props.setProperty("mail.store.protocol", "pop3");
		// ���ý����ʼ��������ĵ�ַ�����ﻹ��������163Ϊ��
		props.setProperty("mail.pop3.host", "pop3.163.com");
		// ���������½�һ���ʼ��Ự.
		Session session = Session.getInstance(props);
		// �ӻỰ�����л��POP3Э���Store����
		Store store;
		try {
			store = session.getStore("pop3");
			// �����Ҫ�鿴�����ʼ�����ϸ��Ϣ����Ҫ����Debug��־
			session.setDebug(false);
			// �����ʼ�������
			store.connect("pop3.163.com", "javalesson@163.com", "sharper123");
			// ��ȡ�ʼ����������ռ���
			Folder folder = store.getFolder("INBOX");
			// ��ֻ��Ȩ�޴��ռ���
			folder.open(Folder.READ_ONLY);
			// ��ȡ�ռ����е��ʼ���Ҳ����ʹ��getMessage(int �ʼ��ı��)����ȡ����ĳһ���ʼ�
			messages = folder.getMessages();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return messages;
	}

	public ArrayList<StudentInMail> reciveInfoFormMail() {
		ArrayList<StudentInMail> students = new ArrayList<>();
		Message[] messages = getMessages();
		if (messages != null) {
			int count = messages.length;
			System.out.println("Message Count:" + count);
			for (int i = 0; i < count; i++) {
				msg = (MimeMessage) messages[i];
				try {
					String[] studentInfo = recive(messages[i], i);
					// �д���ĸ�������
					if (studentInfo[1].contains("zip") || studentInfo[1].contains("rar")
							|| studentInfo[1].contains("7z")) {
						StudentInMail studentInMail = new StudentInMail();
						studentInMail.setName(studentInfo[0]);

						String[] attchs = studentInfo[1].split(";");
						ArrayList<String> homeworks = new ArrayList<String>();
						for (int j = 0; j < attchs.length; j++) {
							if (attchs[j].contains("zip") || attchs[j].contains("rar")
									|| studentInfo[1].contains("7z")) {
								String attchName = attchs[j].split("\\.")[0];
								homeworks.add(attchName);
							}
						}
						studentInMail.setHomeworks(homeworks);
						students.add(studentInMail);
						System.out.println(i + ":" + studentInMail.getName() + ":"
								+ studentInMail.getHomeworks().size());
					}
				} catch (MessagingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return students;
	}

	// protected static void main(String[] args) throws MessagingException,
	// IOException {
	// Properties props = new Properties();
	// props.setProperty("mail.smtp.host", "smtp.sina.com");
	// props.setProperty("mail.smtp.auth", "true");
	// Session session = Session.getDefaultInstance(props, null);
	// URLName urlname = new URLName("pop3", "pop.qq.com", 110, null,
	// "715881036", "kingsoft");
	//
	// Store store = session.getStore(urlname);
	// store.connect();
	// Folder folder = store.getFolder("INBOX");
	// folder.open(Folder.READ_ONLY);
	// Message msgs[] = folder.getMessages();
	// int count = msgs.length;
	// System.out.println("Message Count:" + count);
	// ReciveMail rm = null;
	// for (int i = 0; i < count; i++) {
	// rm = new ReciveMail((MimeMessage) msgs[i]);
	// rm.recive(msgs[i], i);
	// ;
	// }
	//
	// }

}