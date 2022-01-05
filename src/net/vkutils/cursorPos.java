package vkutils;

import org.lwjgl.glfw.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.JNI.invokeV;
import static vkutils.setup.VkUtils2.window;


public final class cursorPos {
//    X(),
//    Y
//    KEY(1)
    static long monitor=0;

//    static final GLFWMonitorCallback monitorCallback;




    public static double x;
    public static double y;

    public static boolean isPressed =false;
    public static boolean isReleased =true;

    public static boolean isKeyPressed;
    public static boolean isKeyReleased;

    public static int mBut;
    public static boolean isClick;//= isPressed || isReleased;

    public static int keyPressed;
    public static double xoffset;
    public static double yoffset;
    private static boolean isScrolling;

    public static double px;

    public static double py;

    private static  boolean isKeyHeld;

    private static boolean canDoInput;

    private static  double movX;



public static final class invocationcallbacks {
    private static final GLFWErrorCallback errorCallback;
    private static GLFWFramebufferSizeCallback sizeCallback;

    private static final GLFWCursorPosCallback cursorFallback;
    private static final GLFWFramebufferSizeCallback framebuffer;
    private static final GLFWMouseButtonCallback mouseButtonCallBack;
    private static final GLFWKeyCallback keyCallBack;
    private static final GLFWScrollCallback scrollCallback;
    static {
//        glfwInit();
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
//            glfwSetFramebufferSizeCallback(sizeCallback = GLFWFramebufferSizeCallback.create(window));


        glfwSetKeyCallback(window, (keyCallBack = GLFWKeyCallback.create((long window, int key, int scancode, int action, int mods) -> {
            keyPressed = key;
            switch (action) {
                case GLFW_PRESS -> {
//                    System.out.println("Pressed");
                    isKeyPressed = true;
                    isKeyReleased = false;
                    isKeyHeld = false;
                }
                case GLFW_RELEASE -> {
//                    System.out.println("Released");
                    isKeyPressed = false;
                    isKeyReleased = true;
                    isKeyHeld = false;
                }
                case GLFW_REPEAT -> //                    System.out.println("Held");
                        isKeyHeld = true;

            }
            canDoInput = isKeyPressed || !isKeyReleased;
            System.out.println(key + "+" + action + "+" + scancode + "+" + mods);
        })));

        glfwSetMouseButtonCallback(window, mouseButtonCallBack = GLFWMouseButtonCallback.create((window1, key, action, mods) -> {
            mBut = key;
            switch (action) {
                case GLFW_PRESS -> {
                    isPressed = true;
                    isReleased = false;
                }
                case GLFW_RELEASE -> {
                    isPressed = false;
                    isReleased = true;
                }
            }
            System.out.println(key + "+" + action + "+" + mods);
            System.out.println("IsPressed:" + isPressed + "IsReleased: " + isReleased);
        }));
        glfwSetScrollCallback(window, scrollCallback = GLFWScrollCallback.create((window1, xoffset1, yoffset1) -> System.out.println(xoffset1 + "+" + yoffset1)));
        glfwSetFramebufferSizeCallback(window, framebuffer = GLFWFramebufferSizeCallback.create((l, i, i1) -> {

            System.out.println("ReSize");
            glfwSetWindowMonitor(window, monitor, 0, 0, i, i1, -1);

        }));

        glfwSetCursorPosCallback(window, (cursorFallback = GLFWCursorPosCallback.create((window1, xPos, yPos) -> {


            x = xPos;
            y = yPos;
            System.out.println(xPos + "+" + yPos);
        })));




    }
    public static void handleInputs()
    {
        long __functionAddress = Functions.PollEvents;
        invokeV(__functionAddress);
    }
    public static boolean isActive()
    {
        return glfwGetWindowAttrib(window, GLFW_FOCUSED) == 1;
    }

}

    public static void setOrient() {

    }

    public static void setCursorPosition(double i, double j)
    {
        x=i;
        y=j;
    }

//SUper Lazy CUt-COpy-{Paste if the origina INput Keyboard Clas Function of the Same Name to faiclate ease Svae EHance SYnergise Enervate Time when Adjusting thenKEyoard INput Hanldingd FUnctions Through Find/Replace/ CTRL+SHFT+ALT+J Substitution and /DIspalcment Equavenciy Exchange laternictaionlsmenbaiaatabiysededu TSgetede /dedhdvetdfrfcc/ /ESC

