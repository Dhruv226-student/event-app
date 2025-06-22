package com.example.eventapp.services.cloud;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadFile(MultipartFile file, String folder) throws IOException {
        return (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "auto",     // handles image/video
                "overwrite", true
        ));
    }

        // ✅ For videos
   @SuppressWarnings("unchecked")
   public Map<String, Object> uploadFile(MultipartFile file, String folder, String resourceType) throws IOException {
    return (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
            "folder", folder,
            "resource_type", resourceType,
            "overwrite", true
    ));
}

public String deleteFile(String publicId) throws IOException {
    // Try deleting as image first
    Map<String, Object> imageOptions = new HashMap<>();
    @SuppressWarnings("unchecked")
    Map<String, Object> imageResult = (Map<String, Object>) cloudinary.uploader().destroy(publicId, imageOptions);

    String resultStatus = (String) imageResult.get("result");

    if ("ok".equals(resultStatus)) {
        return "ok";
    }

    // If not found, try as video
    if ("not found".equals(resultStatus)) {
        Map<String, Object> videoOptions = new HashMap<>();
        videoOptions.put("resource_type", "video");

        @SuppressWarnings("unchecked")
        Map<String, Object> videoResult = (Map<String, Object>) cloudinary.uploader().destroy(publicId, videoOptions);
        String videoStatus = (String) videoResult.get("result");

        if (!"ok".equals(videoStatus)) {
            System.err.println("Cloudinary delete failed for both image and video: " + publicId + " | Result: " + videoStatus);
        }

        return videoStatus;
    }

    // Log unexpected cases (e.g., error)
    System.err.println("Cloudinary delete failed: " + publicId + " | Result: " + resultStatus);
    return resultStatus;
}



}
