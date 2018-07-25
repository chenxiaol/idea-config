package Just_Test.CutPointUpLoad;

/*
**FileSplitterFetch.java
*/

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileSplitterFetch extends Thread {
	String sURL; //File URL
	long nStartPos; //File Snippet Start Position
	long nEndPos; //File Snippet End Position
	int nThreadID; //Thread's ID
	boolean bDownOver = false; //Downing is over
	boolean bStop = false; //Stop identical
	FileAccessI fileAccessI = null; //File Access interface

	public FileSplitterFetch(String sURL, String sName, long nStart, long nEnd, int id)
			throws IOException {
		this.sURL = sURL;
		this.nStartPos = nStart;
		this.nEndPos = nEnd;
		nThreadID = id;
		fileAccessI = new FileAccessI(sName, nStartPos);
	}

	public void run() {
		while (nStartPos < nEndPos && !bStop) {
			try {
				URL url = new URL(sURL);
				HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
				httpConnection.setRequestProperty("User-Agent", "NetFox");
				String sProperty = "bytes=" + nStartPos + "-";
				httpConnection.setRequestProperty("RANGE", sProperty);
				Utility.log(sProperty);
				InputStream input = httpConnection.getInputStream();
				//logResponseHead(httpConnection);
				byte[] b = new byte[1024];
				int nRead;
				while ((nRead = input.read(b, 0, 1024)) > 0 && nStartPos < nEndPos
						&& !bStop) {
					nStartPos += fileAccessI.write(b, 0, nRead);
					//if(nThreadID == 1)
					// Utility.log("nStartPos = " + nStartPos + ", nEndPos = " + nEndPos);
				}
				Utility.log("Thread " + nThreadID + " is over!");
				bDownOver = true;
				//nPos = fileAccessI.write (b,0,nRead);
			} catch (Exception e) {
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

	public void splitterStop() {
		bStop = true;
	}
}

/*
**FileAccess.java
*/

class FileAccessI implements Serializable {
	RandomAccessFile oSavedFile;
	long nPos;

	public FileAccessI() throws IOException {
		this("", 0);
	}

	public FileAccessI(String sName, long nPos) throws IOException {
		oSavedFile = new RandomAccessFile(sName, "rw");
		this.nPos = nPos;
		oSavedFile.seek(nPos);
	}

	public synchronized int write(byte[] b, int nStart, int nLen) {
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
	public TestMethod() { ///xx/weblogic60b2_win.exe
		try {
			//SiteInfoBean bean = new SiteInfoBean("http://localhost:8080/down.zip","L:\\temp",
			SiteInfoBean bean = new SiteInfoBean("http://39.108.74.219:90/images/aj001.png", "D:\\用户目录\\我的图片", "aj001.png", 5);
			SiteFileFetch fileFetch = new SiteFileFetch(bean);
			fileFetch.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new TestMethod();
	}
}
