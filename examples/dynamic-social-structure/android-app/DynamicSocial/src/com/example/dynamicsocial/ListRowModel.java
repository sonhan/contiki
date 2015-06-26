package com.example.dynamicsocial;

public class ListRowModel {
	private String name;
	private String content;
	private String icon;
	
	public ListRowModel(String name, String content, String icon) {
		this.name = name;
		this.content = content;
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}
}
