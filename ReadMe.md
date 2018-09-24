This is a Spring Boot application runs on embeded Server .
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

