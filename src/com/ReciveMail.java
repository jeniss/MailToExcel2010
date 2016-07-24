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
	 * 获取发送邮件者信息
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
	 * 获取邮件收件人，抄送，密送的地址和信息。根据所传递的参数不同 "to"-->收件人,"cc"-->抄送人地址,"bcc"-->密送地址
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
	 * 获取邮件主题
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
	 * 获取邮件正文内容
	 * 
	 * @return
	 */
	protected String getBodyText() {

		return bodytext.toString();
	}

	/**
	 * 解析邮件，将得到的邮件内容保存到一个stringBuffer对象中，解析邮件 主要根据MimeType的不同执行不同的操作，一步一步的解析
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
	 * 判断邮件是否需要回执，如需回执返回true，否则返回false
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
	 * 获取此邮件的message-id
	 * 
	 * @return
	 * @throws MessagingException
	 */
	protected String getMessageId() throws MessagingException {
		return msg.getMessageID();
	}

	/**
	 * 判断此邮件是否已读，如果未读则返回false，已读返回true
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
	 * 判断是是否包含附件
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
		// 存储接收邮件服务器使用的协议，这里以POP3为例
		props.setProperty("mail.store.protocol", "pop3");
		// 设置接收邮件服务器的地址，这里还是以网易163为例
		props.setProperty("mail.pop3.host", "pop3.163.com");
		// 根据属性新建一个邮件会话.
		Session session = Session.getInstance(props);
		// 从会话对象中获得POP3协议的Store对象
		Store store;
		try {
			store = session.getStore("pop3");
			// 如果需要查看接收邮件的详细信息，需要设置Debug标志
			session.setDebug(false);
			// 连接邮件服务器
			store.connect("pop3.163.com", "javalesson@163.com", "sharper123");
			// 获取邮件服务器的收件箱
			Folder folder = store.getFolder("INBOX");
			// 以只读权限打开收件箱
			folder.open(Folder.READ_ONLY);
			// 获取收件箱中的邮件，也可以使用getMessage(int 邮件的编号)来获取具体某一封邮件
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
					// 有打包的附件则处理
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