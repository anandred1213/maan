package com.practise.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class FileUploadRequest {

    @Schema(type = "string", format = "binary", description = "Image file to upload")
    public byte[] file;
}
