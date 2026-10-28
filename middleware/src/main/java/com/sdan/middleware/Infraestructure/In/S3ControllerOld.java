package com.sdan.middleware.Infraestructure.In;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

//@RestController
//public class S3ControllerOld {
//    @Autowired
//    private S3ServiceOld s3ServiceOld;
//
//    @PostMapping("/upload")
//    public ResponseEntity<String> upload(@RequestParam("file")MultipartFile file)throws IOException {
//        s3ServiceOld.uploadFile(file);
//        return ResponseEntity.ok("File uploaded succesfuly");
//    }
//
//    @GetMapping("/download/{filename}")
//    public ResponseEntity<byte[]> download(@PathVariable String filename) {
//        byte[] data = s3ServiceOld.downloadFile(filename);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
//                .body(data);
//    }
//
//}