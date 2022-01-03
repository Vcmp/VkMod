package vkutils;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

import static org.lwjgl.system.Checks.CHECKS;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Reference {
    private final long address;

    public Reference(long a)
    {
        if (a == NULL) {
            throw new NullPointerException();
        }
        address=a;
    }


    public long address()
    {
        return address;
    }
    public long AX()
    {
        return MemoryUtil.memGetLong(address);
    }
}
