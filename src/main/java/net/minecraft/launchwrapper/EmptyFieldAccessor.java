package net.minecraft.launchwrapper;

import sun.reflect.FieldAccessor;

public class EmptyFieldAccessor implements FieldAccessor {
    @Override
    public Object get(Object o) throws IllegalArgumentException {
        return new Object();
    }

    @Override
    public boolean getBoolean(Object o) throws IllegalArgumentException {
        return false;
    }

    @Override
    public byte getByte(Object o) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public char getChar(Object o) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public short getShort(Object o) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public int getInt(Object o) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public long getLong(Object o) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public float getFloat(Object o) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public double getDouble(Object o) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public void set(Object o, Object o1) throws IllegalArgumentException, IllegalAccessException {

    }

    @Override
    public void setBoolean(Object o, boolean b) throws IllegalArgumentException, IllegalAccessException {

    }

    @Override
    public void setByte(Object o, byte b) throws IllegalArgumentException, IllegalAccessException {

    }

    @Override
    public void setChar(Object o, char c) throws IllegalArgumentException, IllegalAccessException {

    }

    @Override
    public void setShort(Object o, short i) throws IllegalArgumentException, IllegalAccessException {

    }

    @Override
    public void setInt(Object o, int i) throws IllegalArgumentException, IllegalAccessException {

    }

    @Override
    public void setLong(Object o, long l) throws IllegalArgumentException, IllegalAccessException {

    }

    @Override
    public void setFloat(Object o, float v) throws IllegalArgumentException, IllegalAccessException {

    }

    @Override
    public void setDouble(Object o, double v) throws IllegalArgumentException, IllegalAccessException {

    }
}
