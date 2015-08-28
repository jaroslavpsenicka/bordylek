package org.bordylek.service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Document(collection = "community")
public class Community implements Unique {

	@Id
	private String id;

	@NotNull
	@Indexed(sparse = true)
	private String title;

	@NotNull
	private Date createDate;
	
	private String summary;
	private String text;
	private boolean deleted;
	private String iconId;
	private String url;
	
	private Point location;

	public Community() {
		this.createDate = new Date();
	}
	
	public Community(String title) {
		this.createDate = new Date();
		this.title = title;
	}

	public Community(String title, Point location) {
		this.createDate = new Date();
		this.title = title;
		this.location = location;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isDeleted() {
		return this.deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getCreateDate() {
		return this.createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getIconId() {
		return this.iconId;
	}

	public void setIconId(String iconId) {
		this.iconId = iconId;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getSummary() {
		return this.summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Point getLocation() {
		return location;
	}
	
	public void setLocation(Point location) {
		this.location = location;
	}
	
	@Override
	public String toString() {
		return "Community "+title+" (id="+id+")";
	}
}
