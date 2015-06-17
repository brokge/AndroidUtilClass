package com.dxy.android.statistics.util;

import android.app.Service;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.dxy.android.statistics.DXYStatistics;
import com.dxy.android.statistics.DXYStatisticsConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 统计工具类
 * chenlw@dxyer.com
 * Created by chenlw on 2015/6/10.
 */

public class DXYStatisticsUtil {
    private static final String TAG = DXYStatisticsUtil.class.getSimpleName();

    public static Map<String, String> getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Map<String, String> map = new HashMap();
        NetworkInfo info = cm.getActiveNetworkInfo();
        if ((info == null) || (!info.isConnectedOrConnecting()) || (withinInBlackList())) {
            map.put("access_subtype", "offline");
            map.put("access", "offline");
            map.put("carrier", "");
        } else {
            map.put("access_subtype", info.getSubtypeName());
            map.put("access", cleanNetworkTypeName(info.getTypeName()));
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            String carrierName = manager.getNetworkOperatorName();
            map.put("carrier", carrierName);
        }
        return map;
    }

    private static String cleanNetworkTypeName(String type) {
      /*  if (AVUtils.isBlankString(type)) {
            return "offline";
        }*/
        String t = type.toUpperCase();
        if (t.contains("WIFI")) {
            return "WiFi";
        }
        if (type.contains("MOBILE")) {
            return "Mobile";
        }
        return type;
    }

    public static Map<String, Object> deviceInfo(Context context) {
        Map<String, Object> map = new HashMap();
        Map<String, String> networkInfo = getNetworkInfo(context);
        if (networkInfo != null) {
            map.putAll(networkInfo);
        }
        Map<String, Object> deviceInfo = getDeviceInfo(context);
        if (deviceInfo != null) {
            map.putAll(deviceInfo);
        }
        return map;
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    public static Map<String, Object> getDeviceInfo(Context context) {
        Map<String, Object> map = new HashMap();

        String packageName = context.getApplicationContext().getPackageName();
        map.put("package_name", packageName);
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            map.put("app_version", info.versionName);
            map.put("version_code", Integer.valueOf(info.versionCode));
            map.put("sdk_version", "Android v1.0");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        map.put("resolution", "" + width + "*" + height);
        map.put("device_model", Build.MODEL);
        map.put("device_manufacturer", Build.MANUFACTURER);
        map.put("os_version", Build.VERSION.RELEASE);
        map.put("device_name", Build.DEVICE);
        map.put("device_brand", Build.BRAND);
        map.put("device_board", Build.BOARD);
        map.put("device_manuid", Build.FINGERPRINT);
        map.put("cpu", getCPUInfo());
        map.put("os", "Android");
        map.put("sdk_type", "Android");

        long offset = TimeZone.getDefault().getRawOffset();
        try {
            offset = TimeUnit.HOURS.convert(offset, TimeUnit.MILLISECONDS);
        } catch (NoSuchFieldError e) {
            offset /= 3600000L;
        }
        map.put("time_zone", Long.valueOf(offset));
        return map;
    }

    public static String getCPUInfo() {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        if (new File("/proc/cpuinfo").exists()) {
            try {
                br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("Processor")) {
                        int position = line.indexOf(":");
                        if ((position >= 0) && (position < line.length() - 1)) {
                            sb.append(line.substring(position + 1).trim());
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "getCPUInfo", e);
            } finally {
                //AVPersistenceUtils.closeQuietly(br);
            }
        }
        return sb.toString();
    }

    public static String getLocalIpAddress() {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (!first) {
                            result.append('\n');
                        }
                        result.append(inetAddress.getHostAddress().toString());
                        first = false;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.i(TAG, ex.toString());
        }
        return result.toString();
    }

    public static String getApplicationFilePath(Context context) {
        File filesDir = context.getFilesDir();
        if (filesDir != null) {
            return filesDir.getAbsolutePath();
        }
        return "Couldn't retrieve ApplicationFilePath";
    }

    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    public static String getRandomString(int length) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomString = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            randomString.append(letters.charAt(new Random().nextInt(letters.length())));
        }
        return randomString.toString();
    }

    public static String uniqueId() {
        return UUID.randomUUID().toString();
    }

    public static boolean isStringEqual(String src, String target) {
        if ((src == null) && (target == null)) {
            return true;
        }
        if (src != null) {
            return src.equals(target);
        }
        return false;
    }

    static List<String> CELLPHONEBLACKLIST = Arrays.asList(new String[]{"d2spr"});

    private static boolean withinInBlackList() {
        if (CELLPHONEBLACKLIST.contains(Build.DEVICE)) {
            return true;
        }
        return false;
    }

    /**
     * 检查是否缺少某个权限
     *
     * @param context        context
     * @param packName       appPack name
     * @param permissionName permissionName
     * @return true or false
     */
    public static boolean checkPermission(Context context, String packName, String permissionName) {
        PackageManager pm = context.getPackageManager();
        return (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission(permissionName, packName));
    }

    /**
     * 获取 zip 文件绝对路径
     *
     * @param zipPath "/dxy/statistics/zip/"
     * @return 返回 absolutePath
     */
    public static String getZipSDPath(String zipPath) {
        File file = new File(getExternalStorageDirectory() + zipPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * 获取日志 绝对 路径
     *
     * @param logPath "/dxy/statistics/log/"
     * @return 返回 absolutePath
     */
    public static String getLogSDPath(String logPath) {
        File file = new File(getExternalStorageDirectory() + logPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * 外部内存卡路径
     *
     * @return
     */
    public static String getExternalStorageDirectory() {
        Map<String, String> map = System.getenv();
        String[] values = new String[map.values().size()];
        map.values().toArray(values);
        String path = values[values.length - 1];    //外置SD卡的路径
        String sdcardPath = getSDPath();
        if (path.startsWith("/mnt/") && !sdcardPath.equals(path)) {
            return path;
        } else {
            return sdcardPath;
        }
    }

    public static String getSDPath() {
        if (isSDPresent()) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    /**
     * 是否有内存卡
     *
     * @return
     */
    public static boolean isSDPresent() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }


    public static void printLog(String tagContent) {
        if (DXYStatisticsConfig.ISDebugMode) {
            Log.i(DXYStatistics.TAG, tagContent);
        }
    }

    public static String getUploadUrl() {
        if (!DXYStatisticsConfig.ISDebugMode) {
            return DXYStatisticsConfig.UPLOADURL.contains("net") ? DXYStatisticsConfig.UPLOADURL.replace("net", "cn") : DXYStatisticsConfig.UPLOADURL;
        }
        return DXYStatisticsConfig.UPLOADURL;
    }
}
