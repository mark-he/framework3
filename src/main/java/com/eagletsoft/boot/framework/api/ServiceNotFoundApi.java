package com.eagletsoft.boot.framework.api;

import com.eagletsoft.boot.framework.common.errors.ServiceException;
import com.eagletsoft.boot.framework.common.errors.StandardErrors;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/")
public class ServiceNotFoundApi {
    //@RequestMapping(value = "/**",method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Object invalid(){
        throw new ServiceException(StandardErrors.UNAVAILABLE);
    }
}
