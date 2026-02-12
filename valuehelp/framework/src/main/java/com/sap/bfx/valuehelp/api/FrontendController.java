package com.sap.bfx.valuehelp.api;

import com.sap.bfx.api.AbstractFrontendController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class FrontendController extends AbstractFrontendController {

    @Autowired
    public FrontendController(ApplicationContext ctx) {
        super(ctx);
    }
}