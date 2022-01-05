package vkutils.setup;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public final class GLU2 {
public static final class theUnSafe
{
    public static final Unsafe UNSAFE;


    //private static final int offset = 16;

    static {
        UNSAFE=extracted();


    }

    private static Unsafe extracted()
    {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
    public static final class theGLU {




        /* public static void gluPerspective(float aspect, float zFar, float zNear) {
            *//*if (deltaZ != 0.0F && sine != 0.0F && aspect != 0.0F)*//*
            {
//                        __gluMakeIdentityf();
                INCBUF.rewind().put(IDENTITY_MATRIX, 0, IDENTITY_MATRIX.length);
                //            matrix.put(0, COTANGENT / aspect);

                IDENTITY_MATRIX[0] = COTANGENT / aspect;
                IDENTITY_MATRIX[10] = -(zFar + zNear) / zFar;
                INCBUF.put(IDENTITY_MATRIX, 0, IDENTITY_MATRIX.length);
                glMultMatrix(INCBUF);
            }
        }*/

        public static void memcpy(long srcAddress, long dstAddress, long Bytes)
        {
            theUnSafe.UNSAFE.copyMemory(srcAddress, dstAddress, Bytes);
        }

        //todo: Might be posible to use X Orientation as the xasis argmnt as a varyin Axis
        /*public static void glRotatef(float w, float x, float y, float z) {
            w /= 100D;
            double n = Math.sqrt(y * y * w * w);
//        n/=2.0D;
//
//        w/=n;
//        w/=100F;

            float a = (float) (*//*2**//*n * Math.acos(w));
            float r = (float) Math.acos(y);
            float r2 = (float) Math.atan(w * n*/

        public static void memcpy2(float[] vertices, long handle, int l)
        {
            theUnSafe.UNSAFE.copyMemory(vertices, 16, null, handle, l);
        }


        public static void memcpy2(short[] vertices, long handle, int l)
        {
            theUnSafe.UNSAFE.copyMemory(vertices, 16, null, handle, l);
        }

        //        static <T extends Buffer> T wrap(Class<? extends T> clazz, long address, int capacity) {
//            T buffer;
//            try {
//                buffer = (T)UNSAFE.allocateInstance(clazz);
//            } catch (InstantiationException e) {
//                throw new UnsupportedOperationException(e);
//            }
//
//            UNSAFE.putLong(buffer, ADDRESS, address);
//            UNSAFE.putInt(buffer, MARK, -1);
//            UNSAFE.putInt(buffer, LIMIT, capacity);
//            UNSAFE.putInt(buffer, CAPACITY, capacity);
//
//            return buffer;
//        }

//        public static void glPushMatrix()
//        {
//            INCBUF.push
//        }public static void glPushMatrix()
//        {
//            INCBUF.push
//        }

    }
}
