package com.sap.bfx.maven.cmd.devserver;

import org.springframework.http.MediaType;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TestServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType(MediaType.TEXT_PLAIN_VALUE);
        res.getOutputStream().println("Hallo");
    }
}
