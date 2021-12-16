package vkutils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.jemalloc.JEmalloc;

final class ScalarArray {
    private final long address ;
    private final long aLong;
    private int capacity;

    public <T extends Number>ScalarArray(@NotNull Number a)
    {
        capacity=a.intValue();
        address = JEmalloc.nje_mallocx(a.longValue(), JEmalloc.MALLOCX_ARENA(2)&JEmalloc.MALLOCX_ALIGN(8));
        aLong = Long.BYTES;
    }

    long getLong(int ref)
    {
        capacity--;
        return GLU2.theUnSafe.UNSAFE.getLong(address+(ref*aLong));
    }

    void Put(int ref, long v)
    {
            GLU2.theUnSafe.UNSAFE.putLong(address + (ref * aLong), v);
    }
    void Put(int ref, long @NotNull ... v)
    {
        for (long a: v)
            GLU2.theUnSafe.UNSAFE.putLong(address + (ref++ * aLong), a);
    }

    public long[] getArray(int a, int b)
    {
        long[] x = new long[a|b];
        for (int i=0;i<=x.length;i++)
        {
            x[i]= GLU2.theUnSafe.UNSAFE.getLong(address+(a++*aLong));
        }
        return x;
    }
    public long[] getArrayAlt(int a)
    {
        long[] x = new long[capacity-a];
        for (int i=0;i<=capacity-a;i++)
        {
            x[i]= getLong(a++);
        }
        return x;
    }

    public void CopyOf(int offset, @NotNull ScalarArray a)
    {
        GLU2.theUnSafe.UNSAFE.copyMemory(address, a.getAddress(), (capacity-(offset))*aLong);
    }

    @Contract(pure = true)
    private long getAddress()
    {
        return address;
    }

    void Del()
    {
        JEmalloc.nje_free(address);
    }
}
