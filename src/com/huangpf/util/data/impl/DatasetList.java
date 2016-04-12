package com.huangpf.util.data.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.huangpf.util.data.IData;
import com.huangpf.util.data.IDataset;

public class DatasetList extends ArrayList<Object> implements IDataset {
    private static final long serialVersionUID = 8302984775243577040L;

    /**
     * 初始化
     */
    public DatasetList() {
        super(20);
    }

    public DatasetList(int size) {
        super(size);
    }

    public DatasetList(IData data) {
        super(20);
        add(data);
    }

    public DatasetList(IData[] datas) {
        super(20);
        for (IData data : datas) {
            add(data);
        }
    }

    public DatasetList(IDataset list) {
        super(20);
        addAll(list);
    }

    public DatasetList(String jsonArray) {
        super(20);
        Collection<?> collection = JSONArray.toCollection(JSONArray.fromObject(jsonArray));
        addAll(collection);
    }

    public DatasetList(JSONArray array) {
        super(20);
        Collection<?> collection = JSONArray.toCollection(array);
        addAll(collection);
    }

    public String[] getNames() {
        return size() > 0 ? ((IData) get(0)).getNames() : null;
    }

    @Override
    public Object get(int index) {
        return super.get(index);
    }

    public Object get(int index, String name) {
        Object data = get(index);
        if (null == data) {
            return null;
        }
        if ((data instanceof Map<?, ?>)) {
            IData map = new DataMap();
            map.putAll((HashMap<String, Object>) data);
            return map.get(name);
        }

        return null;
    }

    public Object get(int index, String name, Object def) {
        Object value = get(index, name);
        return value == null ? def : value;
    }

    public IData getData(int index) {
        Object value = get(index);
        if (value == null)
            return null;
        if ((value instanceof String))
            return new DataMap((String) value);
        if ((value instanceof JSONObject)) {
            IData data = DataMap.fromJSONObject((JSONObject) value);
            return data;
        }
        return (IData) value;
    }

    public IDataset getDataset(int index) {
        Object value = get(index);
        if (value == null) {
            return null;
        }
        if ((value instanceof String))
            return new DatasetList((String) value);
        if ((value instanceof JSONArray)) {
            return new DatasetList((JSONArray) value);
        }
        return (IDataset) value;
    }

    public IData first() {
        return size() > 0 ? (IData) get(0) : null;
    }

    public IData toData() {
        IData data = new DataMap();

        Iterator<Object> it = iterator();
        while (it.hasNext()) {
            IData element = (IData) it.next();
            Iterator<String> iterator = element.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (data.containsKey(key)) {
                    IDataset list = (IDataset) data.get(key);
                    list.add(element.get(key));
                } else {
                    IDataset list = new DatasetList();
                    list.add(element.get(key));
                    data.put(key, list);
                }
            }
        }

        return data;
    }

}
