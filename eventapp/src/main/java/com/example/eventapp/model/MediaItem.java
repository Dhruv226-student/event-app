package com.example.eventapp.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaItem{

    private String url;        // 🌐 Cloudinary URL
    private String publicId;   // 🧩 Needed for deletion
    private String type;       // image or video
}
