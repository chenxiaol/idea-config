package PointUpload.MultiThreadPointUpload;

/*
 /*
 * SiteFileFetch.java
 */

import com.alibaba.fastjson.JSON;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class SiteFileFetch extends Thread {
	private static ConcurrentHashMap<String, SiteFileFetch> conMap = new ConcurrentHashMap<>();
	private SiteInfoBean siteInfoBean = null; // 文件信息 Bean
	private long[] nStartPos; // 开始位置
	private long[] nEndPos; // 结束位置
	private FileSplitterFetch[] fileSplitterFetch; // 子线程对象
	private long nFileLength; // 文件长度
	private boolean bFirst = true; // 是否第一次取文件
	private boolean bStop = false; // 停止标志
	private File tmpFile; // 文件下载的临时信息
	private DataOutputStream output; // 输出到文件的输出流

	public SiteFileFetch(SiteInfoBean bean) throws IOException {
		siteInfoBean = bean;
		//tmpFile = File.createTempFile ("zhong","1111",new File(bean.getSFilePath()));
		tmpFile = new File("C:\\Users\\Public\\Pictures\\Sample Pictures\\temp");
		if (0 < tmpFile.listFiles().length) {
			System.out.println(JSON.toJSON("临时文件:" + tmpFile));
			bFirst = false;
			read_nPos(tmpFile.listFiles());
		} else {
			nStartPos = new long[bean.getNSplitter()];
			nEndPos = new long[bean.getNSplitter()];
		}
	}

	@Override
	public void run() {
		// 获得文件长度
		// 分割文件
		// 实例 FileSplitterFetch
		// 启动 FileSplitterFetch 线程
		// 等待子线程返回
		try {
			//没有产生间断是第一次传输
			if (bFirst) {
				nFileLength = getFileSize();
				if (nFileLength == -1) {
					System.err.println("File Length is not known!");
					return;
				} else if (nFileLength == -2) {
					System.err.println("File is not access!");
					return;
				} else {
					//相当于作了分区如nstart[100]-nend[200]
					for (int i = 0; i < nStartPos.length; i++) {
						nStartPos[i] = (i * (nFileLength / nStartPos.length));
					}

					for (int i = 0; i < nEndPos.length - 1; i++) {
						nEndPos[i] = nStartPos[i + 1];
					}
					nEndPos[nEndPos.length - 1] = nFileLength;

					System.out.println("当前分区策略");
					for (int i = 0; i < nStartPos.length; i++) {
						System.out.println("start[" + i + "]=" + nStartPos[i] + "--" + "end[" + i + "]=" + nEndPos[i]);
					}
				}
			}
			long startmiss = System.currentTimeMillis();
			// 启动子线程
			fileSplitterFetch = new FileSplitterFetch[nStartPos.length];
			for (int i = 0; i < nStartPos.length; i++) {
				//路径名称
				String sName = siteInfoBean.getSFilePath() + File.separator + siteInfoBean.getSFileName();
				//多线程执行
				fileSplitterFetch[i] = new FileSplitterFetch(siteInfoBean.getSSiteURL(), sName, nStartPos[i], nEndPos[i], i);
				Utility.log("Thread " + i + " , nStartPos = " + nStartPos[i] + ", nEndPos = " + nEndPos[i]);
				fileSplitterFetch[i].start();
				//write_nPos();
				fileSplitterFetch[i].join();
			}
			System.out.println("文件写总共耗时：" + (System.currentTimeMillis() - startmiss) + "ms");
			System.err.println("文件下载结束！");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 获得文件长度
	public long getFileSize() {
		int nFileLength = -1;
		try {
			URL url = new URL(siteInfoBean.getSSiteURL());
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			int responseCode = httpConnection.getResponseCode();
			if (responseCode >= 400) {
				processErrorCode(responseCode);
				return -2; //-2 represent access is error
			}

			String headerField = httpConnection.getHeaderField("Content-Length");
			if (null != headerField) {
				nFileLength = Integer.parseInt(headerField);
			} else {
				nFileLength = -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Utility.log(nFileLength);
		return nFileLength;
	}

	// 保存下载信息（文件指针位置）
	public void write_nPos() throws IOException {
		try {
			output = new DataOutputStream(new FileOutputStream(tmpFile));
			output.writeInt(nStartPos.length);
			for (int i = 0; i < nStartPos.length; i++) {
				// output.writeLong(nPos[i]);
				output.writeLong(fileSplitterFetch[i].nStartPos);
				output.writeLong(fileSplitterFetch[i].nEndPos);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			output.close();
		}
	}

	// 读取保存的下载信息（文件指针位置）
	public void read_nPos(File[] files) throws IOException {
		DataInputStream input = null;
		try {
			input = new DataInputStream(new FileInputStream(tmpFile));
			int nCount = files.length;
			nStartPos = new long[nCount];
			nEndPos = new long[nCount];
			for (int i = 0; i < files.length; i++) {
				nStartPos[i] = input.readLong();
				nEndPos[i] = input.readLong();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != input) {
				input.close();
			}
		}
	}

	private void processErrorCode(int nErrorCode) {
		System.err.println("Error Code : " + nErrorCode);
	}

	// 停止文件下载
	public void siteStop() {
		bStop = true;
		for (int i = 0; i < nStartPos.length; i++)
			fileSplitterFetch[i].splitterStop();
	}

	public static void main(String[] args) throws IOException {
		String pathname = "C:\\Users\\Public\\Pictures\\Sample Pictures\\新建文本文档.txt";
		//tmpFile = new File(pathname);
		/*String pathname2 = "C:\\Users\\Public\\Pictures\\Sample Pictures\\新建文本文档 (2).txt";
		fileSplitterFetch = new FileSplitterFetch[] { new FileSplitterFetch("", pathname2, 0, 1000, 0),
				new FileSplitterFetch("", pathname2, 1000, 2000, 1),
				new FileSplitterFetch("", pathname2, 2000, 3000, 2),
				new FileSplitterFetch("", pathname2, 3000, 4000, 3)

		};
		nStartPos = new long[] { 0,1,2,3 };
		write_nPos();*/
		//read_nPos();
	}
}