    public static boolean isKeyDown(int i)
    {
        return (i==keyPressed)&& !isPressed;
    }

    public static int getEventButton()
    {
        return mBut;
    }

    public static void updateWBuffers()
    {
//        glfwPollEvents();
        glfwSwapBuffers(window);
//        glfwWaitEvents();
    }

    public static boolean getEventKeyState() {
        return isKeyPressed;
    }

    public static boolean isButtonDown(int i) {
        return (i==mBut)&&isPressed;
    }

    public static boolean isKeyPressed() {
        return isPressed;
    }




    public static void handleKeyInput()
    {
        /*if (
        {
            keyPressed, isKeyPressed||isKeyHeld);
//            
            updatePlayerMoveState();
        }
        *//*if (getEventKeyState())*//* {
                    *//*if (getEventKey() == GLFW_KEY_F11) {
                        toggleFullscreen();
                    } else*//* {
            if ( {
                
            }
            *//*switch (keyPressed)
            {
                case GLFW_KEY_ESCAPE-> {
//                    .displayInGameMenu();
                }
                            *//**//*
            }*//*
            int i = 0;
            while (i < 9) {
                if (keyPressed == GLFW_KEY_1 + i) {
                    currentItem = i;
                }
                i++;
            }
        }
    }*/
    }

    public static boolean getEventButtonState()
    {
        return isPressed;
    }

    public static double getEventDWheel()
    {
//        System.out.println(xoffset+"<->"+yoffset);
        if(!isScrolling) {

            isScrolling=true;
            return (yoffset);
        }

            yoffset=0;
            isScrolling=false;
            return 0;
    }
    public static String getKeyName()
    {
        int i = keyPressed;
        String c = glfwGetKeyName(i, i);
        System.out.println(c);
        return c;
    }
    /*public static Optional<String> getEventCharacter()
    {
        //        System.out.println(c);
        return Optional.ofNullable(glfwGetKeyName(keyPressed, glfwGetKeyScancode(keyPressed)));


    }*/

    public static void setGrabbed(boolean b) {
        if(b)
        {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
//            glfwSetCursorPos(window, 0, 0);
            return;
        }
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_FALSE);

    }


    public static void handleKeypresses()
    {
        if (isKeyPressed)
        {

//
           /* *//*if (getEventKeyState())*//* {
            if (keyPressed == GLFW_KEY_F11) {

            } else*/

                /*if (Minecraft.currentScreen != null) {

                }
                switch (keyPressed) {
                    case GLFW_KEY_ESCAPE ->

                    case GLFW_KEY_F1 ->
                    case GLFW_KEY_F3 ->
                    case GLFW_KEY_F5 ->
                    case GLFW_KEY_F7 ->
                    case GLFW_KEY_TAB ->
                    case GLFW_KEY_LEFT_ALT ->
                    case GLFW_KEY_T ->
                    case GLFW_KEY_INSERT ->
                    case GLFW_KEY_F11 ->
                }
                isKeyPressed=false;
                int i = 0;
                while (i < 9) {
                    if (keyPressed == GLFW_KEY_1 + i) {
                        currentItem = i;
                    }
                    i++;
                }*/
            }


    }

    /*public static void getDesktopDisplayMode()
    {
        final List<GLFWVidMode> glfwVidModes = glfwGetVideoModes(glfwGetPrimaryMonitor()).stream().toList();
        swapChainExtent.width(glfwVidModes.get(glfwVidModes.size()-1).width());
        swapChainExtent.height(glfwVidModes.get(glfwVidModes.size()-1).height());
        monitor=glfwGetPrimaryMonitor();
    }

    public static void resetDisplayMode(int a, int b)
    {
        monitor=0;
        swapChainExtent.width(a);
        swapChainExtent.height(b);
    }*/

    //    static final void setXYPos(double v, double v1)
//
//    {
//        xPos=v;
//        yPos=v1
//    }


}
