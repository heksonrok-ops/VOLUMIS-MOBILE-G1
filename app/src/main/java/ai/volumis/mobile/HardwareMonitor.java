package ai.volumis.mobile;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.StatFs;
import java.util.Arrays;
import java.util.Locale;

public final class HardwareMonitor {

    private static final long MB = 1024L * 1024L;

    private HardwareMonitor() {}

    public static ActivityManager.MemoryInfo memory(
        Context context
    ) {
        ActivityManager manager =
            (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE
            );

        ActivityManager.MemoryInfo info =
            new ActivityManager.MemoryInfo();

        manager.getMemoryInfo(info);
        return info;
    }

    public static long totalRamMB(Context context) {
        return memory(context).totalMem / MB;
    }

    public static long availableRamMB(Context context) {
        return memory(context).availMem / MB;
    }

    public static long freeStorageMB(Context context) {
        StatFs storage = new StatFs(
            context.getFilesDir().getAbsolutePath()
        );

        return storage.getAvailableBytes() / MB;
    }

    public static boolean supportsArm64() {
        return Arrays.asList(
            Build.SUPPORTED_ABIS
        ).contains("arm64-v8a");
    }

    public static int recommendedThreads() {
        return Math.max(
            1,
            Math.min(
                4,
                Runtime.getRuntime()
                    .availableProcessors() / 2
            )
        );
    }

    public static int recommendedContext(Context context) {
        return availableRamMB(context) >= 1800
            ? 1024 : 512;
    }

    public static boolean canAttemptLocalModel(
        Context context
    ) {
        ActivityManager.MemoryInfo info =
            memory(context);

        return supportsArm64()
            && !info.lowMemory
            && info.availMem / MB >= 1000
            && freeStorageMB(context) >= 1500;
    }

    public static String report(Context context) {

        return String.format(
            Locale.ROOT,
            "VOLUMIS HARDWARE REPORT\n\n"
            + "Device: %s %s\n"
            + "Android: %s\n"
            + "RAM total: %d MB\n"
            + "RAM available: %d MB\n"
            + "Storage free: %d MB\n"
            + "CPU cores: %d\n"
            + "ARM64: %s\n"
            + "ABI: %s\n"
            + "Recommended threads: %d\n"
            + "Recommended context: %d\n"
            + "Local model precheck: %s",

            Build.MANUFACTURER,
            Build.MODEL,
            Build.VERSION.RELEASE,
            totalRamMB(context),
            availableRamMB(context),
            freeStorageMB(context),
            Runtime.getRuntime().availableProcessors(),
            supportsArm64() ? "YES" : "NO",
            Arrays.toString(Build.SUPPORTED_ABIS),
            recommendedThreads(),
            recommendedContext(context),
            canAttemptLocalModel(context)
                ? "PASS" : "WAIT"
        );
    }
}
