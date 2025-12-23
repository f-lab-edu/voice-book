package com.sj.voicebook.member.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String upload(MultipartFile file);
    String getImageUrl(String imageKey);
}
