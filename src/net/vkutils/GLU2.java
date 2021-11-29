package vkutils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public final class GLU2 {

    public static final class theGLU {

        private static final Unsafe UNSAFE;


       /* public static final FloatBuffer MODView = BufferUtils.createFloatBuffer(0x1FFFFF);
        //     static final FloatBuffer PROJ = BufferUtils.createFloatBuffer(0x1FFFFF);
        //static final FloatBuffer INCBUF = BufferUtils.createFloatBuffer(0x1FFFFF);

        private static final float[] MATRIX = {1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1};*/
        //private static final int offset = 16;
        /*private static final FloatBuffer matrix2 = BufferUtils.createFloatBuffer(offset).put((MATRIX), 0, (MATRIX).length);


        private static final FloatBuffer matrix = BufferUtils.createFloatBuffer(offset);
        private static final double PI2 = 2F * (Math.atan(1) * 4) / 180F;
        private static final double RADIANS = 2F / PI2;
        private static final float COTANGENT = (float) Math.cos(RADIANS) / (float) Math.sin(RADIANS);
        private static final float FOVModifier = 0.0F;//1.0F

        private static final float[] IDENTITY_MATRIX = {1.0F, 0.0F, 0.0F, 0.0F,
                0.0F, COTANGENT, 0.0F, 0.0F,
                0.0F, 0.0F, 1.0F, -1.0F,
                0.0F, 0.0F, -2.0F * 0.05F, FOVModifier};*/

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


       /* private static void __gluMakeIdentityf() {
//        int oldPos = matrix.position();
//        matrix;
            matrix.rewind().put(IDENTITY_MATRIX, 0, IDENTITY_MATRIX.length);
        }
*/
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

       /* private static void glMultMatrix(float[] a) {
            GL30.nglMultMatrixf(getPointer(matrix.put(0, a)));
        }

        private static void glMultMatrix(FloatBuffer modView) {
            modView.rewind();
            GL30.nglMultMatrixf(getPointer(modView));
//        modView.rewind();
        }*/

       /* public static long getPointer(Object obj) {
            return UNSAFE.getLong(obj, offset);
        }*/
        public static long getPointer(Object obj, long offset) {
            return UNSAFE.getLong(obj, offset);
        }public static long getPointer(long offset) {
            return UNSAFE.getLong(null, offset);
        }

        public static void memcpy(long srcAddress, long dstAddress, long Bytes)
        {
            UNSAFE.copyMemory(srcAddress, dstAddress, Bytes);
        }
        public static void memcpy(Object o, long srcAddress, Object o2, long dstAddress, long Bytes)
        {
            UNSAFE.copyMemory(o, srcAddress, null, dstAddress, Bytes);
        }
        public static long alloc(long a)
        {
            return UNSAFE.allocateMemory(a);
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
            float r2 = (float) Math.atan(w * n*//**a/*2*//*);
//        w*=r;
            float c = (float) Math.cos(w * y);
            float s = (float) Math.sin(w * y);

            GL11C.nglGetFloatv(ARBVertexBlend.GL_MODELVIEW0_ARB, GLU2.theGLU.getPointer(MODView));
            MODView.put(MATRIX, 0, MATRIX.length);


            MATRIX[0] = c;
            MATRIX[2] = a * (s * -c) + r2;
            MATRIX[8] = -a * (-s * c) - r2;
            MATRIX[5] = y;
            MATRIX[10] = c;


//        c/=s;


//        MATRIX[0] = c;
//        MATRIX[2] = s;
//        MATRIX[8] = -s;
//        MATRIX[10] = c;
//        MODView.put(MATRIX);
       *//* MATRIX[0] = y;
//        MATRIX[1] = (float) (y*x*s * c);
//        MATRIX[1] = (float) (y*x - c-z*s);
        MATRIX[1] = y - c * s;
        MATRIX[2] = *//**//*s + y **//**//*COTANGENT* c;

        MATRIX[3]= (1);
        MATRIX[7]= (float) (w/n/100);
        MATRIX[11]= (1);*//*

//
//        MATRIX[4] = x * y - c - z * s;
//        MATRIX[5] = y * y - c + c;
//        MATRIX[6] = y * z - c + x * s;
//
            *//* MATRIX[8] =  *//**//*-s + y **//**//* COTANGENT*-c;
//        MATRIX[9] = -(y * z - c - x * s);
        MATRIX[9] =  y - c * s;
        MATRIX[10] = y;*//*

//        GL11.glPopMatrix();

            MODView.put(MATRIX, 0, MATRIX.length);

//        MODView.rewind();
//        GL11.glLoadMatrixf(MODView);

//        GL11.glLoadMatrixf(MATRIX);
//        GL11.glPushMatrix();


//        final float zs = Z * s;
//        final float ys = Y * s;
//        final float xs = X * s;
//        final float v = 1 - c;

//        double n = Y*W*((Math.sqrt(X*X+Y*Y+Z*Z)));
//        n/=200;
//        W*=n;
//        X*=n;
//        Z*=n;
//        Y*=n;
//        float c= (float) Math.cos(W*.01);
//        float s= (float) Math.sin(W*.01);
//        final float v = 1 - c;
//        ROTATION_MATRIX[1]  = X;
//        ROTATION_MATRIX[12]  = v +(c*Y*X);
//        ROTATION_MATRIX[8]  = v-(Z*s*X*Z);
//        ROTATION_MATRIX[4]  = v + Y*s;
//
//        ROTATION_MATRIX[0]  = 1;
//
//        ROTATION_MATRIX[6]  = Y*X;
//        ROTATION_MATRIX[2]  = v +(Z*s*(Y));
//        ROTATION_MATRIX[13]  = v +(c*Y*Z);
//        ROTATION_MATRIX[9]  = v- X*s;
//
//        ROTATION_MATRIX[5]  = 1;
//        ROTATION_MATRIX[15]  = 1;
//        ROTATION_MATRIX[11]  = X*Z;
//        ROTATION_MATRIX[7] = v-(Y*s*Y*Z);
//        ROTATION_MATRIX[3] = v-(X*s*(Z));
//        ROTATION_MATRIX[14] = v +c;
//        ROTATION_MATRIX[10] = 1;

//        ROTATION_MATRIX[5]=t;
//        ROTATION_MATRIX[6]=-v;
//        ROTATION_MATRIX[9]=v;

//        ROTATION_MATRIX[1]=-v;
//        ROTATION_MATRIX[4]=v;

            //        IDENTITY_MATRIX2[1]=v1;
//        ROTATION_MATRIX[8]=v*v2;
//       ROTATION_MATRIX[0]= 1.0f - 2.0f*qy*qy - 2.0f*qz*qz;
//       ROTATION_MATRIX[1]= 2.0f*qx*qy - 2.0f*qz*qw;
//       ROTATION_MATRIX[2]= 2.0f*qx*qz + 2.0f*qy*qw;// 0.0f,
//        ROTATION_MATRIX[2]2.0f*qx*qy + 2.0f*qz*qw, 1.0f - 2.0f*qx*qx - 2.0f*qz*qz, 2.0f*qy*qz - 2.0f*qx*qw, 0.0f,
//                2.0f*qx*qz - 2.0f*qy*qw, 2.0f*qy*qz + 2.0f*qx*qw, 1.0f - 2.0f*qx*qx - 2.0f*qy*qy, 0.0f,
//                0.0f, 0.0f, 0.0f, 1.0f);
            glMultMatrix(MODView);
//        GL11.glRotatef(w,x,y,z);
        }*/


       /* public static void glLightfv(int i, int i1, FloatBuffer func_1156_a) {
            GL11.nglLightfv(i, i1, getPointer(func_1156_a));
        }

        public static void glLightModelfv(int i, FloatBuffer func_1156_a) {
            GL11.nglLightModelfv(i, getPointer(func_1156_a));
        }

        public static void glLoadIdentity(int i) {
//        GL21.glBufferData(GL21.GL_ARRAY_BUFFER, matrix.put(0, IDENTITY_MATRIX2), GL21.GL_STREAM_COPY);
            GL11.glMatrixMode(i);
            INCBUF.rewind().put(MATRIX, 0, MATRIX.length);
            GL11.nglLoadMatrixf(getPointer(INCBUF));

        }

        public static void glLoadIdentity() {
            GL11.nglLoadMatrixf(getPointer(matrix2));
        }

        public static void glCallLists(IntBuffer buffer) {
            GL11.nglCallLists(buffer.mark().limit(), GL11.GL_UNSIGNED_INT, getPointer(buffer));
        }*/

        public static void memcpy2(float[] vertices, long handle, int l)
        {
            UNSAFE.copyMemory(vertices, 16, null, handle, l);
        }


        public static void memcpy2(short[] vertices, long handle, long l)
        {
            UNSAFE.copyMemory(vertices, 16, null, handle, l);
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
