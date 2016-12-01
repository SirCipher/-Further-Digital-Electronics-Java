package server;

import java.io.Serializable;

public class VideoFile implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id, title, filename, imageAddress;

	public VideoFile() {

	}

	public void setId(String id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setImageAddress(String url) {
		this.imageAddress = url;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Object getID() {
		return id;
	}

	public Object getTitle() {
		return title;
	}

	public Object getFilename() {
		return filename;
	}

	public Object getPoster() {
		return imageAddress;
	}

}