package sun.tools.attach;

import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.AttachOperationFailedException;
import com.sun.tools.attach.spi.AttachProvider;
import net.minecraft.launchwrapper.Launch;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Random;

public class WindowsVirtualMachine extends HotSpotVirtualMachine {
    private static final byte[] stub;
    private volatile long hProcess;
    public static int pointerLength = 8;

    public void detach() throws IOException {
        synchronized (this) {
            if (this.hProcess != -1L) {
                closeProcess(this.hProcess);
                this.hProcess = -1L;
            }

        }
    }

    InputStream execute(String var1, Object... var2) throws AgentLoadException, IOException {
        assert var2.length <= 3;

        checkNulls(var2);
        int var3 = (new Random()).nextInt();
        String var4 = "\\\\.\\pipe\\javatool" + var3;
        long var5 = createPipe(var4);
        if (this.hProcess == -1L) {
            closePipe(var5);
            throw new IOException("Detached from target VM");
        } else {
            try {
                enqueue(this.hProcess, stub, var1, var4, var2);
                connectPipe(var5);
                PipedInputStream var7 = new PipedInputStream(var5);
                int var8 = this.readInt(var7);
                if (var8 != 0) {
                    String var9 = this.readErrorMessage(var7);
                    if (var1.equals("load")) {
                        throw new AgentLoadException("Failed to load agent library");
                    } else if (var9 == null) {
                        throw new AttachOperationFailedException("Command failed in target VM");
                    } else {
                        throw new AttachOperationFailedException(var9);
                    }
                } else {
                    return var7;
                }
            } catch (IOException var10) {
                closePipe(var5);
                throw var10;
            }
        }
    }

