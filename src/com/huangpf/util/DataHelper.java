package com.huangpf.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.huangpf.util.data.IData;
import com.huangpf.util.data.IDataset;
import com.huangpf.util.data.impl.DataMap;
import com.huangpf.util.data.impl.DatasetList;
import com.huangpf.util.execptions.Thrower;

public class DataHelper {
    public static final String STR_SEPARATOR = ",";

    // 默认除法运算精度
    private static final int DEFAULT_SCALE = 2;

    private static final String[] ZERO_SEQ = { "", "0", "00", "000", "0000", "00000", "000000", "0000000", "00000000" };

    private static final int[] TEN_POW_SEQ = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000 };

    private static final ThreadLocal<Map<String, DecimalFormat>> decimalFormatMap = new ThreadLocal<Map<String, DecimalFormat>>() {
        @Override
        protected java.util.Map<String, DecimalFormat> initialValue() {
            return new ConcurrentHashMap<String, DecimalFormat>();
        }
    };

    /**
     * 提供精确的加法运算
     * 
     * @param v1
     *            被加数
     * @param v2
     *            加数
     * @return 两个参数的和
     */
    public static double addForDou(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * 提供精确的加法运算
     * 
     * @param v1
     *            被加数
     * @param v2
     *            加数
     * @return 两个参数的和
     */
    public static String addForDou(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.add(b2).toString();
    }

    /**
     * 提供（相对）精确的除法运算，当放生除不尽的情况时，精确到小数点以后10位，以后的四舍五入
     * 
     * @param v1
     *            被除数
     * @param v2
     *            除数
     * @return 两个除数的商
     */
    public static double divForDou(double v1, double v2) {
        return divForDou(v1, v2, DEFAULT_SCALE);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指定精度，以后的数字四舍五入
     * 
     * @param v1
     *            被除数
     * @param v2
     *            除数
     * @param scale
     *            表示需要精确到小数点以后几位
     * @return 两个参数的商
     */
    public static double divForDou(double v1, double v2, int scale) {
        if (scale < 0) {
            Thrower.throwException(AcctCompException.ACCT_COMP_ERROR_MSG, "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 提供精确的小数位四舍五入
     * 
     * @param v
     *            需要四舍五入的数位
     * @param scale
     *            小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(double v, int scale) {
        // 注意不可直接使用double，否则可能出现负数舍入不正确
        BigDecimal b = new BigDecimal(Double.toString(v));
        return b.setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 提供精确的小数位四舍五入
     * 
     * @param String
     *            v 需要四舍五入的数位
     * @param int scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static String round(String v, int scale) {
        // 注意不可直接使用double，否则可能出现负数舍入不正确
        BigDecimal b = new BigDecimal(v);
        return b.setScale(scale, RoundingMode.HALF_UP).toString();
    }

    /**
     * @remark 针对Idataset返回一条记录,获取字段值，若结果集为空则返回null
     * @fieldName 需要获取得key名称
     * @return String
     * @throws Exception
     */

    public static String getStrOfDataset(IDataset ds, String fieldName) throws Exception {
        if (ds == null || ds.isEmpty())
            return null;
        return ds.first().getString(fieldName);
    }

    /**
     * 从结果集中的第一条记录中获取整形字段值，若结果集为空则抛出异常
     * 
     * @param ds
     * @param fieldName
     * @return int
     * @throws Exception
     */
    public static int getIntOfDataset(IDataset ds, String fieldName) throws Exception {
        if (ds == null || ds.size() == 0)
            Thrower.throwException(AcctCompException.ACCT_COMP_RESULT_EMPTY);
        return ds.first().getInt(fieldName);
    }

    /**
     * 根据字段名和字段的值筛选符合条件的记录
     * 
     * @param source
     * @param key
     * @param value
     * @return
     */
    public static IDataset filter(IDataset source, String key, String value) {
        return filter(source, new String[] { key }, new String[] { value });
    }

    /**
     * 根据字段名和字段的值筛选符合条件的记录
     * 
     * @param source
     * @param keys
     * @param values
     * @return
     * @author
     */
    public static IDataset filter(IDataset source, String[] keys, String[] values) {
        return filterWithType(source, keys, values, true);
    }

    private static IDataset filterWithType(IDataset source, String[] keys, String[] values, boolean filterEquals) {
        if (keys.length != values.length) {
            Thrower.throwException(AcctCompException.INPUTPARAMS_NOT_MATCH);
        }

        int length = keys.length;

        if (length == 0)
            return source;

        IDataset result = new DatasetList();
        Iterator<Object> it = source.iterator();

        OUTER_LABEL: while (it.hasNext()) {
            IData one = (IData) it.next();

            for (int i = 0; i < length; i++) {
                if (values[i].equals(one.getString(keys[i])) != filterEquals)
                    continue OUTER_LABEL;
            }

            result.add(one);
        }

        return result;
    }

    /**
     * 根据字段名和字段的值筛选不符合条件的记录
     * 
     * @param source
     * @param key
     * @param value
     * @return
     */
    public static IDataset unequalsFilter(IDataset source, String key, String value) {
        return unequalsFilter(source, new String[] { key }, new String[] { value });
    }

    /**
     * 根据字段名和字段的值筛选不符合条件的记录
     * 
     * @param source
     * @param keys
     * @param values
     * @return
     * @author
     */
    public static IDataset unequalsFilter(IDataset source, String[] keys, String[] values) {
        return filterWithType(source, keys, values, false);
    }

    /**
     * 从结果集中选择字段名对应的字段组成新的结果集
     * 
     * @param source
     * @param keys
     * @return IDataset
     */
    public static IDataset select(IDataset source, String keys) throws Exception {
        IDataset result = new DatasetList();

        String[] keyArray = keys.split(",");

        OUTER_LABEL: for (int i = 0; i < source.size(); i++) {
            IData item = source.getData(i);

            IData newItem = new DataMap();
            for (String key : keyArray) {
                if (item.containsKey(key))
                    newItem.put(key, item.get(key));
                else
                    continue OUTER_LABEL;
            }

            result.add(newItem);
        }

        return result;
    }

    /**
     * 查找.从IDataset中查找字段名为key，其值为value的IData
     * 
     * @param source
     * @param key
     * @param value
     * @return IData
     */
    public static IData locate(IDataset source, String key, String value) {
        return locate(source, new String[] { key }, new String[] { value });
    }

    /**
     * 查找 从IDataset中查找字段名为key，其值为value的IData
     * 
     * @param source
     * @param keys
     * @param values
     * @return IData
     */
    public static IData locate(IDataset source, String[] keys, String[] values) {
        if (keys.length != values.length) {
            Thrower.throwException(AcctCompException.INPUTPARAMS_NOT_MATCH);
        }

        int length = keys.length;

        if (length == 0)
            return source.getData(0);

        Iterator<Object> it = source.iterator();

        OUTER_LABEL: while (it.hasNext()) {
            IData one = (IData) it.next();

            for (int i = 0; i < length; i++) {
                if (!values[i].equals(one.getString(keys[i])))
                    continue OUTER_LABEL;
            }

            return one;
        }

        return null;
    }

    /**
     * 按照指定的key把idataset转化成HashMap
     * 
     * @param ds 的对象必须为IData
     * @param key
     * @return IData
     * @author
     */
    public static IData convert2Map(IDataset ds, String key) throws Exception {
        return convert2Map(ds, new String[] { key });
    }

    /**
     * 按照指定的key把idataset转化成HashMap
     * 
     * @param ds 的对象必须为IData
     * @param key
     * @return IData
     * @author
     */
    public static IData convert2Map(IDataset ds, String[] keyArr) throws Exception {
        IData map = new DataMap();
        for (int i = 0; i < ds.size(); ++i) {
            IData data = ds.getData(i);
            String value = getJoinedValueByCodes(data, keyArr);
            map.put(value, data);
        }
        return map;
    }

    /**
     * 从IDataset里面找key对应的内容，如果和value相同，就返回目前这个IData
     * 
     * @param datas
     * @param key
     * @param value
     * @return
     * @author chenjw
     */
    public static IData getTheData(IDataset datas, String key, String value) {
        for (int i = 0; i < datas.size(); i++) {
            IData data = datas.getData(i);
            if (data.containsKey(key) && data.getString(key).equals(value)) {
                return data;
            }
        }
        return null;
    }

    /**
     * 在IDataset中删除键为key，值为空的元素
     * 
     * @param datas IDataset数据对象
     * @param key
     * @author
     */
    public static void removeEmptyElement(IDataset datas, String key) throws Exception {
        if (datas == null || datas.size() == 0)
            return;
        for (int i = 0; i < datas.size(); i++) {
            IData data = datas.getData(i);
            if (null == data.getString(key)) {
                datas.remove(i--);
            }
        }
    }

    /**
     * 返回列表中指定的 fromIndex（包括 ）和 toIndex（不包括）之间的部分视图。（如果 fromIndex 和 toIndex
     * 相等，则返回的列表为空）。
     * 
     * @param source IDataset数据对象
     * @param fromIndex
     * @param toIndex
     * @return IDataset
     */
    public static IDataset subList(IDataset source, int fromIndex, int toIndex) {
        if (fromIndex == toIndex)
            return null;

        if (fromIndex < 0 || toIndex > source.size() || fromIndex > toIndex)
            throw new IndexOutOfBoundsException("fromIndx:" + fromIndex + ",size:" + source.size() + ",toIndex:" + toIndex);

        IDataset result = new DatasetList();
        for (int i = fromIndex; i < toIndex; i++) {
            result.add(source.get(i));
        }

        return result;
    }

    /**
     * 从集合中获得code到name的映射，适用于将code转为name的时候 以STR_SEPARATOR分割
     * 支持多个关键字对应一个名字 若多个key 建议使用getStrByCodes从data中获取keys的String
     * 
     * @param ds code与name对应的集合
     * @param codeKey 集合中code的Keys 以","分隔
     * @param nameKey 集合中name的Key
     * @return IData
     */
    public static IData getCodeNameMap(IDataset ds, String codeKeys, String nameKey) {
        IData data = new DataMap();
        IData perData = null;

        String[] codeKeyArr = codeKeys.split(STR_SEPARATOR);
        if (ds != null && !ds.isEmpty()) {
            for (int i = 0; i < ds.size(); i++) {
                perData = ds.getData(i);
                String key = getJoinedValueByCodes(perData, codeKeyArr);
                data.put(key, perData.get(nameKey));
            }
        }
        return data;
    }

    public static String join(Object[] valueArr) {
        return StringUtils.join(valueArr, STR_SEPARATOR);
    }

    public static String join(Object[] valueArr, String separator) {
        return StringUtils.join(valueArr, separator);
    }

    /**
     * 从IData中获取keys所对应的values的String形式 其value以STR_SEPARATOR分割
     * 配合 getCodeNameMap 来使用
     * 
     * @param data IData数据对象
     * @param codeKeys
     * @return String
     */
    public static String getJoinedValueByCodes(IData data, String codeKeys) {
        String[] codeKeyArr = codeKeys.split(STR_SEPARATOR);
        return getJoinedValueByCodes(data, codeKeyArr);
    }

    public static String getJoinedValueByCodes(IData data, String[] codeKeyArr) {
        if (codeKeyArr.length == 1) {
            return data.getString(codeKeyArr[0]);
        }
        StringBuilder strBuilder = new StringBuilder(20);
        for (int j = 0; j < codeKeyArr.length; j++) {
            if (j > 0)
                strBuilder.append(STR_SEPARATOR);

            strBuilder.append(data.getString(codeKeyArr[j]));
        }

        return strBuilder.toString();
    }

    public static IData convert2ListMap(IDataset ds, String key) {
        IData map = new DataMap();
        for (int i = 0; i < ds.size(); ++i) {
            IData data = ds.getData(i);
            String value = getJoinedValueByCodes(data, new String[] { key });
            if (map.containsKey(value)) {
                ((IDataset) map.get(value)).add(data);
            } else {
                IDataset tempDs = new DatasetList();
                tempDs.add(data);
                map.put(value, tempDs);
            }
        }
        return map;
    }

    /**
     * 从IDataset里面找key对应的内容，如果和value相同，就返回包涵这个IData的新的IDataset
     * 
     * @param datas
     * @param key
     * @param value
     * @return
     * @author lif
     */
    public static IDataset getTheDataset(IDataset datas, String key, String value) {
        IDataset dataset = new DatasetList();
        for (int i = 0; i < datas.size(); i++) {
            IData data = datas.getData(i);
            if (data.containsKey(key) && data.getString(key).equals(value)) {
                dataset.add(data);
            }
        }
        return dataset;
    }

    /**
     * 从IDataset里面找key对应的内容，如果和value相同，就返回目前这个IData里面 column对应的那列值
     * 
     * @param datas
     * @param key
     * @param value
     * @param column
     * @return
     */
    public static String getTheDataValue(IDataset datas, String key, String value, String column) {

        IData data = getTheData(datas, key, value);
        return data == null ? "" : data.getString(column);
    }

    /**
     * 分析数据中的所有出现的key对应value，组成一个不重复的List
     * 
     * @param datas
     * @param key
     * @return List
     * @throws Exception
     */
    public static Set<String> getDistinctValues(IDataset datas, String key) {
        Set<String> set = new HashSet<String>();
        for (int i = 0, size = datas.size(); i < size; i++) {
            IData data = datas.getData(i);
            if (!Validator.isEmpty(data, key)) {
                set.add(data.getString(key));
            }
        }
        return set;
    }

    /**
     * 从datas里面取出指定列的数据，形成一个IDataset，如果没有该值，就是用默认值
     * 
     * @param datas
     * @param keys
     * @return
     * @throws Exception
     * @author chenjw
     */
    public static IDataset spliceIDataset(IDataset datas, String[] keys, String defaultValue) throws Exception {
        IDataset outParams = new DatasetList();
        for (int i = 0; i < datas.size(); i++) {
            IData data = datas.getData(i);
            IData outParam = new DataMap();
            for (int j = 0; j < keys.length; j++) {
                DataHelper.setParam(outParam, keys[j], data, defaultValue);
            }
            outParams.add(outParam);
        }
        return outParams;
    }

    /**
     * 将datas中含有特定键值对作为一个新的IDataset返回
     * 
     * @param datas
     * @param keys
     */
    public static IDataset cloneIDataset(IDataset datas, String... keys) throws Exception {
        IDataset out = datas.getClass().newInstance();
        for (int j = 0; j < datas.size(); j++) {
            IData data = datas.getData(j);
            IData outParam = new DataMap();
            for (int i = 0; i < keys.length; i++) {
                DataHelper.setParam(outParam, keys[i], data);
            }
            out.add(outParam);
        }
        return out;
    }

    /**
     * 从datas中取出键值对中，键为key的IData，加入到一个新的IDataset中并返回
     * 
     * @param datas
     * @param key
     * @return
     * @throws Exception
     */
    public static IDataset spliceIDataset(IDataset datas, String key) throws Exception {
        IDataset outParams = new DatasetList();
        for (int j = 0; j < datas.size(); j++) {
            IData data = datas.getData(j);
            IData outParam = new DataMap();
            if (!Validator.isEmpty(data, key)) {
                DataHelper.setParam(outParam, key, data);
                outParams.add(outParam);
            }
        }
        return outParams;
    }

    // add by lif 2010-6-24 16:24:11 add

    /**
     * 对ds中指定的若干列有则修改（将其对应的值格式化0.00后做修改）无则新增（对象中增加columns和""键值对）
     * 
     * @param ds
     * @param columns
     */
    public static void div100DatasetSpecial(IDataset ds, String[] columns) {
        for (int i = 0; i < ds.size(); i++) {
            IData tmpData = ds.getData(i);
            for (int j = 0; j < columns.length; j++) {
                if (Validator.isEmpty(tmpData, columns[j])) {
                    tmpData.put(columns[j], "");
                } else
                    tmpData.put(columns[j], div100(tmpData.getString(columns[j])));
            }
        }
    }

    /**
     * 从datas里面，把key对应的内容为null的那几行记录删除
     * 
     * @param datas
     * @param key
     */
    public static void removeBlankObj(IDataset datas, String key) {
        for (int i = 0; i < datas.size(); i++) {
            IData data = datas.getData(i);
            if (Validator.isEmpty(data, key)) {
                datas.remove(data);
            }
        }
    }

    /**
     * 从IDataset里面找出所有与key对应的内容并且和value相同，形成一个IDataset
     * 
     * @param datas
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    public static IDataset spliceIData(IDataset datas, String key, String value) throws Exception {
        IDataset outParams = new DatasetList();
        for (int i = 0; i < datas.size(); i++) {
            IData data = datas.getData(i);
            if (value.equals(data.getString(key))) {
                IData outParam = new DataMap();
                outParam.putAll(data);
                outParams.add(outParam);
            }

        }
        return outParams;
    }

    /**
     * 如果目标IData里面没有该键值或为null或为"",则抛出异常
     * 
     * @param srcData
     * @param key
     * @return
     * @throws Exception
     */
    public static String getStrEx(IData srcData, String key) throws Exception {

        if (Validator.isEmpty(srcData, key)) {
            Thrower.throwException(AcctCompException.INPUTPAMAMS_NOT_FOUND, key);
        }
        return srcData.getString(key).trim();
    }

    /**
     * 如果目标IData里面没有该键值或为null或为"",则抛出异常
     * 
     * @param srcData
     * @param key
     * @return
     * @throws Exception
     */
    public static int getIntEx(IData srcData, String key) throws Exception {
        return toInt(getStrEx(srcData, key));
    }

    /**
     * 如果目标IData里面没有该键值或为null或为"",则抛出异常
     * 
     * @param srcData
     * @param key
     * @return
     * @throws Exception
     */
    public static double getDoubleEx(IData srcData, String key) throws Exception {
        return toDouble(getStrEx(srcData, key));
    }

    /**
     * 如果目标IData里面没有该键值或为null或为"",则设默认值
     * 
     * @param descParam
     * @param key
     * @param defaultValue
     * @throws Exception
     */
    public static void setParamIfNone(IData descParam, String key, String defaultValue) {
        if (Validator.isEmpty(descParam, key)) {
            descParam.put(key, defaultValue.trim());
        }
    }

    /**
     * 将srcData中提取本业务相关的参数,没有则使用默认值
     * 
     * @param inparam
     * @param key
     * @param defaultValue
     * @param objdata
     * @throws Exception
     */
    public static void setParam(IData descParam, String key, IData srcData, String defaultValue) {
        descParam.put(key, srcData.getString(key, defaultValue));
    }

    /**
     * 将srcData中提取本业务相关的参数,没有则不设置(!针对数值性)
     * 
     * @param srcData
     * @param key
     * @param inparam
     * @throws Exception
     */
    public static void setParam(IData descParam, String key, IData srcData) {
        if (srcData.containsKey(key)) {
            descParam.put(key, srcData.get(key));
        }
    }

    /**
     * 将srcData中提取本业务相关的参数,没有则不设置(!针对数值性)
     * 
     * @param descData 目标数据对象
     * @param srcData 源数据对象
     * @param keys 需要设置值的key集合
     * @throws Exception
     */
    public static void setParam(IData descData, IData srcData, String... keys) {
        for (int i = 0; i < keys.length; i++) {
            setParam(descData, keys[i], srcData);
        }

    }

    /**
     * 将srcData中提取本业务相关的参数,没有则抛出异常
     * 
     * @param srcData
     * @param key
     * @param inparam
     * @throws Exception
     */
    public static void setParamEx(IData descParam, String key, IData srcData) throws Exception {
        if (srcData.containsKey(key)) {
            descParam.put(key, srcData.get(key));
        } else {
            Thrower.throwException(AcctCompException.INPUTPAMAMS_NOT_FOUND, key.toString());
        }
    }

    /**
     * 将输入value除以100
     * 
     * @param value
     * @return
     */
    public static String div100(double value) {
        return format2Centi(value / 100);
    }

    /**
     * 将输入value除以1000
     * 
     * @param value
     * @return
     */
    public static String div1000(double value) {
        return div1000(value, 3);
    }

    /**
     * 将输入value除以1000，四舍五入保留scale位小数
     * 
     * @param value
     * @param scale
     * @return
     */
    public static String div1000(double value, int scale) {
        return formatDecimal(value / 1000, scale);
    }

    /**
     * 将输入字符串除以100并格式化为0.00
     * 
     * @param value
     * @return
     */
    public static String div100(String value) {
        if (Validator.isBlank(value))
            return "0.00";

        char[] chars = value.toCharArray();
        int len = chars.length;
        char[] oc = new char[len > 2 ? len + 1 : 4];

        for (int i = 0, j = 0; i < len; i++, j++) {
            char c = chars[i];
            if (c > '9' || c < '0')
                return format2Centi(div100(toDouble(value)));
            if (i == len - 2) {
                oc[j++] = '.';
            }
            oc[j] = c;
        }
        if (len > 2) {
            return String.valueOf(oc);
        } else if (len == 2) {
            return "0." + value;
        } else {
            return "0.0" + value;
        }
    }

    /**
     * 将输入字符串除以100并格式化为0.0
     * 
     * @param value
     * @return
     */
    public static String div1000(String value) {
        return div1000(value, 3);
    }

    /**
     * 将输入字符串除以1000并格式化为0.0
     * 
     * @param value
     * @param scale
     * @return
     */
    public static String div1000(String value, int scale) {
        return div1000(value == null ? 0.0 : toDouble(value), scale);
    }

    /**
     * 对ds中指定的若干列进行除100的金额转换
     * 
     * @param ds
     * @param columns
     * @author skywalker
     */
    public static void div100(IDataset ds, String[] columns) {
        for (int i = 0; i < ds.size(); i++) {
            IData tmpData = ds.getData(i);
            div100(tmpData, columns);
        }
    }

    /**
     * 对ds中指定的若干列进行除1000的金额转换
     * 
     * @param ds
     * @param columns
     */
    public static void div1000(IDataset ds, String... columns) {
        div1000(ds, 3, columns);
    }

    /**
     * 对ds中指定的若干列进行除1000的金额转换
     * 
     * @param ds
     * @param scale
     * @param columns
     */
    public static void div1000(IDataset ds, int scale, String... columns) {
        for (int i = 0; i < ds.size(); i++) {
            IData data = ds.getData(i);
            div1000(data, scale, columns);
        }
    }

    /**
     * 对Data中指定的若干列进行除1000的金额转换
     * 
     * @param ds
     * @param scale
     * @param columns
     */
    public static void div1000(IData data, int scale, String... columns) {
        for (int j = 0; j < columns.length; j++) {
            data.put(columns[j], div1000(data.getString(columns[j]), scale));
        }
    }

    /**
     * 将输入value乘以100并取整
     * 
     * @param value
     * @return
     */
    public static String mult100(double value) {
        return mult(new BigDecimal(Double.toString(value)), 100);
    }

    /**
     * 将输入value乘以100并取整
     * 
     * @param value
     * @return
     */
    public static String mult1000(double value) {
        return mult(new BigDecimal(Double.toString(value)), 1000);
    }

    /**
     * 将输入value乘以multiValue并取整
     * 
     * @param value
     * @return
     */
    private static String mult(BigDecimal b1, int multiValue) {

        BigDecimal b2 = bigDecimalMap.get(multiValue);
        if (b2 == null) {
            b2 = new BigDecimal(multiValue);
            bigDecimalMap.put(multiValue, b2);
        }
        BigDecimal r = b1.multiply(b2);
        return formatDecimal(r, 0);
    }

    public static String mult100(String value) {
        return multScale(value, 2);
    }

    /**
     * 将输入value乘以100并取整
     * 
     * @param value
     * @return
     */
    private static String multScale(String value, int scale) {
        if (value == null)
            return null;
        int len = value.length(); // 0.01 4
        int stardDotIndex = len - scale - 1;

        int dotIndex = value.indexOf('.'); // 1
        if (dotIndex < 0) {
            return value + ZERO_SEQ[scale];
        }

        if (dotIndex >= stardDotIndex) { // 0.00
            String intPart = value.substring(0, dotIndex);
            String rightPart = (dotIndex == stardDotIndex) ? value.substring(dotIndex + 1) : value.substring(dotIndex + 1)
                    + ZERO_SEQ[dotIndex - stardDotIndex];
            String result = intPart + rightPart;

            int i = 0;
            for (; i < result.length() - 1; i++) {
                if (result.charAt(i) != '0')
                    break;
            }
            return i != 0 ? result.substring(i) : result;
        }

        return mult(new BigDecimal(value), TEN_POW_SEQ[2]);
    }

    /**
     * 将输入value乘以1000并取整
     * 
     * @param value
     * @return
     */
    public static String mult1000(String value) {
        return multScale(value, 3);
    }

    private static void multScale(IData data, int scale, String... keyArr) {
        if (keyArr == null || keyArr.length == 0)
            return;
        for (int i = 0; i < keyArr.length; i++) {
            data.put(keyArr[i], multScale(data.getString(keyArr[i]), scale));
        }
    }

    /**
     * 将data里面指定的值乘以100
     * 
     * @param data
     * @param keyArr
     */
    public static void mult100(IData data, String... keyArr) {
        multScale(data, 2, keyArr);
    }

    /**
     * 将data里面指定的值乘以1000
     * 
     * @param data
     * @param keyArr
     */
    public static void mult1000(IData data, String... keyArr) {
        multScale(data, 3, keyArr);
    }

    private static void multScale(IDataset ds, int scale, String... keyArr) {
        if (ds == null || ds.size() == 0)
            return;
        for (int i = 0; i < ds.size(); i++) {
            multScale(ds.getData(i), scale, keyArr);
        }
    }

    /**
     * 对ds中指定的若干列进行乘100的金额转换
     * 
     * @param ds
     * @param columns
     * @author skywalker
     */
    public static void mult100(IDataset ds, String... columns) {
        multScale(ds, 2, columns);
    }

    /**
     * 对ds中指定的若干列进行乘1000的金额转换
     * 
     * @param ds
     * @param columns
     * @author skywalker
     */
    public static void mult1000(IDataset ds, String... columns) {
        multScale(ds, 3, columns);
    }

    /**
     * 将data里面指定的值除以100
     * 
     * @param data
     * @param key
     */
    public static void div100(IData data, String key) {
        if (key == null || "".equals(key))
            return;
        data.put(key, div100(data.getString(key)));
    }

    /**
     * 将data里面指定的值除以100
     * 
     * @param data
     * @param keys
     */
    public static void div100(IData data, String[] keys) {
        if (keys.length < 1)
            return;

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            div100(data, key);
        }
    }

    /**
     * 在data里面的某个内容后面加上个 String。 例如 data里面有个值是 cjw,
     * 我们在它后面加上个'123',就可以用这个了，变为cjw123
     * 
     * @param data
     * @param key
     * @param c
     * @throws AmException
     * @author chenjw
     */
    public static void addString(IData data, String key, String c) throws Exception {
        if (key == null || key.equals("")) {
            Thrower.throwException(AcctCompException.ACCT_COMP_ERROR_MSG, "addString() 时, key为 null 或 ''");
        }
        if (c == null || c.equals("")) {
            return;
        }
        data.put(key, data.getString(key) + c);
    }

    /**
     * String 转化成 int
     * 
     * @param obj
     * @return
     */
    public static int toInt(Object obj) {
        if (obj == null)
            return 0;
        String val = obj.toString().trim();
        if (val.length() == 0)
            return 0;
        else
            return Integer.parseInt(val);
    }

    /**
     * String 转化成 long 并进行格式化
     * 
     * @param obj
     * @return
     */
    public static long toLong(Object obj) {
        if (obj == null) {
            return 0;
        }
        String value = obj.toString().trim();
        if (value.length() == 0)
            return 0;
        else
            return Long.parseLong(value);
    }

    /**
     * String 转化成 double 并进行格式化
     * 
     * @param obj
     * @return
     */
    public static double toDouble(Object obj) {
        String value;
        if (obj == null)
            return 0.00;
        value = obj.toString().trim();
        if (value.length() == 0)
            return 0.00;
        else
            return Double.parseDouble(value);

    }

    /**
     * int 转化成 String
     * 
     * @param value
     * @return
     */
    public static String toStr(int value) {
        return Integer.toString(value);
    }

    /**
     * int 转化成 String
     * 
     * @param value
     * @return
     */
    public static String toStr(double value) {
        return format2Centi(value);
    }

    public static void format2Centi(IData data, String... keys) {
        for (String key : keys) {
            data.put(key, format2Centi(data.get(key)));
        }
    }

    public static void format2Centi(IDataset ds, String... keys) {
        for (Object data : ds) {
            format2Centi((IData) data, keys);
        }
    }

    /**
     * 根据基数以及单位数组格式化数字
     * 
     * @param value 需要格式化的数字
     * @param units 单位数组
     * @param radix 基数，时间60，流量1024
     * @param fillAllUnit 是否显示全部单位
     * @param unitLen 每个单位数字的长度
     * @return
     */
    public static String formatNumByRadix(long value, String[] units, int radix, boolean fillAllUnit, int unitLen) throws Exception {
        DecimalFormat format = null;
        final int digitLen = unitLen < 0 ? 0 : unitLen;

        String key = "formatNumByRadix_" + unitLen;
        format = CacheUtil.get(decimalFormatMap.get(), key, new ICacheSourceProvider<DecimalFormat>() {
            @Override
            public DecimalFormat getSource() throws Exception {
                if (digitLen > 0) {
                    StringBuilder pat = new StringBuilder(digitLen);
                    for (int i = 0; i < digitLen; i++)
                        pat.append('0');

                    return new DecimalFormat(pat.toString());
                } else {
                    return new DecimalFormat();
                }
            }
        });

        String newValue = "";
        int j = 0;
        for (; j < units.length - 1; j++) {

            if (!fillAllUnit && value == 0)
                break;
            String perU = format.format(value % radix);
            newValue = perU + units[j] + newValue;
            value = value / radix;
        }
        if (value > 0 || fillAllUnit) {
            newValue = format.format(value) + units[units.length - 1] + newValue;
        }
        return newValue;
    }

    /**
     * double 转化成 String 并格式化成0.00
     * 
     * @param value
     * @return
     */
    public static String format2Centi(double value) {
        return format2Centi(Double.toString(value));
    }

    /**
     * double 转化成 String 并格式化成0.000
     * 
     * @param value
     * @return
     */
    public static String format2Milli(double value) {
        return format2Milli(Double.toString(value));
    }

    /**
     * 数字的String 格式化0.00后输出
     * 
     * @param obj
     * @return
     */
    public static String format2Centi(Object obj) {
        return formatDecimal(obj, 2);
    }

    /**
     * 数字的String 格式化0.000后输出
     * 
     * @param obj
     * @return
     */
    public static String format2Milli(Object obj) {
        return formatDecimal(obj, 3);
    }

    /**
     * 数字的String 格式化0.000后输出
     * 
     * @param obj
     * @return
     */
    public static String formatDecimal(Object obj, final int decimalLen) {
        double value = toDouble(obj);
        DecimalFormat format = null;
        String key = "formatDecimal_" + decimalLen;
        try {
            format = CacheUtil.get(decimalFormatMap.get(), key, new ICacheSourceProvider<DecimalFormat>() {
                @Override
                public DecimalFormat getSource() throws Exception {
                    StringBuilder formatStr = new StringBuilder(5);
                    if (decimalLen <= 0)
                        formatStr.append("#0");
                    else {
                        formatStr.append("#0.");
                        for (int i = 0; i < decimalLen; i++) {
                            formatStr.append('0');
                        }
                    }
                    DecimalFormat format = new DecimalFormat(formatStr.toString());
                    format.setRoundingMode(RoundingMode.HALF_UP);
                    return format;
                }
            });
        }
        catch (Exception e) {
            Thrower.throwException(AcctCompException.ACCT_COMP_ERROR_MSG, "获取Double格式化工具出错", e);
            format = new DecimalFormat("#0.00");
            format.setRoundingMode(RoundingMode.HALF_UP);
        }

        return format.format(value);
    }

    public static IData getSumDataWithDoubleFields(IDataset ds, String cols) throws Exception {
        return getSumDataWithDoubleFields(ds, cols.split(","));
    }

    /**
     * 根据传入的字段进行汇总生成汇总行记录
     * 
     * @param ds
     * @param cols
     * @return
     * @throws Exception
     */
    public static IData getSumDataWithDoubleFields(IDataset ds, String[] cols) throws Exception {
        IData data = new DataMap();
        if (cols == null || cols.length == 0)
            return data;
        for (int i = 0, len = cols.length; i < len; i++) {
            double fee = 0;
            for (int j = 0, size = ds.size(); j < size; j++) {
                fee = addForDou(fee, ds.getData(j).getDouble(cols[i]));
            }
            data.put(cols[i], fee);
        }
        return data;
    }

    public static IData getSumDataWithIntFields(IDataset ds, String cols) throws Exception {
        return getSumDataWithIntFields(ds, cols.split(","));
    }

    /**
     * 根据传入的字段进行汇总生成汇总行记录
     * 
     * @param ds
     * @param cols
     * @return
     * @throws Exception
     */
    public static IData getSumDataWithIntFields(IDataset ds, String[] cols) throws Exception {
        IData data = new DataMap();
        if (cols == null || cols.length == 0)
            return data;
        for (int i = 0, len = cols.length; i < len; i++) {
            long fee = 0L;
            for (int j = 0, size = ds.size(); j < size; j++) {
                fee += ds.getData(j).getLong(cols[i]);
            }
            data.put(cols[i], fee);
        }
        return data;
    }

    /**
     * 根据列名 添加一行合计列
     * 
     * @param ds
     * @param columns
     * @param col
     *            这个2个是写中文 合计 2字用的
     * @param name
     */
    public static void addSumCol(IDataset ds, String[] columns, String col, String name) {
        IData sumData = new DataMap();
        sumData.put(col, name);
        for (int i = 0; i < columns.length; i++) {
            String fee = "0";
            for (int j = 0; j < ds.size(); j++) {
                IData d = ds.getData(j);
                fee = addForDou(fee, d.getString(columns[i]));
            }
            sumData.put(columns[i], fee);
        }
        ds.add(sumData);
    }

    public static IDataset merge(IDataset ds, String[] primaryKeys, String[] moneyKeys) throws Exception {
        return merge(ds, new DataMergeComparator(primaryKeys, moneyKeys));
    }

    public static <R extends List<T>, T> R merge(R list, IMergeComparator<R, T> comp) throws Exception {
        R rl = comp.getResultList();
        Map<String, T> map = new HashMap<String, T>();

        for (int i = 0; i < list.size(); i++) {
            T t = list.get(i);
            String key = comp.getPrimaryKey(t);
            if (map.containsKey(key)) {
                T t2 = map.get(key);
                comp.mergeTo(t2, t);
            } else {
                map.put(key, t);
                rl.add(t);
            }
        }
        return rl;
    }

    public static void to100(IData data, String... keys) {
        for (String key : keys) {
            String value = data.getString(key);
            data.put(key, to100(value));
        }
    }

    /**
     * 厘转换单位为分
     * 
     * @param value
     */
    public static String to100(Object value) {
        if (value == null) {
            return "0";
        }
        String v = value.toString();
        if ("0".equals(v))
            return v;
        long val = Long.parseLong(v);
        long i = val > 0 ? (val + 5) / 10 : (val - 5) / 10;
        return Long.toString(i);
    }

    /**
     * 分转换单位为厘
     * 
     * @param value
     */
    public static String to1000(Object value) {
        if (value == null)
            return "0";

        String v = value.toString();
        if ("0".equals(v))
            return v;
        return v + "0";
    }

    /**
     * 从IDataset中获取第index位置的Data，若没有则新增并add到IDataset中
     * 
     * @param outDatas
     * @param index
     * @return
     * @throws Exception
     */
    public static IData getDataFromIDataset(IDataset outDatas, int index) {
        return getDataFromIDataset(outDatas, index, null);
    }

    public static IDataset getOnePageData(IDataset ids, Pagination pg) throws Exception {
        try {

            if (pg != null)
                pg.setCount(ids != null ? ids.size() : 0);
            if (ids == null || pg == null || pg.getPageSize() >= ids.size())
                return ids;

            int pageSize = pg.getPageSize(); // 每页显示行数
            int currPage = pg.getCurrent(); // 目标页数
            int dataSize = ids.size(); // 数据总量
            int start = (currPage - 1) * pageSize; // 起始位置:从0开始

            if (currPage - 1 > dataSize / pageSize) {
                start = 0;
                currPage = 1;
            } // 考虑到越界的情况，从第一页开始
            IDataset ret = new DatasetList();
            if (dataSize / pageSize < currPage) // 最后一页的情况
                pageSize = dataSize % pageSize;
            for (int i = 0; i < pageSize; i++) {
                Object d = ids.get(start + i);
                ret.add(d);
            }
            return ret;
        }
        catch (Exception e) {
            Thrower.throwException(AcctCompException.ACCT_COMP_ERROR_MSG, "数据分页时发生错误！" + e.getMessage());
            return null;
        }
    }

    /**
     * @param outDatas
     * @param index
     * @param initData
     * @return
     * @throws Exception
     */
    public static IData getDataFromIDataset(IDataset outDatas, int index, IData initData) {
        IData data = null;
        if (index >= outDatas.size()) {
            data = new DataMap();
            outDatas.add(data);
        } else {
            data = outDatas.getData(index);
        }
        if (initData != null && !initData.isEmpty())
            data.putAll(initData);
        return data;
    }

    public static IDataset newDataset(String key, String value) {
        IData data = new DataMap();
        data.put(key, value);
        return newDataset(data);
    }

    public static IDataset newDataset(IData data) {
        IDataset ds = new DatasetList();
        ds.add(data);
        return ds;
    }

    /**
     * 对IData中设置X_RESULTCODE和X_RESULTINFO字段
     * 
     * @param outData
     * @param resultCode
     * @param resultInfo
     */
    public static void setResultInfo(IData outData, int resultCode, String resultInfo) {
        outData.put("X_RESULTCODE", "" + resultCode);
        outData.put("X_RESULTINFO", resultInfo);
    }

    /**
     * 对IDataset中设置X_RESULTCODE和X_RESULTINFO字段
     * 
     * @param outDatas
     * @param resultCode
     * @param resultInfo
     * @throws Exception
     */
    public static void setResultInfo(IDataset outDatas, int resultCode, String resultInfo) throws Exception {
        setResultInfo(getDataFromIDataset(outDatas, 0), resultCode, resultInfo);

    }

    /**
     * IData中金额相加
     * 
     * @param srcData
     *            相加后存放金额的IData
     * @param addData
     *            相加的IData
     * @param keyArr
     *            金额字段集合
     */
    public static void addFeeColumn(IData srcData, IData addData, String... keyArr) {
        for (String key : keyArr) {
            long addFee = addData.getLong(key, 0);
            addFeeColumn(srcData, addFee, key);
        }
    }

    /**
     * IData中金额相加
     * 
     * @param srcData
     * @param addFee
     * @param key
     */
    public static void addFeeColumn(IData srcData, long addFee, String key) {
        long srcFee = srcData.getLong(key, 0);
        srcData.put(key, srcFee + addFee);
    }

    public static final void sort(IDataset data, String key, int keyType) {
        com.ailk.common.data.impl.DataHelper.sort(data, key, keyType);
    }

    public static final void sort(IDataset data, String key, int keyType, int order) {
        com.ailk.common.data.impl.DataHelper.sort(data, key, keyType, order);
    }

    public static final void sort(IDataset data, String key1, int keyType1, String key2, int keyType2) {
        com.ailk.common.data.impl.DataHelper.sort(data, key1, keyType1, key2, keyType2);
    }

    public static final void sort(IDataset data, String key1, int keyType1, int order1, String key2, int keyType2, int order2) {
        com.ailk.common.data.impl.DataHelper.sort(data, key1, keyType1, order1, key2, keyType2, order2);
    }

    public static final void sort(IDataset datas, String[] keys, int[] keyTypes) {
        sort(datas, keys, keyTypes, null);
    }

    public static final void sort(IDataset datas, String[] keys, int[] keyTypes, int order) {
        int[] orders = new int[keyTypes.length];
        for (int i = 0; i < keyTypes.length; i++) {
            orders[i] = order;
        }
        sort(datas, keys, keyTypes, orders);
    }

    public static final void sort(IDataset datas, String[] keys, int[] keyTypes, int[] orders) {
        Collections.sort(datas, new DataComparator(keys, keyTypes, orders));
    }

    public static final IDataset filter(IDataset source, String filter) throws Exception {
        return com.ailk.common.data.impl.DataHelper.filter(source, filter);
    }

    public static final IDataset distinct(IDataset source, String fieldNames, String token) throws Exception {
        return com.ailk.common.data.impl.DataHelper.distinct(source, fieldNames, token);
    }

    /**
     * 税率计算
     * 
     * @param value
     * @return
     */
    public static String divTaxRate(int value) {
        return (value <= 0) ? "0" : String.valueOf(value / 100);
    }

}
