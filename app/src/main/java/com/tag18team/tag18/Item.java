package com.tag18team.tag18;
public class Item {
	private int idImg;
	private long fileID;
	private String name;
	private String path;
	private boolean is_external;
	public Item(Integer id, String name, String path, boolean is_external) {
		this.fileID = id;
		this.path = path;
		this.name = name;
		this.is_external=is_external;
	}
	public String getName() {
		return name;
	}
	public String getPath() {
		return path;
	}
	public long getID() {
		return fileID;
	}
	public boolean isExternal(){
		return is_external;
	}
	public void setPath(String path) {
		this.path=path;
	}
	public void setIdImg(int idImg) {
		this.idImg = idImg;
	}
}
