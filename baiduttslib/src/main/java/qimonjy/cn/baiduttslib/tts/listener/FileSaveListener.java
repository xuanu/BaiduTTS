package qimonjy.cn.baiduttslib.tts.listener;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.baidu.tts.client.SpeechError;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import qimonjy.cn.baiduttslib.tts.SpeechUtils;
import qimonjy.cn.baiduttslib.tts.util.Md5Utils;

/**
 * 保存回调音频流到文件。您也可以直接处理音频流
 * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
 * Created by fujiayi on 2017/9/15.
 */

public class FileSaveListener extends UiMessageListener {
    /**
     * 保存文件的目录
     */
    private String destDir;

    /**
     * 文件
     */
    private File ttsFile;
    /**
     * ttsFile 文件流
     */
    private FileOutputStream ttsFileOutputStream;

    /**
     * ttsFile 文件buffer流
     */
    private BufferedOutputStream ttsFileBufferedOutputStream;


    private SpeechUtils.SyntherListerner syntherListerner;

    public FileSaveListener(Handler mainHandler, String destDir, SpeechUtils.SyntherListerner syntherListerner) {
        super(mainHandler);
        this.destDir = destDir;
        this.syntherListerner = syntherListerner;
    }




    @Override
    public void onSynthesizeStart(String utteranceId) {
        syntherListerner.start(utteranceId);
        // 保存的语音文件是 16K采样率 16bits编码 单声道 pcm文件。
        ttsFile = SpeechUtils.makeSaveFile(destDir, utteranceId);
        try {
            if (ttsFile.exists()) {
                ttsFile.delete();
            }
            ttsFile.createNewFile();
            FileOutputStream ttsFileOutputStream = new FileOutputStream(ttsFile);
            ttsFileBufferedOutputStream = new BufferedOutputStream(ttsFileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 语音流 16K采样率 16bits编码 单声道 。
     *
     * @param utteranceId
     * @param data        二进制语音 ，注意可能有空data的情况，可以忽略
     * @param progress    如合成“百度语音问题”这6个字， progress肯定是从0开始，到6结束。 但progress无法保证和合成到第几个字对应。
     */
    @Override
    public void onSynthesizeDataArrived(String utteranceId, byte[] data, int progress) {
        super.onSynthesizeDataArrived(utteranceId, data, progress);
        try {
            ttsFileBufferedOutputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSynthesizeFinish(String utteranceId) {
        super.onSynthesizeFinish(utteranceId);
        syntherListerner.finish(utteranceId,ttsFile.getAbsolutePath());
        close();
    }

    /**
     * 当合成或者播放过程中出错时回调此接口
     *
     * @param utteranceId
     * @param speechError 包含错误码和错误信息
     */
    @Override
    public void onError(String utteranceId, SpeechError speechError) {
        syntherListerner.error(utteranceId);
        close();
        ttsFile.delete();
        super.onError(utteranceId, speechError);
    }

    /**
     * 关闭流，注意可能stop导致该方法没有被调用
     */
    private void close() {
        if (ttsFileBufferedOutputStream != null) {
            try {
                ttsFileBufferedOutputStream.flush();
                ttsFileBufferedOutputStream.close();
                ttsFileBufferedOutputStream = null;
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        if (ttsFileOutputStream != null) {
            try {
                ttsFileOutputStream.close();
                ttsFileOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sendMessage("关闭文件成功");
    }
}
