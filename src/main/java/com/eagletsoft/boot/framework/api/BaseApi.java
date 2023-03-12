package com.eagletsoft.boot.framework.api;

import com.eagletsoft.boot.framework.api.utils.ApiResponse;
import com.eagletsoft.boot.framework.common.security.meta.Access;
import com.eagletsoft.boot.framework.data.entity.Entity;
import com.eagletsoft.boot.framework.data.filter.PageSearch;
import com.eagletsoft.boot.framework.data.service.ICRUDService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.Serializable;

public abstract class BaseApi<T extends Entity, SAVE, UPDATE> {

    protected abstract ICRUDService<T> getService();

    @Access(":read")
    @RequestMapping(value = "/{id}/read",method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Object read(@PathVariable Serializable id){
        return ApiResponse.make(this.getService().find(id));
    }

    @Access(":write")
    @RequestMapping(value = "/create",method = {RequestMethod.POST})
    public @ResponseBody Object create(@RequestBody @Valid SAVE obj){
        return ApiResponse.make(this.getService().create(obj));
    }

    @Access(":write")
    @RequestMapping(value = "/{id}/remove",method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Object remove(@PathVariable Serializable id){
        this.getService().delete(id);
        return ApiResponse.make();
    }

    @Access(":write")
    @RequestMapping(value = "/{id}/update",method = {RequestMethod.POST})
    public @ResponseBody Object update(@PathVariable Serializable id, @Valid @RequestBody UPDATE obj){
        return ApiResponse.make(this.getService().update(id, obj));
    }

    @Access(":read")
    @RequestMapping(value = "/search",method = {RequestMethod.POST})
    public @ResponseBody Object search(@RequestBody @Valid PageSearch pageRequest){
        return ApiResponse.make(this.getService().search(pageRequest));
    }
}
