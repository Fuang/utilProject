package com.huangpf.util.data.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.huangpf.util.data.IData;
import com.huangpf.util.data.IDataset;

public class DataMap extends HashMap<String, Object> implements IData {
    private static final long serialVersionUID = 5728540280422795959L;

    public DataMap() {
    }

    public DataMap(int size) {
        super(size);
    }

    public DataMap(Map<String, Object> map) {
        super(map);
    }

    public DataMap(String jsonObject) {
        JSONObject map = JSONObject.fromObject(jsonObject);
        if (map != null) {
            putAll(fromJSONObject(map));
        }
    }

    public static DataMap fromJSONObject(JSONObject object) {
        if (object != null) {
            DataMap data = new DataMap();
            Iterator<?> keys = object.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object value = object.get(key);

                if (value != null) {
                    if ((value instanceof JSONObject)) {
                        data.put(key, new DataMap((JSONObject) value));
                    } else if ((value instanceof JSONArray)) {
                        data.put(key, new DatasetList((JSONArray) value));
                    } else if ((value instanceof String)) {
                        data.put(key, value);
                    } else {
                        data.put(key, value);
                    }
                } else {
                    data.put(key, value);
                }
            }
            return data;
        }
        return null;
    }

    public Object get(String key) {
        Object value = super.get(key);
        if (null == value)
            return null;
        return value;
    }

    @Override
    public String[] getNames() {
        String[] names = new String[size()];
        Iterator<String> keys = keySet().iterator();
        int index = 0;
        while (keys.hasNext()) {
            names[index] = (keys.next());
            index++;
        }
        return names;
    }

    @Override
    public boolean isNoN(String name) {
        return (name == null) || (!containsKey(name));
    }

    @Override
    public String getString(String name) {
        Object value = get(name);
        if (value == null)
            return null;
        return value.toString();
    }

    @Override
    public String getString(String name, String defaultValue) {
        String value = getString(name);
        if (value == null)
            return defaultValue;
        return value;
    }

    @Override
    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        Object value = get(name);
        if (null == value)
            return defaultValue;
        return "true".equalsIgnoreCase(value.toString());
    }

    @Override
    public double getDouble(String name) {
        return getDouble(name, 0.0D);
    }

    @Override
    public double getDouble(String name, double defaultValue) {
        Object value = get(name);
        if (value == null) {
            return defaultValue;
        }
        return Double.parseDouble(value.toString());
    }

    @Override
    public int getInt(String name) {
        return getInt(name, 0);
    }

    @Override
    public int getInt(String name, int defaultValue) {
        Object value = get(name);
        if (value == null)
            return defaultValue;
        return Integer.parseInt(value.toString());
    }

    @Override
    public long getLong(String name) {
        return getLong(name, 0L);
    }

    @Override
    public long getLong(String name, long defaultValue) {
        Object value = get(name);
        if (value == null)
            return defaultValue;
        return Long.parseLong(value.toString());
    }

    @Override
    public IData getData(String name) {
        Object value = get(name);
        if (value == null) {
            return null;
        }
        if ((value instanceof IData))
            return (IData) value;
        if ((value instanceof String)) {
            return new DataMap((String) value);
        }
        return null;
    }

    @Override
    public IData getData(String name, IData def) {
        Object value = get(name);
        if (value == null) {
            return def;
        }
        if ((value instanceof IData))
            return (IData) value;
        if ((value instanceof String)) {
            return new DataMap((String) value);
        }
        return def;
    }

    @Override
    public IDataset getDataset(String name, IDataset def) {
        Object value = get(name);
        if (value == null) {
            return def;
        }
        if ((value instanceof IDataset))
            return (IDataset) value;
        if ((value instanceof JSONArray)) {
            IDataset ds = new DatasetList((JSONArray) value);
            return ds;
        }
        return def;
    }

    @Override
    public IDataset getDataset(String name) {
        return getDataset(name, null);
    }

    @Override
    public IData subData(String group) throws Exception {
        return subData(group, false);
    }

    @Override
    public IData subData(String group, boolean istrim) throws Exception {
        IData element = new DataMap();

        String[] names = getNames();
        String prefix = group + "_";
        for (String name : names) {
            if (name.startsWith(prefix)) {
                element.put(istrim ? name.substring(prefix.length()) : name, get(name));
            }
        }

        return element;
    }

    public String put(String key, String value) {
        return (String) super.put(key, value);
    }

    public IData put(String key, IData value) {
        return (IData) super.put(key, value);
    }

    public IDataset put(String key, IDataset value) {
        return (IDataset) super.put(key, value);
    }

}
