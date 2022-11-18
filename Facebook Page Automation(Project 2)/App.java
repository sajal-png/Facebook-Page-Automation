import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;



public class App 
{
	
	static WebDriver driver;
	static WebDriverWait wait;
	
    public static void main( String[] args )
    {
    	String host = "pop.gmail.com";// change accordingly
		String mailStoreType = "pop3";
		String username = "getmailalert2020@gmail.com";// change accordingly
		String password = "mailalert@2020";// change accordingly
		check(host, mailStoreType, username, password);
    }
    
    public static void check(String host, String storeType, String user, String password) {
		int c = 1;
		do {
			try {

				// create properties field
				Properties properties = new Properties();

				properties.put("mail.pop3.auth", "true");
				properties.put("mail.pop3.host", host);
				properties.put("mail.pop3.port", "995");
				properties.put("mail.pop3.starttls.enable", "true");
				Session emailSession = Session.getDefaultInstance(properties);

				// create the POP3 store object and connect with the pop server
				Store store = emailSession.getStore("imaps");

				store.connect(host, user, password);

				// create the folder object and open it
				Folder emailFolder = store.getFolder("INBOX");
				emailFolder.open(Folder.READ_WRITE);

				// retrieve the messages from the folder in an array and print it
				Message[] messages = emailFolder.getMessages();
				int totEmails = messages.length;
				System.out.println("Total Emails = " +totEmails );

				for (int i = 0; i <totEmails; i++) {
					String result = "";
					Message message = messages[i];
//				System.out.println("**** Last Received Email Info *****\n");
//				System.out.println("Subject: " + message.getSubject());
//				System.out.println("From: " + message.getFrom()[0]);
				MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
					result = getTextFromMimeMultipart(mimeMultipart);
					System.out.println(result);
					String[] URLs = result.replaceAll("<https://www.google.com", "").split(" ");
					for (String list : URLs) {
						if (list.contains("url=https") && !list.contains("www.facebook.com")) {
							list = list.split("url=")[1].split("&ct")[0];
							System.out.println(list);
							postToFacebook(list);
							message.setFlag(Flags.Flag.DELETED, true);
						} else {
							System.out.println(
									"No New Matching Message Found... Total Emails: " + emailFolder.getMessageCount());
						}
					}
				}
				// close the store and folder objects
				emailFolder.close(false);
				store.close();

			} catch (NoSuchProviderException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (c == 1);
	}
    
    
    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
		String result = "";
		int partCount = mimeMultipart.getCount();
		for (int i = 0; i < partCount; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				result = result + "\n" + bodyPart.getContent();
				break; // without break same text appears twice in my tests
			} else if (bodyPart.isMimeType("text/html")) {
				String html = (String) bodyPart.getContent();
				// result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
				result = html;
			} else if (bodyPart.getContent() instanceof MimeMultipart) {
				result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
			}
		}
		return result;
	}
    
    
    public static void postToFacebook(String list) throws InterruptedException {
		try {
			System.setProperty("webdriver.chrome.driver", "C:\\ExternalJars\\chromedriver.exe");
			System.setProperty("webdriver.chrome.silentOutput", "true");
			java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
			ChromeOptions options = new ChromeOptions();
			options.addArguments("user-data-dir=C:\\ChromeProfileFacebook\\Unnao");
//		options.addArguments("--headless");
			driver = new ChromeDriver(options);
//		driver.manage().window().setPosition(new Point(-3000,-3000));
			driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
			driver.manage().window().maximize();
			driver.get("https://www.facebook.com/groups/technonewzz");

			driver.findElement(By.xpath("//span[text()='Write something...']")).click();
			
			driver.findElement(By.xpath("(//div[@data-offset-key])[2]")).sendKeys(list + "\n");
			Thread.sleep(10000);
			driver.findElement(By.xpath("//span[text()='Post']")).click();
			Thread.sleep(5000);
			System.out.println("Post Success");
			driver.quit();
		} catch (Exception e) {
		}
	}
		
		
		
		
}