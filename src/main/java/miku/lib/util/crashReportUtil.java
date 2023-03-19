package miku.lib.util;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;

public class crashReportUtil {
    public static void addDetail(CrashReportCategory crashreportcategory,String s,Minecraft minecraft){
        crashreportcategory.addDetail(s, new ICrashReportDetail<String>()
        {
            public String call() {
                return minecraft.currentScreen.getClass().getCanonicalName();
            }
        });
    }
}
