package PointUpload.MultiThreadPointUpload;

/*
**FileSplitterFetch.java
*/

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileSplitterFetch extends Thread {

	private static volatile boolean bStop = false; //Stop identical

	private String sURL; //File URL
	long nStartPos; //File Snippet Start Position
	long nEndPos; //File Snippet End Position
	private final int nThreadID; //Thread's ID
	boolean bDownOver = false; //Downing is over
	private FileAccessI fileAccessI = null; //File Access interface

	public FileSplitterFetch(String sURL, String sName, long nStart, long nEnd, int id)
			throws IOException {
		this.sURL = sURL;
		this.nStartPos = nStart;
		this.nEndPos = nEnd;
		nThreadID = id;
		fileAccessI = new FileAccessI(sName, nStartPos);
	}

	@Override
	public void run() {
		while (nStartPos < nEndPos && !bStop) {
			try {
				URL url = new URL(sURL);
				HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
				httpConnection.setRequestProperty("User-Agent", "NetFox");
				String sProperty = "bytes=" + nStartPos + "-" + nEndPos;
				httpConnection.setRequestProperty("RANGE", sProperty);
				Utility.log(sProperty);
				//logResponseHead(httpConnection);

				try (InputStream input = httpConnection.getInputStream();) {
					byte[] b = new byte[1024];
					int nRead;
					int i = 0;
					while ((nRead = input.read(b, 0, 1024)) > 0 && nStartPos < nEndPos && !bStop) {
						nStartPos += fileAccessI.write(b, 0, nRead);
						/*if ((i++) == 3) {
							bStop = true;
						}*/
					}
				/*int i;
				BufferedInputStream bf = new BufferedInputStream(input);
				while ((i = bf.read()) > 0 && nStartPos < nEndPos && !bStop) {
					nStartPos += fileAccessI.write(b, 0, i);
				}*/

					Utility.log("Thread " + nThreadID + " is over!" + "nStartPos : " + nStartPos);
					bDownOver = true;
					//nPos = fileAccessI.write (b,0,nRead);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (bStop) {
			try {
				writeStatuIntoFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 打印回应的头信息
	public void logResponseHead(HttpURLConnection con) {
		for (int i = 1; ; i++) {
			String header = con.getHeaderFieldKey(i);
			if (header != null)
				//responseHeaders.put(header,httpConnection.getHeaderField(header));
				Utility.log(header + " : " + con.getHeaderField(header));
			else
				break;
		}
	}

	public static void splitterStop() {
		bStop = true;
		System.out.println("change to stop bStop: " + true);
	}

	public void writeStatuIntoFile() throws IOException {
		String pathname = "C:\\Users\\Public\\Pictures\\Sample Pictures\\" + nThreadID + ".txt";
		File file = new File(pathname);
		if (nStartPos > nEndPos) {
			file.delete();
		} else {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);
			dataOutputStream.writeLong(nStartPos);
			dataOutputStream.writeLong(nEndPos);
		}
	}

}

/*
**FileAccess.java
*/

class FileAccessI implements Serializable {
	private RandomAccessFile oSavedFile;
	private long nPos;

	public FileAccessI() throws IOException {
		this("", 0);
	}

	public FileAccessI(String sName, long nPos) throws IOException {
		oSavedFile = new RandomAccessFile(sName, "rw");
		this.nPos = nPos;
		oSavedFile.seek(nPos);
	}

	public int write(byte[] b, int nStart, int nLen) {
		int n = -1;
		try {
			oSavedFile.write(b, nStart, nLen);
			n = nLen;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return n;
	}

}

/*
**SiteInfoBean.java
*/
class SiteInfoBean {
	private String sSiteURL; //Site's URL
	private String sFilePath; //Saved File's Path
	private String sFileName; //Saved File's Name
	private int nSplitter; //Count of Splited Downloading File

	public SiteInfoBean() {
		//default value of nSplitter is 5
		this("", "", "", 5);
	}

	public SiteInfoBean(String sURL, String sPath, String sName, int nSpiltter) {
		sSiteURL = sURL;
		sFilePath = sPath;
		sFileName = sName;
		this.nSplitter = nSpiltter;
	}

	public String getSSiteURL() {
		return sSiteURL;
	}

	public void setSSiteURL(String value) {
		sSiteURL = value;
	}

	public String getSFilePath() {
		return sFilePath;
	}

	public void setSFilePath(String value) {
		sFilePath = value;
	}

	public String getSFileName() {
		return sFileName;
	}

	public void setSFileName(String value) {
		sFileName = value;
	}

	public int getNSplitter() {
		return nSplitter;
	}

	public void setNSplitter(int nCount) {
		nSplitter = nCount;
	}
}

/*
**Utility.java
*/

class Utility {
	public Utility() {
	}

	public static void sleep(int nSecond) {
		try {
			Thread.sleep(nSecond);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void log(String sMsg) {
		System.err.println(sMsg);
	}

	public static void log(int sMsg) {
		System.err.println(sMsg);
	}
}

/*
**TestMethod.java
*/

class TestMethod {
	public TestMethod() {
		try {
			//SiteInfoBean bean = new SiteInfoBean("http://localhost:8080/down.zip","L:\\temp",
			SiteInfoBean bean = new SiteInfoBean("http://39.108.74.219:90/images/aj001.png", "C:\\Users\\Public\\Pictures\\Sample Pictures", "aj001.png", 5);
			SiteFileFetch fileFetch = new SiteFileFetch(bean);
			fileFetch.start();
			FileSplitterFetch.splitterStop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new TestMethod();
	}
}
