package com.docviewer.models;

import java.util.ArrayList;
import java.util.List;

public class FileResponse extends BaseResponse {
List<File> files = new ArrayList<>();

public List<File> getFiles() {
	return files;
}

public void setFiles(List<File> files) {
	this.files = files;
}

 
}