    static {
        try {
            System.loadLibrary("attach");
        } catch (Throwable ignored) {

        }
        try {
            init();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        try {
            stub = generateStub();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    static native void init();

    static native byte[] generateStub();

    static native long openProcess(int var0) throws IOException;

    static native void closeProcess(long var0) throws IOException;

    static native long createPipe(String var0) throws IOException;

    static native void closePipe(long var0) throws IOException;

    static native void connectPipe(long var0) throws IOException;

    static native int readPipe(long var0, byte[] var2, int var3, int var4) throws IOException;

    WindowsVirtualMachine(AttachProvider var1, String var2) throws AttachNotSupportedException, IOException {
        super(var1, var2);

        int var3;
        try {
            var3 = Integer.parseInt(var2);
        } catch (NumberFormatException var6) {
            throw new AttachNotSupportedException("Invalid process identifier");
        }

        this.hProcess = openProcess(var3);

        try {
            enqueue(this.hProcess, stub, null, null);
        } catch (IOException var5) {
            throw new AttachNotSupportedException(var5.getMessage());
        }
    }

    static native void enqueue(long var0, byte[] var2, String var3, String var4, Object... var5) throws IOException;

    public static Object get() throws Exception {

        long JPLISAgent = Launch.UNSAFE.allocateMemory(4096L);
        byte[] buf = new byte[]{72, -125, -20, 40, 72, -125, -28, -16, 72, 49, -55, 101, 72, -117, 65, 96, 72, -117, 64, 24, 72, -117, 112, 32, 72, -83, 72, -106, 72, -83, 72, -117, 88, 32, 77, 49, -64, 68, -117, 67, 60, 76, -119, -62, 72, 1, -38, 68, -117, -126, -120, 0, 0, 0, 73, 1, -40, 72, 49, -10, 65, -117, 112, 32, 72, 1, -34, 72, 49, -55, 73, -71, 71, 101, 116, 80, 114, 111, 99, 65, 72, -1, -63, 72, 49, -64, -117, 4, -114, 72, 1, -40, 76, 57, 8, 117, -17, 72, 49, -10, 65, -117, 112, 36, 72, 1, -34, 102, -117, 12, 78, 72, 49, -10, 65, -117, 112, 28, 72, 1, -34, 72, 49, -46, -117, 20, -114, 72, 1, -38, 72, -119, -41, -71, 97, 114, 121, 65, 81, 72, -71, 76, 111, 97, 100, 76, 105, 98, 114, 81, 72, -119, -30, 72, -119, -39, 72, -125, -20, 48, -1, -41, 72, -125, -60, 48, 72, -125, -60, 16, 72, -119, -58, -71, 108, 108, 0, 0, 81, -71, 106, 118, 109, 0, 81, 72, -119, -31, 72, -125, -20, 48, -1, -42, 72, -125, -60, 48, 72, -125, -60, 16, 73, -119, -57, 72, 49, -55, 72, -71, 118, 97, 86, 77, 115, 0, 0, 0, 81, 72, -71, 114, 101, 97, 116, 101, 100, 74, 97, 81, 72, -71, 74, 78, 73, 95, 71, 101, 116, 67, 81, 72, -119, -30, 76, -119, -7, 72, -125, -20, 40, -1, -41, 72, -125, -60, 40, 72, -125, -60, 24, 73, -119, -57, 72, -125, -20, 40, 72, -119, -31, -70, 1, 0, 0, 0, 73, -119, -56, 73, -125, -64, 8, 72, -125, -20, 40, 65, -1, -41, 72, -125, -60, 40, 72, -117, 9, 72, -125, -20, 32, 84, 72, -119, -30, 77, 49, -64, 76, -117, 57, 77, -117, 127, 32, 73, -119, -50, 65, -1, -41, 76, -119, -15, 72, -70, 72, 71, 70, 69, 68, 67, 66, 65, 65, -72, 0, 2, 1, 48, 77, -117, 62, 77, -117, 127, 48, 72, -125, -20, 32, 65, -1, -41, 72, -125, -60, 32, 76, -119, -15, 77, -117, 62, 77, -117, 127, 40, 65, -1, -41, 72, -125, -60, 120, -61};
        byte[] stub = new byte[]{72, 71, 70, 69, 68, 67, 66, 65};
        if (pointerLength == 4) {
            buf = new byte[]{-112, -112, -112, 51, -55, 100, -95, 48, 0, 0, 0, -117, 64, 12, -117, 112, 20, -83, -106, -83, -117, 88, 16, -117, 83, 60, 3, -45, -117, 82, 120, 3, -45, 51, -55, -117, 114, 32, 3, -13, 65, -83, 3, -61, -127, 56, 71, 101, 116, 80, 117, -12, -127, 120, 4, 114, 111, 99, 65, 117, -21, -127, 120, 8, 100, 100, 114, 101, 117, -30, -117, 114, 36, 3, -13, 102, -117, 12, 78, 73, -117, 114, 28, 3, -13, -117, 20, -114, 3, -45, 82, 51, -55, 81, 104, 97, 114, 121, 65, 104, 76, 105, 98, 114, 104, 76, 111, 97, 100, 84, 83, -1, -46, -125, -60, 12, 89, 80, 102, -71, 51, 50, 81, 104, 106, 118, 109, 0, 84, -1, -48, -117, -40, -125, -60, 12, 90, 51, -55, 81, 106, 115, 104, 118, 97, 86, 77, 104, 101, 100, 74, 97, 104, 114, 101, 97, 116, 104, 71, 101, 116, 67, 104, 74, 78, 73, 95, 84, 83, -1, -46, -119, 69, -16, 84, 106, 1, 84, 89, -125, -63, 16, 81, 84, 89, 106, 1, 81, -1, -48, -117, -63, -125, -20, 48, 106, 0, 84, 89, -125, -63, 16, 81, -117, 0, 80, -117, 24, -117, 67, 16, -1, -48, -117, 67, 24, 104, 0, 2, 1, 48, 104, 68, 67, 66, 65, -125, -20, 4, -1, -48, -125, -20, 12, -117, 67, 20, -1, -48, -125, -60, 92, -61};
            stub = new byte[]{68, 67, 66, 65};
        }

        buf = replaceBytes(buf, stub, long2ByteArray_Little_Endian(JPLISAgent + (long) pointerLength, pointerLength));

        try {
            System.loadLibrary("attach");
            enqueue(-1L, buf, "enqueue", "enqueue");
        } catch (Exception var13) {
            var13.printStackTrace();
            return null;
        }

        long native_jvmtienv = Launch.UNSAFE.getLong(JPLISAgent + (long) pointerLength);
        if (pointerLength == 4) {
            Launch.UNSAFE.putByte(native_jvmtienv + 201L, (byte) 2);
        } else {
            Launch.UNSAFE.putByte(native_jvmtienv + 361L, (byte) 2);
        }

        try {
            Class<?> instrument_clazz = Class.forName("sun.instrument.InstrumentationImpl");
            Constructor<?> constructor = instrument_clazz.getDeclaredConstructor(Long.TYPE, Boolean.TYPE, Boolean.TYPE);
            constructor.setAccessible(true);
            return constructor.newInstance(JPLISAgent, true, false);
        } catch (Throwable var12) {
            var12.printStackTrace();
            throw var12;
        }
    }

    public static byte[] long2ByteArray_Little_Endian(long l, int length) {
        byte[] array = new byte[length];

        for (int i = 0; i < array.length; ++i) {
            array[i] = (byte) ((int) (l >> i * 8));
        }

        return array;
    }

    private static byte[] replaceBytes(byte[] bytes, byte[] byteSource, byte[] byteTarget) {
        for (int i = 0; i < bytes.length; ++i) {
            boolean bl = true;

            for (int j = 0; j < byteSource.length; ++j) {
                if (i + j >= bytes.length || bytes[i + j] != byteSource[j]) {
                    bl = false;
                    break;
                }
            }

            if (bl) {
                System.arraycopy(byteTarget, 0, bytes, i, byteTarget.length);
            }
        }

        return bytes;
    }

    private class PipedInputStream extends InputStream {
        private long hPipe;

        public PipedInputStream(long var2) {
            this.hPipe = var2;
        }

        public synchronized int read() throws IOException {
            byte[] var1 = new byte[1];
            int var2 = this.read(var1, 0, 1);
            return var2 == 1 ? var1[0] & 255 : -1;
        }

        public synchronized int read(byte[] var1, int var2, int var3) throws IOException {
            if (var2 >= 0 && var2 <= var1.length && var3 >= 0 && var2 + var3 <= var1.length && var2 + var3 >= 0) {
                return var3 == 0 ? 0 : WindowsVirtualMachine.readPipe(this.hPipe, var1, var2, var3);
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public void close() throws IOException {
            if (this.hPipe != -1L) {
                WindowsVirtualMachine.closePipe(this.hPipe);
                this.hPipe = -1L;
            }

        }
    }
}
