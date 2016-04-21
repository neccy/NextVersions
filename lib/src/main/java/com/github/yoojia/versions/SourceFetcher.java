package com.github.yoojia.versions;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author YOOJIA CHEN (yoojiachen@gmail.com)
 * @since 2.0
 */
class SourceFetcher {

    private final OnVersionHandler mOnVersionHandler;
    private final Queue<Source> mSourceQueue = new ConcurrentLinkedQueue<>();
    private final Thread mThread;

    SourceFetcher(OnVersionHandler onVersionHandler) {
        mOnVersionHandler = onVersionHandler;
        mThread = new Thread(new Runnable() {

            @Override public void run() {
                while (! Thread.currentThread().isInterrupted()) {
                    final Source source = mSourceQueue.poll();
                    if (source != null) {
                        mOnVersionHandler.onVersion(source.versionFromSource());
                    }else{
                        await();
                    }
                }
                System.err.println("- Source fetcher daemon thread STOP !!!");
            }

            private void await(){
                synchronized (mThread) {
                    try {
                        mThread.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

        });
        mThread.setDaemon(true);
        mThread.start();
    }

    public void submit(List<Source> sources){
        for (Source source : sources) {
            mSourceQueue.offer(source);
        }
        synchronized (mThread) {
            mThread.notifyAll();
        }
    }

    public interface OnVersionHandler {

        boolean onVersion(Version version);

    }
}
