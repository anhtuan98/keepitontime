package vn.eagleeye.keepitontime;

/**
 * Created by natuan on 11/16/16.
 */
public class MyThread extends Thread{
    protected boolean isStop = false;

    public void dispose()
    {
        isStop = true;
    }
    public boolean isRunning()
    {
        return !this.isStop;
    }
}
