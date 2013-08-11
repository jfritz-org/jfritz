package de.robotniko.fbcrawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import de.robotniko.fbcrawler.exceptions.SIDException;
import de.robotniko.fbcrawler.exceptions.WrongPasswordException;

public class Main {

	private static String boxUrl = "http://192.168.0.1";
	private static String boxPassword = "zc9x54m1";
	private static String savePath = "c:/temp/fritzBox";
	
//	private static Vector<String> visitedUrls = new Vector<String>();
//	
//	private static String linkRegex = "\\s+href=\"([^\"]*)\"";
//	private static Pattern linkPattern;
//	private static Matcher linkMatcher;
	
	public static String[] entry_pages = {
		"/cgi-bin/system_status",
		"/jason_boxinfo.xml",
		"/login_sid.lua",
		"/logincheck.lua",
		"/home/home.lua"
		};
	
	public static void main(String[] args) {
		createSaveDirectories();
		
//		linkPattern = Pattern.compile(linkRegex);
		
		for (String page: entry_pages) {			
			getNonAuthPage(page);
		}
		
		login();
	}


	private static void getNonAuthPage(String page) {
		String response = getUrl(page);
		response = response.replaceAll("\\?", "§");
		saveNonAuthResponseToFile(page, response);
	}
	
	private static void getPage(String page) {
		String response = getUrl(page);
		response = response.replaceAll("\\?", "§");
		saveAuthResponseToFile(page, response);

//		
//		Matcher linkMatcher = linkPattern.matcher(response);
//		while (linkMatcher.find()) {
//			String newUrl = linkMatcher.group(1);
//			if (!visitedUrls.contains(newUrl)) {
//				getPage(newUrl);
//			}
//		}
	}

	private static void createSaveDirectories() {
		File path = new File(savePath);
		if (!path.exists()) {
			path.mkdirs();
		}
	}
	
	public static String getUrl(String url) {
//		visitedUrls.add(url);
		
		String responseBody = "";
		
		HttpClient httpClient = new DefaultHttpClient();
		
		String request = url;
		
		if (!"0000000000000000".equals(SIDLogin.getSessionId())) {
			request = request + "?sid=" + SIDLogin.getSessionId();
		}
		
        try {
            HttpGet httpget = new HttpGet(boxUrl + request);

            System.out.println("executing request " + httpget.getURI());

            // Create a response handler
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            responseBody = httpClient.execute(httpget, responseHandler);
        } catch (ClientProtocolException e) {
        	responseBody = e.getMessage();
        } catch (IOException e) {
        	responseBody = e.getMessage();
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        
        return responseBody;
	}
	
	private static void saveNonAuthResponseToFile(String page, String response) {
		File file = new File(savePath + "/nonauth/" + page);
		file.getParentFile().mkdirs();

		try {
			PrintWriter pw = new PrintWriter(file, "utf-8");
			pw.write(response);
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void saveAuthResponseToFile(String page, String response) {
		File file = new File(savePath + "/auth/" + page);
		file.getParentFile().mkdirs();

		try {
			PrintWriter pw = new PrintWriter(file, "utf-8");
			pw.write(response);
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void login() {
		try {
			if (SIDLogin.isValidLoginMethod(boxUrl)) {
				SIDLogin.login(boxUrl, boxPassword);

				for (String page: entry_pages) {
					getPage(page);
				}			
			}
		} catch (SIDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WrongPasswordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
