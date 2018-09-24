package com.docviewer.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.docviewer.models.BaseResponse;
import com.docviewer.models.File;
import com.docviewer.models.FileResponse;
import com.docviewer.services.DriveService;

@RequestMapping("/files")
@RestController
public class FileController {

	@Autowired
	 DriveService service;
	
	/**
	 * It will return all the files and folder inside   
	 * a folder path mentioned after /files/list/  
	 *  Ex:-files/list/Google%20Photos/2014/
	 *  listing files inside folder Google Photos/2014 
	 */
    @GetMapping("/list/**")
    public BaseResponse getFiles(HttpServletRequest request) {
    	//here 3 is being used because of Url hierarchy
     	 return service.getAllFilesAndFolders(request.getServletPath(), 3);      
    }
    /**
     * It will return file in a folder path mentioned after /files/download.
     * ex :- files/download/Google%20Photos/2014/?file=20140408_190651.jpg
     *  File is inside folder Google Photos/2014  
     * Will return Http.Timeout when Server not able to connect to google drive
     * Will return 404 if File is not found  
     */
	@GetMapping(value="/download/**")
	public void downloadFiles(HttpServletRequest request, @RequestParam("file") String file,
			HttpServletResponse response) {
		// here 3 is being used because of Url hierarchy
		BaseResponse baseresponse = service.getFile(request.getServletPath(), 3, file);
		if (baseresponse instanceof FileResponse) {
			FileResponse fileResponse = (FileResponse) baseresponse;
			if (fileResponse != null) {
				List<File> files = fileResponse.getFiles();

				if (fileResponse.getFiles().size() > 0) {
					service.addFileInresponse(files.get(0).getName(), files.get(0).getId(), response, fileResponse);
				} else {
					response.setStatus(404);
				}
			}

		} else {
			response.setStatus(404);
		}

	}
    /**
     * It will take multipart file as argument to folder 
     * mentioned path after /upload   
     */
	@PostMapping("/upload/**")
    public BaseResponse uploadFile(@RequestParam("file") MultipartFile file,HttpServletRequest request) {
	 	 return service.uploadFile(request.getServletPath(), 3,file);      
    }
	
	
}
