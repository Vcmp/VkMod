package vkutils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.jemalloc.JEmalloc;

final class ScalarArray {
    private final long address;
    private static long aLong;
    private static int capacity;
    private static int lim;

    public <T extends Number>ScalarArray(@NotNull Number a)
    {
        capacity=a.intValue();
        address = JEmalloc.nje_mallocx(a.longValue(), JEmalloc.MALLOCX_ARENA(2)&JEmalloc.MALLOCX_ALIGN(8));
        aLong = Long.BYTES;
    }



    long get()
    {
//        capacity--;
        return GLU2.theUnSafe.UNSAFE.getLong(address+(lim*aLong));
    }long getLong(int ref)
    {
        capacity--;
        return GLU2.theUnSafe.UNSAFE.getLong(address+(ref*aLong));
    }
    long getLongSafe(int ref)
    {
        capacity--;
        return GLU2.theUnSafe.UNSAFE.getLong(address+(ref*aLong))|1;
    }

    void Put(int ref, long v)
    {
            GLU2.theUnSafe.UNSAFE.putLong(address + (ref * aLong), v);
            lim++;
    }
    void Put(long @NotNull ... v)
    {
        for (long a: v)
            GLU2.theUnSafe.UNSAFE.putLong(address + (lim++ * aLong), a);
    } void Put(Long ref, long @NotNull ... v)
    {
        for (long a: v)
            GLU2.theUnSafe.UNSAFE.putLong(address + (ref++ * aLong), a);
        lim+=v.length;
    }
    void Put(long v)
    {
            GLU2.theUnSafe.UNSAFE.putLong(address + (lim++ * aLong), v);
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

    public @NotNull ScalarArray Dup2(int offset, long address, long varargs)
    {
        ScalarArray a = null;
        try {
            a = (ScalarArray) GLU2.theUnSafe.UNSAFE.allocateInstance(ScalarArray.class);
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        GLU2.theUnSafe.UNSAFE.putLong(a.getAddress(), offset, address);
        GLU2.theUnSafe.UNSAFE.putLong(a.getAddress(), 24, varargs);
        return a;
    }

    @Contract(pure = true)
    public ScalarArray Dup2Safe()
    {
       ScalarArray a = this;
        GLU2.theUnSafe.UNSAFE.putLong(a, lim, a.getAddress()+lim);
        return a;
    }

    public void showAll()
    {
        for(int a=0; a<lim; a++)
        {
            System.out.println(getLong(a));
        }
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
