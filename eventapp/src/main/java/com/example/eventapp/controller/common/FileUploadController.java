package com.example.eventapp.controller.common;

import com.example.eventapp.services.cloud.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/v1/common")
public class FileUploadController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(@RequestParam("file") MultipartFile file) {
        try {
            
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinaryService.uploadFile(file, "manager-media");
            return ResponseEntity.ok().body(uploadResult);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }


    @GetMapping("/video-project")
    public String getMethodName(@RequestParam String param) {
        return new String();
    }
    
}
