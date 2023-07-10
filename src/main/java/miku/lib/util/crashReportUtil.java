package miku.lib.util;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReportCategory;

public class crashReportUtil {
    public static void addDetail(CrashReportCategory crashreportcategory,String s,Minecraft minecraft){
        crashreportcategory.addDetail(s, () -> minecraft.currentScreen.getClass().getCanonicalName());
    }
}
