package com.eagletsoft.boot.framework.common.dict;

import com.eagletsoft.boot.framework.cache.meta.Cache;
import com.eagletsoft.boot.framework.common.i18n.MessageMaker;
import com.eagletsoft.boot.framework.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryManager {

    @Autowired
    private MessageMaker messageMaker;

    private Map<String, Map<String, Object>> dictionary = new HashMap<>();

    private List<IDictionaryProvider> providers = new ArrayList<>();

    public void init(Resource[] resources) throws Exception {
        for (Resource res : resources) {
            String module = res.getFilename().split("\\.")[0];
            Map map = JsonUtils.createMapper().readValue(res.getInputStream(), HashMap.class);
            dictionary.put(module, map);
        }
    }

    public void addProvider(IDictionaryProvider provider) {
        this.providers.add(provider);
    }

    public BiMap<String, String> getDictMap(String uri) {
        List<Map> list = findDictInternal(uri);

        BiMap<String, String> ret = new BiMap<>();
        for (Map map : list) {
            ret.put(map.get("value").toString(), messageMaker.make(uri + "." + map.get("name").toString()));
        }
        return ret;
    }

    public void put(String module, Map map) {
        Map existingMap = dictionary.get(module);
        if (null == existingMap) {
            dictionary.put(module, map);
        }
        else {
            existingMap.putAll(map);
        }
    }


    private List<Map> findDictInternal(String uri) {
        String[] arr = uri.split("@", -1);
        Map<String, Object> moduleMap = dictionary.get(arr[0]);
        Object obj = moduleMap.get(arr[1]);
        if (obj instanceof String) {
            return this.findDictInternal(obj.toString());
        } else {
            return (List<Map>)obj;
        }
    }


    public List<Object> findDict(String uri) {
        List<Map> list = findDictInternal(uri);

        List<Object> ret = new ArrayList<>(list.size());
        for (Map map : list) {
            ret.add(map.get("value"));
        }
        return ret;
    }

    public Map getDictionary(String locale) {
        this.refresh(locale);
        Map<String, Map<String, List<Map>>> ret = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> e : dictionary.entrySet()) {
            Map<String, Object> moduleMap = e.getValue();

            Map<String, List<Map>> newModuleMap = new HashMap<>();
            ret.put(e.getKey(), newModuleMap);

            for (Map.Entry<String, Object> e2: moduleMap.entrySet()) {
                List<Map> list = this.findDictInternal(e.getKey() + "@" + e2.getKey());

                List<Map> newList = new ArrayList<>(list.size());

                newModuleMap.put(e2.getKey(), newList);

                for (Map m : list) {
                    Map newM = new HashMap();
                    newM.putAll(m);
                    String name = (String)m.get("name");
                    if (null != m.get("title")) {
                        newM.put("title", m.get("title"));
                    } else {
                        newM.put("title", messageMaker.makeWithNamespace(e.getKey(), e2.getKey() + "." + name));
                    }
                    newList.add(newM);
                }
            }
        }
        return ret;
    }

    protected void refresh(String locale) {
        for (IDictionaryProvider provider : providers) {
            this.put(provider.getModule(), provider.create(locale));
        }
    }

    public String getDictionaryString(String locale) {
        return JsonUtils.writeValue(this.getDictionary(locale));
    }

    public MessageMaker getMessageMaker() {
        return messageMaker;
    }

    public void setMessageMaker(MessageMaker messageMaker) {
        this.messageMaker = messageMaker;
    }

    public interface IDictionaryProvider {
        String getModule();
        Map<String, List<Map>> create(String locale);
    }
}
