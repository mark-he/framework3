package com.eagletsoft.boot.framework.common.utils;

import java.util.Date;

public class TimeRangeUtils {
    
    public interface Callback {
        void allOut();
        void allIn();
        //左边突出
        void leftOut(Date start, Date end);
        //右边突出
        void rightOut(Date start, Date end);
        //完整包含
        void contain(Date start, Date end);
        //左边在里面
        void leftIn(Date start, Date end);
        //右边在里面
        void rightIn(Date start, Date end);
    }

    public static void compare(Date start, Date end, Date targetStart, Date targetEnd, Callback callback) {
        if (start.after(targetEnd) || end.before(targetStart)) {
            callback.allOut();
        } else {
            if (start.before(targetStart)) {
                //starttime - downstart - 1
                callback.leftOut(start, DateUtils.next(targetStart, -1));
                //downstart - endtime
                if (end.after(targetEnd)) {
                    callback.contain(targetStart, targetEnd);
                    callback.rightOut(DateUtils.next(targetEnd, 1), end);
                } else {
                    callback.rightIn(targetStart, end);
                }
            } else {
                //starttime - endtime
                if (end.after(targetEnd)) {
                    if (start.compareTo(targetStart) == 0) {
                        callback.contain(targetStart, targetEnd);
                    } else {
                        callback.leftIn(start, targetEnd);
                    }
                    callback.rightOut(DateUtils.next(targetEnd, 1), end);
                } else {
                    callback.allIn();
                }
            }
        }
    }
}
