package window;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;

public class Crawler {

    private String strTopWindowTitleText;

    private String strTargetWindowTitleText;

    private String strTargetWindowClassName;

    private WinDef.HWND hTargetWindow;

    private String strContent;

    public Crawler(String strTopWindowTitleText,
                   String strTargetWindowTitleText,
                   String strTargetWindowClassName) throws Exception {
        this.strTopWindowTitleText = strTopWindowTitleText;
        this.strTargetWindowTitleText = strTargetWindowTitleText;
        this.strTargetWindowClassName = strTargetWindowClassName;

        hTargetWindow = FindWindow();
    }

    public void getInfo() throws Exception {
        //选中窗口
        SelectWindow();

        Robot robot = new Robot();
        robot.setAutoDelay(50);
        //F10
        robot.keyPress(KeyEvent.VK_F10);
        robot.keyRelease(KeyEvent.VK_F10);
        robot.delay(500);

        //Ctrl+A
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_CONTROL);

        //Ctrl+C
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_CONTROL);

        // 获取剪贴板中的内容
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable trans = clipboard.getContents(null);
        if (trans != null) {
            // 判断剪贴板中的内容是否支持文本
            if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                strContent = (String) trans.getTransferData(DataFlavor.stringFlavor);
            }
            else
                throw new Exception("Incorrect clipboard content type!");
        }
        else
            throw new Exception("Clipboard is empty!");

        //选中窗口
        SelectWindow();

        //ESC
        robot.keyPress(KeyEvent.VK_ESCAPE);
        robot.keyRelease(KeyEvent.VK_ESCAPE);

        //向下
        robot.keyPress(KeyEvent.VK_DOWN);
        robot.keyRelease(KeyEvent.VK_DOWN);

        //System.out.println(strContent.substring(0, 100));
        System.out.println(strContent);
    }

    private boolean IsComplete() {
        return true;
    }

    private WinDef.HWND FindWindow() throws Exception {
        //Find top window
        final ArrayList<WinDef.HWND> lHandles = new ArrayList<WinDef.HWND>();
        boolean bResult = User32.INSTANCE.EnumWindows(new WinUser.WNDENUMPROC() {
            public boolean callback(WinDef.HWND h, Pointer p) {
                char[] chWinText = new char[User32.INSTANCE.GetWindowTextLength(h)];
                if (0 == chWinText.length)
                    return true;
                User32.INSTANCE.GetWindowText(h, chWinText, chWinText.length);
                if (String.valueOf(chWinText).contains(strTopWindowTitleText)) {
                    lHandles.add(h);
                }
                return true;
            }
        }, null);
        if (bResult == false || lHandles.size() != 1)
            throw new Exception("Find top window failed!");

        WinDef.HWND hTop = lHandles.get(0);
        lHandles.clear();

        //Find target window
        final char[] chText = new char[1024];
        bResult = User32.INSTANCE.EnumChildWindows(hTop, new WinUser.WNDENUMPROC() {
            public boolean callback(WinDef.HWND h, Pointer p) {
                SetCharArrayValue(chText, 0);
                User32.INSTANCE.GetWindowText(h, chText, chText.length);
                String strWinTitle = CharArrayToString(chText);
                SetCharArrayValue(chText, 0);
                User32.INSTANCE.GetClassName(h, chText, chText.length);
                String strClassName = CharArrayToString(chText);
                if ((strWinTitle.compareTo(strTargetWindowTitleText) == 0 || strWinTitle.contains(strTargetWindowTitleText))
                        && (strClassName.compareTo(strTargetWindowClassName) == 0 || strClassName.contains(strTargetWindowClassName))) {
                    lHandles.add(h);
                }
                return true;
            }
        }, null);
        if (bResult == false || lHandles.size() != 1)
            throw new Exception("Find target window failed!");

        return lHandles.get(0);
    }

    private void SelectWindow() {
        User32.INSTANCE.SetForegroundWindow(hTargetWindow);
        User32.INSTANCE.SetFocus(hTargetWindow);
    }
    //private void PrepareWindow()

    private void SetCharArrayValue(char[] ch, int iVal) {
        for (int i1 = 0; i1 < ch.length; i1++) {
            ch[i1] = (char)iVal;
        }
    }
    private String CharArrayToString(char[] ch) {
        if (ch[0] == 0)
            return "";

        int iCount = 0;
        String str = String.valueOf(ch);
        for (int i1 = 0; i1 < ch.length; i1++) {
            if (ch[i1] != (char)0)
                iCount++;
            else
                break;
        }

        return str.substring(0, iCount);
    }


    public static void main(String[] args) {
        try{
            Crawler crawler = new Crawler("国金太阳", "", "MDIClient");
            crawler.getInfo();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
