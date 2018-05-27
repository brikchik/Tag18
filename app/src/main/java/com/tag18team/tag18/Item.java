package com.tag18team.tag18;
public class Item {
	private int idImg;
	private long ID;
	private String name;
	private String pathOrDescription;
	private boolean isExternalOrFavourite;
	public Item(Integer id, String name, String pathOrDescription, boolean isExternalOrFavourite) {
		this.ID = id;
		this.pathOrDescription = pathOrDescription;
		this.name = name;
		this.isExternalOrFavourite=isExternalOrFavourite;
	}
	public String getName() {
		return name;
	}
	public String getPathOrDescription() {
		return pathOrDescription;
	}
	public long getID() {
		return ID;
	}
	public boolean isExternal(){
		return isExternalOrFavourite;
	}
	public void setPathOrDescription(String pathOrDescription) {
		this.pathOrDescription=pathOrDescription;
	}
	public void setIdImg(int idImg) {
		this.idImg = idImg;
	}
}
