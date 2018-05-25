package qimonjy.cn.baidutts;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/5/25.
 */

public class AudioTask {
    private static AudioTask instance;

    private ExecutorService executors = Executors.newSingleThreadExecutor();

    public static AudioTask getInstance() {
        if (instance == null) {
            synchronized (AudioTask.class) {
                if (instance == null) instance = new AudioTask();
            }
        }
        return instance;
    }

    public void play(String filePath) {
        if (TextUtils.isEmpty(filePath)) return;
        final File tempFile = new File(filePath);
        if (!tempFile.exists()) return;
        if (tempFile.isDirectory()) return;
        executors.execute(new Runnable() {
            @Override
            public void run() {
                DataInputStream dis = null;
                try {
                    //从音频文件中读取声音
                    dis = new DataInputStream(new BufferedInputStream(new FileInputStream(tempFile)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //最小缓存区
                int bufferSizeInBytes = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                AudioTrack player = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack.MODE_STREAM);
                byte[] data = new byte[bufferSizeInBytes];
                player.play();//开始播放
                while (true) {
                    int i = 0;
                    try {
                        while (dis.available() > 0 && i < data.length) {
                            data[i] = dis.readByte();//录音时write Byte 那么读取时就该为readByte要相互对应
                            i++;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    player.write(data, 0, data.length);
                    if (i != bufferSizeInBytes) //表示读取完了
                    {
                        player.stop();//停止播放
                        player.release();//释放资源
                        break;
                    }
                }
                player = null;
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dis = null;
            }
        });
    }

}
