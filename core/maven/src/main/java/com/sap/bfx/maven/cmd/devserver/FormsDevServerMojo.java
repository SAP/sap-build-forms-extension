package com.sap.bfx.maven.cmd.devserver;

import com.sap.bfx.maven.common.AppConfig;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.Servlet;
import java.io.InputStreamReader;

@Mojo(name = "serve", requiresDependencyResolution = ResolutionScope.COMPILE)
public class FormsDevServerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    /**
     * The metadata folder.
     */
    @Parameter(property = "metadataFolder", defaultValue = "${project.basedir}/src/main/metadata")
    String metadataFolder;
    private Server server;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.getLog().info("Starting dev server");

        try {
            server = new Server();
            final var connector = new ServerConnector(server);
            connector.setPort(8090);
            server.setConnectors(new Connector[]{connector});

            ServletContextHandler handler = new ServletContextHandler();
            handler.setContextPath("/");

            final var dispatchServletHolder = new ServletHolder(DispatcherServlet.class);
            dispatchServletHolder.setInitParameter("contextConfigLocation", AppConfig.class.getName());
            dispatchServletHolder.setInitParameter("contextClass",
                    AnnotationConfigWebApplicationContext.class.getName());
            handler.addServlet(dispatchServletHolder, "/api/*");

            final var uiServletHolder = new ServletHolder(UiServerServlet.class);
            handler.addServlet(uiServletHolder, "/*");

            server.setHandler(handler);
            server.start();

            final var appContext = ((DispatcherServlet) dispatchServletHolder.getServlet()).getWebApplicationContext();
            final var cfgService = appContext.getBean(ConfigurationService.class);
            cfgService.setMetadataFolder(this.metadataFolder);
            cfgService.setLog(this.getLog());
            cfgService.setProject(project);

            // block until "q" is entered
            final var input = new InputStreamReader(System.in);
            for (; ; ) {
                char c = (char) input.read();

                if (c == 'q') {
                    break;
                }
            }
        } catch (Exception e) {
            this.getLog().error(e);
        }
    }

    public static class DispatcherServletHolder extends ServletHolder {

        DispatcherServletHolder() {
            super(DispatcherServlet.class);
        }

        @Override
        protected Servlet newInstance() throws Exception {
            // Load Spring web application configuration
            final var appContext = new AnnotationConfigWebApplicationContext();
            appContext.register(AppConfig.class);

            return new DispatcherServlet(appContext);
        }
    }
}

/*

File base = new File(System.getProperty("java.io.tmpdir"));
    Context context = tomcat.addContext("", base.getAbsolutePath());

    AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
    appContext.register(SpringConfig.class);
    appContext.refresh();

    DispatcherServlet dispatcherServlet = new DispatcherServlet(appContext);
    Wrapper wrapper = context.createWrapper();
    wrapper.setName("dispatcherServlet");
    wrapper.setServlet(dispatcherServlet);
    context.addChild(wrapper);
    wrapper.setLoadOnStartup(1);
    wrapper.addMapping("/");

 */