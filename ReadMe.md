This is a Spring Boot application runs on embeded tomcat Server .
This is using Google drive API to upload and download the File.

List all the files inside a folder testfolder
GET Method
localhost:8090/files/list/testfolder

List all the files inside a folder child1 which is inside testfolder
GET Method
localhost:8090/files/list/testfolder/child1

Download a file testfile.png which is inside testfolder
GET method
localhost:8090/files/list/testfolder?file = testfile.png

Upload a file inside testfolder
Url
localhost:8090/files/list/testfolder
POST Method - use multipart file upload

while uploading the file inside a folder if folder does not exists it will create that folder.

Clone the application 
import in STS or eclipse 
build gradle because all the dependecies are not in pom.xml
run com.docviewer.Application it has main method 

It has FileController contains three methods
 to upload download and list files .
 
 


