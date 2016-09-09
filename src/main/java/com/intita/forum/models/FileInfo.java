package com.intita.forum.models;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;

import com.intita.forum.util.Transliterator;

import utils.RandomString;

public class FileInfo implements Comparable<FileInfo> {
	private static final int DEFAULT_FILE_PREFIX_LENGTH = 15;
	private String fileName;
	private String shortName;
	private Date creationDate;
	static RandomString randString = new RandomString(DEFAULT_FILE_PREFIX_LENGTH);
	private Long size;
	
	public final static ArrayList<String> supportedImgFormat = new ArrayList<>(Arrays.asList(new String[]{"png", "jpg", "gif", "tif"}));
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
	public static String bytesToSize(Long bytes) {
		String []sizes = {"Bytes", "KB", "MB", "GB", "TB"};
		if (bytes == 0) 
			return "0 Bytes";
		int i = (int) Math.floor(Math.log(bytes) / Math.log(1024));
		return round(bytes / Math.pow(1024, i), 2) + " " + sizes[i];
	}
	public String getSizeString(){
		return bytesToSize(size);
	}
	public String getExtension(){
		return FilenameUtils.getExtension(this.shortName);
	}
	public boolean isImage(){
		return supportedImgFormat.contains(getExtension());
	}
	
	public static FileInfo createFileInfoFromFullName(String fileName,Date creationDate, Long size){
		FileInfo fInfo = new FileInfo();
		fInfo.fileName = fileName;
		fInfo.shortName = deRandomizeFileName(fileName);
		fInfo.creationDate = creationDate;
		return fInfo;
	}
	public static FileInfo createFileInfoFromShortName(String fileName,Date creationDate, Long size){
		FileInfo fInfo = new FileInfo();
		fInfo.shortName = fileName;
		fInfo.fileName = randomizeFileName(fileName);
		fInfo.creationDate = creationDate;
		return fInfo;
	}
	public static FileInfo createFileInfoFromFile(File file){
		FileInfo fInfo = new FileInfo();
		String pathStr = file.getAbsolutePath();
		fInfo.shortName = Transliterator.transliterate(deRandomizeFileName(file.getName()));
		fInfo.fileName = file.getName();
		fInfo.size = file.length();
		Path path = Paths.get(pathStr);
		try {
			BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
			fInfo.creationDate = new Date(attr.creationTime().toMillis());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return fInfo;
	}
	private static String randomizeFileName(String fileName){
		String nameSufix = randString.nextString();
		return fileName + nameSufix;
	}
	private static String deRandomizeFileName(String randomizedFileName){
		return randomizedFileName.substring(0,randomizedFileName.length()-DEFAULT_FILE_PREFIX_LENGTH);
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	@Override
	public int compareTo(FileInfo fInfo) {
		return fInfo.getCreationDate().compareTo(creationDate);
	}

}
