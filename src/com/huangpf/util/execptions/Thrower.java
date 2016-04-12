package com.huangpf.util.execptions;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

import com.huangpf.util.data.IData;

public class Thrower {
    private static Logger log = Logger.getLogger(Thrower.class);

    public static void throwException(IAcctException e) throws RuntimeException {
        throwException(e, (Throwable) null);
    }

    public static void throwException(IAcctException e, Object... param) throws RuntimeException {
        throwException(e, null, param);
    }

    public static void throwException(IAcctException e, Throwable exp, Object... param) throws RuntimeException {
        String msg = String.format(e.getMessage(), param);
        throwException(e.getCode(), e.getLevel(), msg, exp);
    }

    private static void throwException(String code, ExceptionLevel level, String msg, Throwable e) throws RuntimeException {
        if (e != null)
            msg = msg + e.getMessage();

        // add . 如果异常码不是以ACCT_ 开头，则需默认加上，方便外围定位错误来源。
        String acctCode = code;
        if (!code.startsWith("ACCT_")) {
            acctCode = "ACCT_" + code;
        }

        String message = code + ":" + msg;
        if (level == ExceptionLevel.WARN) {
            log.warn(message);
        } else if (level == ExceptionLevel.ERROR) {
            log.error(message, e);
        }

        try {

            String inModeCode = SessionManager.getVisit().getInModeCode();
            String[] info = ErrorCodeUtil.transToArray(inModeCode, acctCode, msg);

            String appCode = info[0];
            if (!"0".equals(inModeCode) && !StringUtils.isNumeric(appCode)) {
                appCode = "-1";
            }
            msg = info[1];
            BaseException ex = new BaseException(appCode, null, msg);

            ErrItfTransCache cache = CacheManager.getReadOnlyCache(ErrItfTransCache.class);
            IData data = cache.getTransData(code, msg, inModeCode);
            if (data != null) {
                if (Validator.isEmpty(data, "X_RESULTINFO")) {
                    data.put("X_RESULTINFO", msg);
                }
                if (Validator.isEmpty(data, "RSPDESC")) {
                    data.put("RSPDESC", msg);
                }
                ex.setData(data);
            }
            throw ex;
        }
        catch (BaseException ex) {
            throw ex;
        }
        catch (Exception ex) {
            Utility.error(ex);
        }
    }

    public static String getStackTraceString(Throwable e) {
        return getStackTraceString(e, Integer.MAX_VALUE);
    }

    public static Throwable getBottomException(Throwable exception) {
        return Utility.getBottomException(exception);
    }

    public static String getStackTraceString(Throwable e, int maxLen) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        e.printStackTrace(pw);

        String str = sw.toString();
        return com.ailk.acctcomp.util.Utility.getByteSubString(str, maxLen);
    }

}

class AcctException extends RuntimeException {
    IAcctException expType = null;

    public IAcctException getExceptionType() {
        return expType;
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = -6822779928581211623L;

    public AcctException(String message, IAcctException expType) {
        super(message);
        this.expType = expType;
    }

    public AcctException(String message, Throwable e, IAcctException expType) {
        super(message, e);
        this.expType = expType;
    }

}
