package com.huangpf.util.data;

import java.io.Serializable;
import java.util.Map;

public abstract interface IData extends Map<String, Object>, Serializable {
    public abstract boolean isNoN(String paramString);

    public abstract String[] getNames();

    public abstract String getString(String paramString);

    public abstract String getString(String paramString1, String paramString2);

    public abstract boolean getBoolean(String paramString);

    public abstract boolean getBoolean(String paramString, boolean paramBoolean);

    public abstract int getInt(String paramString);

    public abstract int getInt(String paramString, int paramInt);

    public abstract long getLong(String paramString);

    public abstract long getLong(String paramString, long paramLong);

    public abstract double getDouble(String paramString);

    public abstract double getDouble(String paramString, double paramDouble);

    public abstract IDataset getDataset(String paramString);

    public abstract IDataset getDataset(String paramString, IDataset paramIDataset);

    public abstract IData getData(String paramString);

    public abstract IData getData(String paramString, IData paramIData);

    public abstract IData subData(String paramString) throws Exception;

    public abstract IData subData(String paramString, boolean paramBoolean) throws Exception;

    public abstract String toString();
}
