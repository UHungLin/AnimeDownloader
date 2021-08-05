package org.lin.util;

import org.lin.exception.DownloaderException;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

/**
 * @author Lin =￣ω￣=
 * @date 2020/6/15
 */
public class FileUtils {

	public static boolean existsDirectory(String strDir) {
		File file = new File(strDir);
		return file.exists() && file.isDirectory();
	}

	public static boolean existsFile(String strDir, String name) {
		strDir = strDir + File.separator + name;
		return existsFile(strDir);
	}

	public static boolean existsFile(String path) {
		File file = new File(path);
		return file.exists();
	}

	public static boolean existsFile(File file) {
		return existsFile(file.getParent(), file.getName());
	}

	public static void createFile(String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			String dirPath = file.getParent();
			createDirectory(dirPath);
			file.createNewFile();
		}
	}

	public static boolean deleteFile(String path) {
		return new File(path).delete();
	}

	public static boolean deleteDir(String path) {
		return deleteDir(new File(path));
	}

	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	public static void createDirectory(String path) throws IOException {
		File file = new File(path);
		if (!file.exists()) {
			Files.createDirectories(file.toPath());
		}
	}

	public static long getFileTotalSize(String strDir, String name) {
		if (!existsFile(strDir, name))
			return 0L;
		strDir = strDir + File.separator + name;
		File file = new File(strDir);
		return file.length();
	}

	public static long getFileTotalSize(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			return file.length();
		}
		return 0L;
	}

	public static String renameFile(String filePath) {
		File file = new File(filePath);
		return renameFile(file.getParent(), file.getName());
	}

	public static String renameFile(String strDir, String fileName) {
		String toPrefix = fileName.substring(0, fileName.lastIndexOf('.'));
		String toSuffix = fileName.substring(fileName.lastIndexOf('.'));
		String newFileName = toPrefix;
		for (int i = 1; i < Integer.MAX_VALUE; i ++) {
			newFileName = toPrefix + "(" + i + ")" + toSuffix;
			if (!existsFile(strDir, newFileName)) {
				break;
			}
		}
		return newFileName.substring(0, newFileName.lastIndexOf('.'));
	}

	public static String renameDir(String filePath) {
		String newFilePath = filePath;
		for (int i = 1; i < Integer.MAX_VALUE; i ++) {
			newFilePath = filePath + "(" + i + ")";
			if (!existsDirectory(newFilePath)) {
				break;
			}
		}
		return new File(newFilePath).getName();
	}

	// xxx.flv -> xxx.mp4
	public static String renameFileSuffix(String filePath, String newSuffixName) {
		File file = new File(filePath);
		if (file.exists()) {
			String parentPath = file.getParent();
			String fileName = file.getName();
			String toPrefix = fileName.substring(0, fileName.lastIndexOf('.'));
			return toPrefix + ".mp4";
		}
		return null;
	}

	public static boolean canWrite(String path) {
		File file = new File(path);
		File temp;
		if (file.isFile()) {
			temp = new File(file.getParent() + System.lineSeparator() + UUID.randomUUID() + ".test");
		} else {
			temp = new File(file.getPath() + System.lineSeparator() + UUID.randomUUID() + ".test");
		}
		try {
			temp.getAbsolutePath();
			temp.createNewFile();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public static long getDiskFreeSize(String path) {
		File file = new File(path);
		return file.getFreeSpace();
	}

	public static void setLength(String filePath, long length) throws IOException, DownloaderException {
		RandomAccessFile raf = null;
		try {
			File file = new File(filePath);
			raf = new RandomAccessFile(file, "rw");
			final long breakpointBytes = new File(filePath).length();
			final long requiredSpaceBytes = length - breakpointBytes;
			final long freeSize = getDiskFreeSize(filePath);
			if (freeSize < requiredSpaceBytes) {
				throw new DownloaderException("out of space exception");
			}
			raf.setLength(length);
		} finally {
			if (raf != null) raf.close();
		}
	}

	public static void main(String[] args) throws IOException, DownloaderException {
		setLength("D:\\test\\test.txt", 10000);
	}

}
