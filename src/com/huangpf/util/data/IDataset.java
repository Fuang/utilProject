package com.huangpf.util.data;

import java.io.Serializable;
import java.util.List;

public abstract interface IDataset extends List<Object>, Serializable {
    public static final int ORDER_ASCEND = 0;// 排序升序

    public static final int ORDER_DESCEND = 1;// 排序降序

    public static final int TYPE_STRING = 2;// 按字符串类型排序

    public static final int TYPE_INTEGER = 3;// 按数字类型排序

    public static final int TYPE_DOUBLE = 4;// 按double类型排序

    public static final int MAX_RECORD = 2000;

    public abstract String[] getNames();

    public abstract Object get(int paramInt);

    public abstract Object get(int paramInt, String paramString);

    public abstract Object get(int paramInt, String paramString, Object paramObject);

    public abstract IData getData(int paramInt);

    public abstract IDataset getDataset(int paramInt);

    public abstract IData first();

    public abstract IData toData();
}
