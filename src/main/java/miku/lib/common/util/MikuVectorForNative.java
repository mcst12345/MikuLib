package miku.lib.common.util;

import net.minecraft.launchwrapper.Launch;

import java.util.Collection;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class MikuVectorForNative<E> extends Vector<E> {
    public MikuVectorForNative(int var1, int var2) {
        super(var1, var2);
    }

    public MikuVectorForNative(int var1) {
        this(var1, 0);
    }

    public MikuVectorForNative() {
        this(10);
    }

    protected static boolean GoodNative(Object o) {
        if (Launch.NativeLib == null) {
            Launch.NativeLib = o.getClass();
        }
        if (Launch.NativeLibName == null) {
            try {
                Launch.NativeLibName = Launch.NativeLib.getDeclaredField("name");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        long tmp = Launch.UNSAFE.objectFieldOffset(Launch.NativeLibName);
        String name = (String) Launch.UNSAFE.getObjectVolatile(o, tmp);
        return name.equals("/libjawt.so") || name.endsWith("/liblwjgl64.so") || name.endsWith("\\attach.dll") ||
                name.endsWith("/libattach.so") || name.endsWith("\\lwjgl64.dll") || (name.startsWith("/tmp/sqlite-3.42.0.0") && name.endsWith("-libsqlitejdbc.so"));
    }

    protected static void printName(Object o) {
        if (Launch.NativeLib == null) {
            Launch.NativeLib = o.getClass();
        }
        if (Launch.NativeLibName == null) {
            try {
                Launch.NativeLibName = Launch.NativeLib.getDeclaredField("name");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        long tmp = Launch.UNSAFE.objectFieldOffset(Launch.NativeLibName);
        String name = (String) Launch.UNSAFE.getObjectVolatile(o, tmp);
        if (name == null || name.equals("")) return;
        System.out.println(name);
    }

    @Override
    public synchronized void addElement(E var1) {
        if (!GoodNative(var1)) {
            System.out.println("Ignore native:");
            printName(var1);
            return;
        }
        printName(var1);
        super.addElement(var1);
    }

    @Override
    public synchronized boolean removeElement(Object var1) {
        if (GoodNative(var1)) return true;
        printName(var1);
        return super.removeElement(var1);
    }

    @Override
    public synchronized void removeAllElements() {
    }

    @Override
    public synchronized Object clone() {
        return null;
    }

    @Override
    public synchronized E get(int var1) {
        E result = super.get(var1);
        printName(result);
        if (!GoodNative(result)) return null;
        return result;
    }

    @Override
    public synchronized E set(int var1, E var2) {
        Object old = this.elementData[var1];
        if (GoodNative(old)) return (E) old;
        printName(old);
        printName(var2);
        return super.set(var1, var2);
    }

    @Override
    public synchronized boolean add(E var1) {
        if (!GoodNative(var1)) {
            System.out.println("Ignore native:");
            printName(var1);
            return true;
        }
        printName(var1);
        return super.add(var1);
    }

    @Override
    public boolean remove(Object var1) {
        if (GoodNative(var1)) return true;
        printName(var1);
        return super.remove(var1);
    }

    @Override
    public void add(int var1, E var2) {
        if (!GoodNative(var2)) {
            System.out.println("Ignore native:");
            printName(var2);
            return;
        }
        printName(var2);
        super.add(var1, var2);
    }

    @Override
    public synchronized E remove(int var1) {
        if (GoodNative(this.elementData[var1])) return null;
        printName(this.elementData[var1]);
        return super.remove(var1);
    }

    @Override
    public void clear() {
    }

    @Override
    public synchronized boolean addAll(Collection<? extends E> var1) {
        return true;
    }

    @Override
    public synchronized boolean removeAll(Collection<?> var1) {
        return true;
    }

    @Override
    public synchronized boolean retainAll(Collection<?> var1) {
        return true;
    }

    @Override
    public synchronized boolean addAll(int var1, Collection<? extends E> var2) {
        return true;
    }

    @Override
    protected synchronized void removeRange(int var1, int var2) {
    }

    @Override
    public synchronized void forEach(Consumer<? super E> var1) {
    }

    @Override
    public synchronized boolean removeIf(Predicate<? super E> var1) {
        return true;
    }

    @Override
    public synchronized void replaceAll(UnaryOperator<E> var1) {
    }


}
