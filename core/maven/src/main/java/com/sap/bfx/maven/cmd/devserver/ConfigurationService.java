package com.sap.bfx.maven.cmd.devserver;

import lombok.Data;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.springframework.stereotype.Service;

@Service
@Data
public class ConfigurationService {

    private MavenProject project;
    private String metadataFolder;
    private Log log;
}
