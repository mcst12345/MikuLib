package miku.lib.common.core;

import java.security.Permission;

//No usage at present
public class MikuSecurityManager extends SecurityManager {
    public MikuSecurityManager() {
    }

    public void checkPermission(Permission perm) {
        String permName = perm.getName() != null ? perm.getName() : "missing";
        if ("setSecurityManager".equals(permName)) {
            System.out.println("Cannot replace the Miku security manager");
            Runtime.getRuntime().exit(39);
        }
    }

    public void checkPermission(Permission perm, Object context) {
        this.checkPermission(perm);
    }
}
