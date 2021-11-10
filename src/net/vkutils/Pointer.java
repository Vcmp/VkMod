package vkutils;

import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.Platform;
import vkutils.GLU2;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

import static org.lwjgl.system.Checks.CHECKS;
import static org.lwjgl.system.MemoryUtil.NULL;

public interface Pointer {

    /** The pointer size in bytes. Will be 4 on a 32bit JVM and 8 on a 64bit one. */
    int POINTER_SIZE = 8;

    /** The pointer size power-of-two. Will be 2 on a 32bit JVM and 3 on a 64bit one. */
    int POINTER_SHIFT = POINTER_SIZE == 8 ? 3 : 2;

    /** The value of {@code sizeof(long)} for the current platform. */
    int CLONG_SIZE  = POINTER_SIZE == 8 && Platform.get() == Platform.WINDOWS ? 4 : POINTER_SIZE;

    /** The value of {@code sizeof(long)} as a power-of-two. */
    int CLONG_SHIFT = CLONG_SIZE == 8 ? 3 : 2;

    /** Will be true on a 32bit JVM. */
    boolean BITS32 = POINTER_SIZE * 8 == 32;

    /** Will be true on a 64bit JVM. */
    boolean BITS64 = POINTER_SIZE * 8 == 64;

    /**
     * Returns the raw pointer address as a {@code long} value.
     *
     * @return the pointer address
     */
    long address();

    /** Default {@link org.lwjgl.system.Pointer} implementation. */
    abstract class Default implements org.lwjgl.system.Pointer {

        protected static final sun.misc.Unsafe UNSAFE;

        protected static final long ADDRESS;

        protected static final long BUFFER_CONTAINER;

        protected static final long BUFFER_MARK;
        protected static final long BUFFER_POSITION;
        protected static final long BUFFER_LIMIT;
        protected static final long BUFFER_CAPACITY;

        static {
            UNSAFE = GLU2.theGLU.UNSAFE;

            try {
                ADDRESS = UNSAFE.objectFieldOffset(org.lwjgl.system.Pointer.Default.class.getDeclaredField("address"));

                BUFFER_CONTAINER = UNSAFE.objectFieldOffset(CustomBuffer.class.getDeclaredField("container"));

                BUFFER_MARK = UNSAFE.objectFieldOffset(CustomBuffer.class.getDeclaredField("mark"));
                BUFFER_POSITION = UNSAFE.objectFieldOffset(CustomBuffer.class.getDeclaredField("position"));
                BUFFER_LIMIT = UNSAFE.objectFieldOffset(CustomBuffer.class.getDeclaredField("limit"));
                BUFFER_CAPACITY = UNSAFE.objectFieldOffset(CustomBuffer.class.getDeclaredField("capacity"));
            } catch (Throwable t) {
                throw new UnsupportedOperationException(t);
            }
        }

        // Removed final due to JDK-8139758. TODO: Restore if the fix is backported to JDK 8.
        protected long address;

        protected Default(long address) {
            if (CHECKS && address == NULL) {
                throw new NullPointerException();
            }
            this.address = address;
        }

        @Override
        public long address() {
            return address;
        }

        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof org.lwjgl.system.Pointer)) {
                return false;
            }

            org.lwjgl.system.Pointer that = (org.lwjgl.system.Pointer)o;

            return address == that.address();
        }

        public int hashCode() {
            return (int)(address ^ (address >>> 32));
        }

        @Override
        public String toString() {
            return String.format("%s pointer [0x%X]", getClass().getSimpleName(), address);
        }

        @SuppressWarnings("unchecked")
        protected static <T extends CustomBuffer<?>> T wrap(Class<? extends T> clazz, long address, int capacity) {
            T buffer;
            try {
                buffer = (T)UNSAFE.allocateInstance(clazz);
            } catch (InstantiationException e) {
                throw new UnsupportedOperationException(e);
            }

            UNSAFE.putLong(buffer, ADDRESS, address);
            UNSAFE.putInt(buffer, BUFFER_MARK, -1);
            UNSAFE.putInt(buffer, BUFFER_LIMIT, capacity);
            UNSAFE.putInt(buffer, BUFFER_CAPACITY, capacity);

            return buffer;
        }

        @SuppressWarnings("unchecked")
        protected static <T extends CustomBuffer<?>> T wrap(Class<? extends T> clazz, long address, int capacity, ByteBuffer container) {
            T buffer;
            try {
                buffer = (T)UNSAFE.allocateInstance(clazz);
            } catch (InstantiationException e) {
                throw new UnsupportedOperationException(e);
            }

            UNSAFE.putLong(buffer, ADDRESS, address);
            UNSAFE.putInt(buffer, BUFFER_MARK, -1);
            UNSAFE.putInt(buffer, BUFFER_LIMIT, capacity);
            UNSAFE.putInt(buffer, BUFFER_CAPACITY, capacity);
            UNSAFE.putObject(buffer, BUFFER_CONTAINER, container);

            return buffer;
        }

    }

}