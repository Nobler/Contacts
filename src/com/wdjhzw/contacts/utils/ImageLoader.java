package com.wdjhzw.contacts.utils;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import com.wdjhzw.contacts.contact.Contact;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;

public class ImageLoader {
    private static final int THREAD_COUNT = 1;

    private static ImageLoader mInstance;

    /**
     * 任务队列和线程池，共同维护加载图像的Runnable实例
     */
    private LinkedList<Runnable> mTasks;
    private ExecutorService mFixedThreadExecutor;

    /**
     * 信号量，由于线程池内部也有一个阻塞线程，防止加入任务的速度过快，使LIFO效果不明显
     */
    private volatile Semaphore mPoolSemaphore;

    /**
     * 用于维护一个MessageQueue的子线程
     */
    private Thread mThread;

    /**
     * 子线程中处理加载图片请求队列的handler
     */
    private Handler mPoolThreadHander;

    /**
     * 引入一个值为1的信号量，防止mPoolThreadHander未初始化完成造成空指针异常
     */
    private volatile Semaphore mPoolThreadSemaphore = new Semaphore(1);

    /**
     * 缓存联系人头像
     */
    private LruCache<Contact, Bitmap> mPhotoCache;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            PhotoBean bean = (PhotoBean) msg.obj;
            bean.view.setImageBitmap(bean.contact.getPhotoImage());

            AnimationSet animationSet = new AnimationSet(true);
            AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
            // 将alphaAnimation对象添加到animationSet中
            animationSet.addAnimation(alphaAnimation);
            // 设置动画执行时间
            alphaAnimation.setDuration(300);
            // 执行动画
            bean.view.startAnimation(animationSet);
        }
    };

    public static ImageLoader getInstance() {
        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(THREAD_COUNT);
                }
            }
        }
        return mInstance;
    }

    public ImageLoader(int threadCount) {
        mThread = new Thread(new Runnable() {

            @SuppressLint("HandlerLeak")
            @Override
            public void run() {
                try {
                    mPoolThreadSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Looper.prepare();

                mPoolThreadHander = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        try {
                            // 在从任务队列中取出任务，由线程池来执行之前，先请求信号量
                            mPoolSemaphore.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mFixedThreadExecutor.execute(getTask());
                    }
                };

                mPoolThreadSemaphore.release();

                Looper.loop();
            }
        });
        mThread.start();

        // 获取最大可用内存，以此来初始化头像缓存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mPhotoCache = new LruCache<Contact, Bitmap>(cacheSize) {
            protected int sizeOf(Contact key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        mFixedThreadExecutor = Executors.newFixedThreadPool(threadCount);
        mPoolSemaphore = new Semaphore(threadCount);
        mTasks = new LinkedList<Runnable>();
    }

    public void loadImage(final Contact contact, final ImageView view) {
        Bitmap bitmap = mPhotoCache.get(contact);
        // 如果缓存中有该联系人对应的头像，则直接设置
        if (bitmap != null) {
            contact.setPhotoImage(bitmap);
        } else {// 否则从provider中读取联系人头像，并缓存
            addTask((new Runnable() {

                @Override
                public void run() {
                    Bitmap bm = ContactsManager.getInstance().getContactPhoto(
                            contact.getId(), false);

                    contact.setPhotoImage(bm);
                    mPhotoCache.put(contact, bm);

                    Message msg = Message.obtain();
                    msg.obj = new PhotoBean(contact, view);
                    mHandler.sendMessage(msg);

                    // 一个子线程加载完成图片之后，释放信号量
                    mPoolSemaphore.release();
                }
            }));
        }
    }

    private synchronized void addTask(Runnable runnable) {
        try {
            if (mPoolThreadHander == null) {
                // 请求信号量，在mPoolThreadHander初始化完成之后再进行相关操作
                mPoolThreadSemaphore.acquire();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mTasks.add(runnable);
        mPoolThreadHander.sendEmptyMessage(0x110);
    }

    private synchronized Runnable getTask() {
        // 从任务队列的尾部取出任务，实现LIFO的处理效果
        return mTasks.removeLast();
    }

    class PhotoBean {
        Contact contact;
        ImageView view;

        public PhotoBean(Contact contact, ImageView view) {
            this.contact = contact;
            this.view = view;
        }
    }
}
