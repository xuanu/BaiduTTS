package qimonjy.cn.baidutts;

import android.content.Context;
import android.media.MediaPlayer;
import android.text.InputFilter;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *      author  ：zzx
 *      e-mail  ：zhengzhixuan18@gmail.com
 *      time    ：2017/08/17
 *      desc    ：
 *      version:：1.0
 * </pre>
 *
 * @author zzx
 */

public class MediaUtils {
    private static MediaUtils instance;
    private MediaPlayer mPlayer;

    public static MediaUtils getInstance() {
        if (instance == null) {
            synchronized (MediaUtils.class) {
                if (instance == null) {
                    instance = new MediaUtils();
                }
            }
        }
        return instance;
    }

    public void play(String path) {
        play(Arrays.asList(path));
    }

    public void play(String[] paths) {
        play(Arrays.asList(paths), null);
    }

    public void play(List<String> paths) {
        play(paths, null);
    }

    public void play(List<String> paths, OnPlayer pOnPlayer) {
        if (pOnPlayer != null) addPlayListener(pOnPlayer);
        if (paths == null || paths.isEmpty()) return;
        mPaths.clear();
        mPaths.addAll(paths);
        if (!mPaths.isEmpty()) {
            play(0);
        }
    }

    public void playRaw(Context pContext, int id, OnPlayer pPlayer) {
        if (pPlayer != null) addPlayListener(pPlayer);
        if (mPlayer == null) {
            mPlayer = MediaPlayer.create(pContext, id);
        }
        try {
            mPlayer.start();
        } catch (IllegalStateException e) {
            mPlayer = null;
            mPlayer = MediaPlayer.create(pContext, id);
            mPlayer.start();
        }
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                stop();
            }

        });
        mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer player, int arg1, int arg2) {
                stop();
                return true;
            }
        });
    }


    private List<String> mPaths = new ArrayList<>();

    private void play(final int position) {
        if (mPaths.size() - 1 < position || position < 0) {
            stop();
            return;
        }
        String nowPath = mPaths.get(position);
        if (TextUtils.isEmpty(nowPath)) {
            play(position + 1);
            return;
        }
        if (nowPath.startsWith("http://") || nowPath.startsWith("https://")) {

        } else {
            File tempFile = new File(nowPath);
            if (!tempFile.exists()) {
                play(position+1);
                return;
            }
        }
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        try {
            mPlayer.reset();
        } catch (IllegalStateException e) {
            mPlayer = null;
            mPlayer = new MediaPlayer();
        }
        try {
            mPlayer.reset();
            mPlayer.setDataSource(nowPath);
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    if (mOnPlayerWeakReference != null && mOnPlayerWeakReference.get() != null)
                        mOnPlayerWeakReference.get().onStepStart(position);
                }
            });
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer arg0) {
                    int next = position + 1;
                    if (mPaths.size() - 1 < next || next < 0) stop();
                    else {
                        if (mOnPlayerWeakReference != null && mOnPlayerWeakReference.get() != null)
                            mOnPlayerWeakReference.get().onStepComplete(position);
                        play(next);
                    }
                }

            });
            mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer player, int arg1, int arg2) {
                    stop();
                    return true;
                }
            });
        } catch (IOException e) {
//            stop();
            play(position + 1);
        }
    }

    /***
     * 关闭播放
     */
    public void stop() {
        if (mPlayer != null) {
            try {
                mPlayer.reset();
                mPlayer.release();
                mPlayer.stop();
                mPlayer = null;
            } catch (IllegalStateException E) {
                E.printStackTrace();
            }
        }
        removeListener();
    }


    private void removeListener() {
        if (mOnPlayerWeakReference != null && mOnPlayerWeakReference.get() != null)
            mOnPlayerWeakReference.get().onComplete();
    }


    private WeakReference<OnPlayer> mOnPlayerWeakReference;

    public interface OnPlayer {
        void onComplete();

        void onStepComplete(int i);

        void onStepStart(int i);
    }


    private void addPlayListener(OnPlayer pOnPlayer) {
        if (pOnPlayer == null) return;
        mOnPlayerWeakReference = new WeakReference<OnPlayer>(pOnPlayer);
    }


    public int getPlayCount() {
        if (mPaths == null) return 0;
        return mPaths.size();
    }

}
