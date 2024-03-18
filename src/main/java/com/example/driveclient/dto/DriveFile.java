package com.example.driveclient.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;


@Data
@Builder
public class DriveFile {
    private Long id;
    private String name;
    private Long size;
    private String modificationDate;
    private String owner;
}
