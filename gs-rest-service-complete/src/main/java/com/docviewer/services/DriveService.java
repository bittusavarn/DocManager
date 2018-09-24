package com.docviewer.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.docviewer.models.BaseResponse;
import com.docviewer.models.FileResponse;
import com.docviewer.models.FileType;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

@Component
public class DriveService {
  
	@Autowired
	Drive drive;
	
	public BaseResponse getAllFilesAndFolders(String path,int index) {
		String[] paths =  path.split("/");
        return getFileResponse(paths, index, null);
	}
	
	public BaseResponse getFile(String path,int index,String file) {
		String[] paths =  path.split("/");
		
        return getFileResponse(paths, index, file);
	
	}
	
	public BaseResponse uploadFile(String path,int index ,MultipartFile file) {
		BaseResponse response = new BaseResponse();
		String[] paths = path.split("/");
		String folderId = getFolderId(paths, index);
		if (folderId == null) {
			response.setError(true);
			response.setMessage("Some Error in fetching Folder Id");
			return response;
		}
		File fileMetadata = new File();
		fileMetadata.setName(file.getOriginalFilename());
		fileMetadata.setParents(Collections.singletonList(folderId));
		AbstractInputStreamContent mediaContent = null;
		try {	
			 mediaContent = new InputStreamContent(file.getContentType(), file.getInputStream());
			 mediaContent.setType(file.getContentType());
		}catch (IOException e) {
		  response.setError(true);
		  response.setMessage(e.getMessage());
		  
		}
		finally {
			try {
				file.getInputStream().close();
			} catch (IOException e) {
				 response.setError(true);
				 response.setMessage(e.getMessage());
				 
			}
		}
		
		try {
			if (mediaContent != null) {
				File actualFile = drive.files().create(fileMetadata, mediaContent).setFields("id, parents").execute();
				FileResponse resp = new FileResponse();
				com.docviewer.models.File fileResponse = new com.docviewer.models.File();
				fileResponse.setId(actualFile.getId());
				fileResponse.setName(fileMetadata.getName());
				fileResponse.setType(mediaContent.getType());
				resp.getFiles().add(fileResponse);
				resp.setMessage("File uploaded Successfullty");
				return resp;
			}
			
			
		}
		catch (Exception e) {
			response.setError(true);
			response.setMessage("Error in inserting file");
		}
		return response;
	}
	 
	public void addFileInresponse(String fileName,String fileId,HttpServletResponse response,BaseResponse baseResponse) {
		 
		try {
			
		    HttpResponse mediaresponse = drive.files().get(fileId).executeMedia();
		    if (null != mediaresponse) {
                InputStream instream = mediaresponse.getContent();
                response.setHeader("Content-Disposition", "attachment; filename="+fileName);
                response.setContentType(mediaresponse.getContentType());
                try {
                    int l;
                    byte[] tmp = new byte[2048];
                    while ((l = instream.read(tmp)) != -1) {
                        response.getOutputStream().write(tmp, 0, l);
                        
                    }
                  
                } finally {
                    instream.close();
                    response.getOutputStream().flush();
                }
                
                
            }
		}
		catch (Exception e) {
			response.setStatus(HttpStatus.SC_REQUEST_TIMEOUT);
			e.printStackTrace();
		
		}
	}
	
	
	private BaseResponse getFileResponse(String[] paths,int index,String fileName) {
	  	FileResponse response = new FileResponse();
	  	String folderId = "root";
    	try {
    	for(int i=index;i<paths.length;i++) {
    			 FileList result = drive.files().list()
    		        		.setQ("mimeType = 'application/vnd.google-apps.folder' and name = '"+paths[i]+"' and '"+folderId+"' in parents")
    		        		 .execute();
    			 if(result.isEmpty()) {
    				 response.setMessage("Not a valid folder name");  
    				 return response;
    				
    			 }
    			 else {
                      List<File> files = result.getFiles();
                      if(!files.isEmpty()) {
                    	  File  f= files.get(0);
                    	  folderId = f.getId();
                      }
                      else {
                    	  response.setMessage("Not a valid folder name");  
         				  return response;
                      }
    			 }
    		       		
    		}
    	    
    	   String query = "";
    	   if(fileName!=null) {
    		    query = "name = '"+fileName+"' and '"+folderId+"' in parents";
    	   }
    	   else {
    		 query = "'" +folderId+ "' in parents";   
    	   }
    	   FileList result = drive.files().list()
        		.setQ(query)
        		 .execute();
    		if(!result.isEmpty()) {
    			List<File> files = result.getFiles();
    			if(!files.isEmpty()) {
    				for(File file : files) {
    			    com.docviewer.models.File tmpFile = new com.docviewer.models.File();
          		     tmpFile.setId(file.getId());
          		     tmpFile.setName(file.getName());
          		     tmpFile.setType("application/vnd.google-apps.folder".equals(file.getMimeType())?FileType.Folder.name():FileType.File.name());
          		     
          		     response.getFiles().add(tmpFile);
          	  }
            }
    	}

    		
    	}
    	catch (Exception e) {
    		response.setError(true);
			response.setMessage("Google Server  is not reachible or some problem in api config "+e.getMessage());
			e.printStackTrace();
		}
     	return response;
	}
	
	private String getFolderId(String[] paths, int index) {
		String folderId = "root";
		try {
			for (int i = index; i < paths.length; i++) {
				FileList result = drive.files().list()
						.setQ("mimeType = 'application/vnd.google-apps.folder' and name = '" + paths[i] + "' and '"
								+ folderId + "' in parents")
						.execute();
				if (result.isEmpty()) {

					File fileMetadata = new File();
					fileMetadata.setName(paths[i]);
					fileMetadata.setMimeType("application/vnd.google-apps.folder");

					File file = drive.files().create(fileMetadata).setFields("id").execute();
					folderId = file.getId();
				} else {
					List<File> files = result.getFiles();
					if (!files.isEmpty()) {
						File f = files.get(0);
						folderId = f.getId();
					} else {
						File fileMetadata = new File();
						fileMetadata.setName(paths[i]);
						fileMetadata.setMimeType("application/vnd.google-apps.folder");
                            
						File file = drive.files().create(fileMetadata).setFields("id").execute();
						folderId = file.getId();

					}
				}

			}
		} catch (Exception e) {
			return null;
		}
		return folderId;

	}
	
	  
	
}
