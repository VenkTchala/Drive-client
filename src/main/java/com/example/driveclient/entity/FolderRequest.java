package com.example.driveclient.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class FolderRequest {
    private String parentPath ;
    private String folderName ;
    private Long size;
}
