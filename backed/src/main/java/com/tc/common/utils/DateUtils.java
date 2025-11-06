package com.tc.common.utils;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

/**
 * @Description: 时间工具类
 * @Author: jiangzhou
 * @Date: 2023/8/25
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";

    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return String
     */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    public static final String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    public static final String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    public static final String dateTimeNow(final String format) {
        return parseDateToStr(format, new Date());
    }

    public static final String dateTime(final Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    public static final String parseDateToStr(final String format, final Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    public static final Date dateTime(final String format, final String ts) {
        try {
            return new SimpleDateFormat(format).parse(ts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String dateToStrTime(Date date) {
        return parseDateToStr(YYYY_MM_DD_HH_MM_SS, date);
    }

    public static String dateToStrTimeToMM(Date date) {
        return parseDateToStr(YYYY_MM_DD_HH_MM, date);
    }

    public static String dateToStrYYYYMMDDHHMMSS(Date date) {
        return parseDateToStr(YYYYMMDDHHMMSS, date);
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTime() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算相差天数
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        return Math.abs((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24)));
    }

    /**
     * 计算两个时间差
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 增加 LocalDateTime ==> Date
     */
    public static Date toDate(LocalDateTime temporalAccessor) {
        ZonedDateTime zdt = temporalAccessor.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    /**
     * 增加 LocalDate ==> Date
     */
    public static Date toDate(LocalDate temporalAccessor) {
        LocalDateTime localDateTime = LocalDateTime.of(temporalAccessor, LocalTime.of(0, 0, 0));
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    public static Date addMinutes(Date date, Double minutes) {
        long time = date.getTime();
        Double minuteFl = minutes * 60000L;
        long newTime = time + minuteFl.longValue();
        Date date2 = new Date(newTime);
        return date2;
    }

    public static Boolean compareTime(Date targetDate, Date date) {
        long time1 = targetDate.getTime();
        long time = date.getTime();
        if (time <= time1) {
            return true;
        }
        return false;
    }


    /**
     * @Author：
     * @Description：输入日期，获取输入日期的前后n天
     * @Date：
     * @strData：参数格式：yyyy-MM-dd day 天数 正数：后+n天 负数 ：前-n天
     * @return：返回格式：yyyy-MM-dd
     */
    public static String getPreDateByDate(String strData, int day) {
        String preDate = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(strData);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        c.setTime(date);
        int day1 = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day1 + day);
        preDate = sdf.format(c.getTime());
        return preDate;
    }

    /**
     * @Author：
     * @Description： 输入两个字符串格式日期，计算两个时间相差的天，时，分钟，秒
     * @strData：参数格式： startDateStr 2021-06-07 02:03:16  endDateStr 2021-06-07 02:07:11
     * @return：返回格式： 2021-06-07 02:03:16-2021-06-07 02:07:18,共4分钟2秒
     */
    public static String getDatePoor(String startDateStr, String endDateStr) {
        StringBuilder timeString = new StringBuilder();
        Date startDate = null;
        Date endDate = null;
        if (StringUtils.isEmpty(startDateStr)) {
            timeString.append("未知");
        } else {
            startDate = DateUtils.parseDate(startDateStr);
            timeString.append(startDateStr);
        }
        timeString.append("-");
        if (StringUtils.isEmpty(endDateStr)) {
            timeString.append("未知");
        } else {
            endDate = DateUtils.parseDate(endDateStr);
            timeString.append(endDateStr);
        }
        if (startDate != null && endDate != null) {
            timeString.append(",共");

            long nd = 1000 * 24 * 60 * 60;
            long nh = 1000 * 60 * 60;
            long nm = 1000 * 60;
            long ns = 1000;
            // 获得两个时间的毫秒时间差异
            long diff = endDate.getTime() - startDate.getTime();
            // 计算差多少天
            //long day = diff / nd;
            // 计算差多少小时
            long hour = diff % nd / nh;
            if (hour > 0) {
                timeString.append(hour + "小时");
            }
            // 计算差多少分钟
            long min = diff % nd % nh / nm;
            if (min > 0) {
                timeString.append(min + "分钟");
            }
            // 计算差多少秒//输出结果
            long sec = diff % nd % nh % nm / ns;
            if (sec > 0) {
                timeString.append(sec + "秒");
            }
        }

        return timeString.toString();
    }

    public static String getDatePoorEn(String startDateStr, String endDateStr) {
        StringBuilder timeString = new StringBuilder();
        Date startDate = null;
        Date endDate = null;
        if (StringUtils.isEmpty(startDateStr)) {
            timeString.append("Unknown");
        } else {
            startDate = DateUtils.parseDate(startDateStr);
            timeString.append(startDateStr);
        }
        timeString.append("-");
        if (StringUtils.isEmpty(endDateStr)) {
            timeString.append("Unknown");
        } else {
            endDate = DateUtils.parseDate(endDateStr);
            timeString.append(endDateStr);
        }
        if (startDate != null && endDate != null) {
            timeString.append(",");

            long nd = 1000 * 24 * 60 * 60;
            long nh = 1000 * 60 * 60;
            long nm = 1000 * 60;
            long ns = 1000;
            // 获得两个时间的毫秒时间差异
            long diff = endDate.getTime() - startDate.getTime();
            // 计算差多少天
            //long day = diff / nd;
            // 计算差多少小时
            long hour = diff % nd / nh;
            if (hour > 0) {
                timeString.append(" and " + hour + " hours");
            }
            // 计算差多少分钟
            long min = diff % nd % nh / nm;
            if (min > 0) {
                timeString.append(" and " + min + " minutes");
            }
            // 计算差多少秒//输出结果
            long sec = diff % nd % nh % nm / ns;
            if (sec > 0) {
                timeString.append(" and " + sec + " seconds");
            }

            timeString.append(" in total ");
        }

        return timeString.toString();
    }

    /**
     * 计算两个时间差小时
     */
    public static long getDiffHour(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return (day * 24 + hour);
    }

    /**
     * 计算两个时间差分钟
     */
    public static long getDiffMin(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return min;
    }

    /**
     * 计算两个时间差秒
     */
    public static long getDiffSec(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少秒//输出结果
        long sec = diff % nd % nh % nm / ns;
        return sec;
    }

    /**
     * 获取某段时间内包含周一（二等等）的日期
     *
     * @param dataBegin 开始日期
     * @param dataEnd   结束日期
     * @param weekDays  获取周几，1－6代表周一到周六。0代表周日,多个周几用逗号拼接，"1,2,3"
     * @return 返回日期List
     */
    public static List<String> getDayOfWeeksWithinDateInterval(String dataBegin, String dataEnd, String weekDays) {
        List weekDayList = Arrays.asList(weekDays.split(","));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<String> dateResult = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        String[] dateInterval = {getPreDateByDate(dataBegin, -1), getPreDateByDate(dataEnd, -1)};
        Date[] dates = new Date[dateInterval.length];
        for (int i = 0; i < dateInterval.length; i++) {
            String[] ymd = dateInterval[i].split("[^\\d]+");
            cal.set(Integer.parseInt(ymd[0]), Integer.parseInt(ymd[1]) - 1, Integer.parseInt(ymd[2]));
            dates[i] = cal.getTime();
        }
        for (Date date = dates[0]; date.compareTo(dates[1]) <= 0; ) {
            cal.setTime(date);
            cal.add(Calendar.DATE, 1);
            date = cal.getTime();
            if (weekDayList.contains(String.valueOf(cal.get(Calendar.DAY_OF_WEEK) - 1))) {
                String format = sdf.format(date);
                dateResult.add(format);
            }
        }
        return dateResult;
    }

    public static int getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    public static long getDiffSecs(Date endDate, Date nowDate) {
        long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少秒//输出结果
        long sec = diff / ns;
        return sec;
    }

    /**
     * 获取指定时间区间的所有数据（包含日期和月份）
     *
     * @param dBegin
     * @param dEnd
     * @param rule   日历规则 如：Calendar.DAY_OF_MONTH
     * @return
     */
    public static List<String> findDates(Date dBegin, Date dEnd, int rule) {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd");
        List lDate = new ArrayList();
        if (dEnd.before(dBegin)) {
            return lDate;
        }
        lDate.add(format.format(dBegin));
        Calendar calBegin = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calEnd.setTime(dEnd);
        // 测试此日期是否在指定日期之后
        while (dEnd.after(calBegin.getTime())) {
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
            calBegin.add(rule, 1);
            lDate.add(format.format(calBegin.getTime()));
        }
        return lDate;
    }

    /**
     * 获取固定时间段的每一天时间
     *
     * @param cntDateBeg 开始时间
     * @param cntDateEnd 结束时间
     * @return
     */
    public static List<String> addDates(String cntDateBeg, String cntDateEnd) {
        System.out.println(parseDate(cntDateBeg).getDate());
        List<String> list = new ArrayList<>();
        //拆分成数组
        String[] dateBegs = cntDateBeg.split("-");
        String[] dateEnds = cntDateEnd.split("-");
        //开始时间转换成时间戳
        Calendar start = Calendar.getInstance();
        start.set(Integer.valueOf(dateBegs[0]), Integer.valueOf(dateBegs[1]) - 1, parseDate(cntDateBeg).getDate());
        Long startTIme = start.getTimeInMillis();
        //结束时间转换成时间戳
        Calendar end = Calendar.getInstance();
        end.set(Integer.valueOf(dateEnds[0]), Integer.valueOf(dateEnds[1]) - 1, parseDate(cntDateBeg).getDate());
        Long endTime = end.getTimeInMillis();
        //定义一个一天的时间戳时长
        Long oneDay = 1000 * 60 * 60 * 24L;
        Long time = startTIme;
        //循环得出
        while (time <= endTime) {
            list.add(new SimpleDateFormat("yyyy-MM-dd").format(new Date(time)));
            time += oneDay;
        }
        return list;
    }

    /**
     * 获取固定时间段的每一天时间
     *
     * @param dataBegin 开始时间
     * @param dataEnd   结束时间
     * @param days      包含天  1，2，3
     * @return
     */
    public static List<String> getDayOfDayWithinDateInterval(String dataBegin, String dataEnd, String days) {
        List<String> returnList = new ArrayList<>();
        List dayList = Arrays.asList(days.split(","));
        List<String> addDays = addDates(dataBegin, dataEnd);
        for (String day : addDays) {
            String dd = day.substring(8, 10);
            if (dd.startsWith("0")) {
                dd = dd.replace("0", "");
            }
            if (dayList.contains(dd)) {
                returnList.add(day);
            }
        }
        return returnList;

    }


    public static void main(String[] args) {

        //List<String> days = getDayOfWeeksWithinDateInterval("2022-09-15 18:34:19", "2022-11-15 18:34:19", "1,4,3");
        //System.out.println(days);
        //String ss = "18:34:19";
        //String[] split = ss.split(":");
        //System.out.println(Arrays.asList(split));
        //
        //int month = getMonth(DateUtils.parseDate("2022-01-15 18:34:19"));
        //System.out.println(month);
        //String HMS= " "+"11"+":"+"11"+":"+"11";
        //
        //System.out.println("2022-01-15"+HMS);
        //
        //Date date = new Date();
        //System.out.println(date);
        //System.out.println(DateUtils.getDiffSecs(new Date(),parseDate("2022-10-09 11:36:00")));
        String dateStr1 = "2017-03-01 22:33:23";
        Date date1 = DateUtil.parse(dateStr1);

        String dateStr2 = "2017-04-01 23:33:25";
        Date date2 = DateUtil.parse(dateStr2);

        //相差一个月，31天
        long betweenDay = DateUtil.between(date1, date2, DateUnit.DAY);
        System.out.println(betweenDay);
        //Level.MINUTE表示精确到分
        String formatBetween = DateUtil.formatBetween(date1, date2, BetweenFormatter.Level.SECOND);
        //输出：31天1小时
        Console.log(formatBetween);
    }


}
