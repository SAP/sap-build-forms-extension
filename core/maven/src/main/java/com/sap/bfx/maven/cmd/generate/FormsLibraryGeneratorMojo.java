package com.sap.bfx.maven.cmd.generate;

import com.sap.bfx.maven.cmd.AbstractFormsMojo;
import com.sap.bfx.maven.common.services.MixinService;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Mojo(name = "library-generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@Configuration
public class FormsLibraryGeneratorMojo extends AbstractFormsMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    /**
     * The metadata folder.
     */
    @Parameter(property = "metadataFolder", defaultValue = "${project.basedir}/src/main/metadata")
    String metadataFolder;

    @Parameter(property = "targetAccessClassFolder", defaultValue = "${project.basedir}/target/generated-sources")
    String tgtAccessClassFolder;

    // See <a href="https://maven.apache.org/guides/mini/guide-configuring-plugins.html">Configuration</a>
    @Parameter(property = "mixinPaths")
    Map<String, String> mixinPaths;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final var appContext = this.createApplicationContext();
        final var log = this.getLog();

        AnsiConsole.systemInstall();
        log.info("Forms Library-Generate starting");

        log.debug("Metadata Folder: " + metadataFolder);
        log.debug("Target AccessClass Folder: " + tgtAccessClassFolder);
        log.debug("Include Paths: " + ((mixinPaths != null) ? mixinPaths.toString() : "{}"));

        try {
            // Handle mixins
            final var mixinService = appContext.getBean(MixinService.class);
            final var params = new MixinService.ServiceParameters(project, metadataFolder, tgtAccessClassFolder,
                    mixinPaths);
            mixinService.execute(MixinService.MixinServiceType.StandaloneMixins, params);

        } catch (Exception e) {
            log.error(e);
            throw new MojoExecutionException(e);
        }

        this.getLog().info("Forms Library-Generate finished");
        AnsiConsole.systemUninstall();
    }
}
