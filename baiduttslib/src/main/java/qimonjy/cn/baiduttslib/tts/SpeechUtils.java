package qimonjy.cn.baiduttslib.tts;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import qimonjy.cn.baiduttslib.tts.control.InitConfig;
import qimonjy.cn.baiduttslib.tts.control.MySyntherizer;
import qimonjy.cn.baiduttslib.tts.control.NonBlockSyntherizer;
import qimonjy.cn.baiduttslib.tts.listener.FileSaveListener;
import qimonjy.cn.baiduttslib.tts.util.Md5Utils;
import qimonjy.cn.baiduttslib.tts.util.OfflineResource;

/**
 * Created by Administrator on 2018/2/3.
 */

public class SpeechUtils {
    protected String appId = "11287196";
    protected String appKey = "cG5s02DEojrGG9s0HXtcSKoE";
    protected String secretKey = "7389bea3d9728e0a67641910c4ae865c";
    protected TtsMode ttsMode = TtsMode.MIX;
    protected MySyntherizer synthesizer;
    protected String offlineVoice = OfflineResource.VOICE_DUYY;
    private Context mContext;
    protected Handler mainHandler;

    private static SpeechUtils instance;

    public static SpeechUtils getInstance() {
        if (instance == null) {
            synchronized (SpeechUtils.class) {
                if (instance == null) instance = new SpeechUtils();
            }
        }
        return instance;
    }

    public SpeechUtils() {

    }

    public void init(Context pContext) {
        mContext = pContext.getApplicationContext();
        this.mainHandler = new Handler(mContext.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.obj != null) Log.e("zeffect", "msg:" + msg.obj.toString());
            }
        };
        initialTts();
    }


    public String speak(String word) {
        if (TextUtils.isEmpty(word)) return "";
        if (word.length() > 512) return "";
        String uttId = getUttid(word);
        File saveFile = makeSaveFile(getDestDir(mContext), uttId);
        if (saveFile.isDirectory()) saveFile.delete();
        if (!saveFile.exists()) saveFile.getParentFile().mkdirs();
        else return saveFile.getAbsolutePath();
        int result = synthesizer.speak(word, uttId);
        return saveFile.getAbsolutePath();
    }


    private List<WordSynther> wordSynthers = new ArrayList<>();

    public String synther(String word) {
        return synther(word, null);
    }

    public String synther(String word, SyntherListerner syntherListerner) {
        if (TextUtils.isEmpty(word)) return "";
        if (word.length() > 512) return "";
        String uttId = getUttid(word);
        File saveFile = makeSaveFile(getDestDir(mContext), uttId);
        if (saveFile.isDirectory()) saveFile.delete();
        if (!saveFile.exists()) saveFile.getParentFile().mkdirs();
        else return saveFile.getAbsolutePath();
        if (syntherListerner != null) {
            wordSynthers.add(new WordSynther().setWord(uttId).setSyntherListernerWeakReference(new WeakReference<SyntherListerner>(syntherListerner)));
        }
        int result = synthesizer.synthesize(word, uttId);
        return saveFile.getAbsolutePath();
    }


    private static class WordSynther {
        private String word;
        private WeakReference<SyntherListerner> syntherListernerWeakReference;

        public String getWord() {
            return word;
        }

        public WordSynther setWord(String word) {
            this.word = word;
            return this;
        }

        public WeakReference<SyntherListerner> getSyntherListernerWeakReference() {
            return syntherListernerWeakReference;
        }

        public WordSynther setSyntherListernerWeakReference(WeakReference<SyntherListerner> syntherListernerWeakReference) {
            this.syntherListernerWeakReference = syntherListernerWeakReference;
            return this;
        }
    }

    protected void initialTts() {
        SpeechSynthesizerListener listener = new FileSaveListener(mainHandler, getDestDir(mContext), myLister);
        Map<String, String> params = getParams();
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
        synthesizer = new NonBlockSyntherizer(mContext, initConfig, mainHandler);
    }


    private SyntherListerner myLister = new SyntherListerner() {
        @Override
        public void start(String uttid) {
            for (int i = 0; i < wordSynthers.size(); i++) {
                WordSynther wordSynther = wordSynthers.get(i);
                WeakReference<SyntherListerner> syntherListernerWeakReference = wordSynther.getSyntherListernerWeakReference();
                if (syntherListernerWeakReference == null || syntherListernerWeakReference.get() == null) {
                    wordSynthers.remove(i);
                    i--;
                    continue;
                }
                String tempUttid = wordSynther.getWord();
                if (tempUttid.equals(uttid)) syntherListernerWeakReference.get().start(uttid);
            }
        }

        @Override
        public void finish(String uttid, String filePath) {
            for (int i = 0; i < wordSynthers.size(); i++) {
                WordSynther wordSynther = wordSynthers.get(i);
                WeakReference<SyntherListerner> syntherListernerWeakReference = wordSynther.getSyntherListernerWeakReference();
                if (syntherListernerWeakReference == null || syntherListernerWeakReference.get() == null) {
                    wordSynthers.remove(i);
                    i--;
                    continue;
                }
                String tempUttid = wordSynther.getWord();
                if (tempUttid.equals(uttid))
                    syntherListernerWeakReference.get().finish(uttid, filePath);
            }
        }

        @Override
        public void error(String uttid) {
            for (int i = 0; i < wordSynthers.size(); i++) {
                WordSynther wordSynther = wordSynthers.get(i);
                WeakReference<SyntherListerner> syntherListernerWeakReference = wordSynther.getSyntherListernerWeakReference();
                if (syntherListernerWeakReference == null || syntherListernerWeakReference.get() == null) {
                    wordSynthers.remove(i);
                    i--;
                    continue;
                }
                String tempUttid = wordSynther.getWord();
                if (tempUttid.equals(uttid)) syntherListernerWeakReference.get().error(uttid);
            }
        }
    };


    public interface SyntherListerner {
        void start(String uttid);

        void finish(String uttid, String filePath);

        void error(String uttid);
    }


    public static String getDestDir(Context pTarget) {
        return pTarget.getExternalFilesDir("speech").getAbsolutePath();
    }

    public static String getUttid(String word) {
        return Md5Utils.md5(word);
    }


    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        params.put(SpeechSynthesizer.PARAM_VOLUME, "5");
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");
        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(mContext, voiceType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return offlineResource;
    }

    public static File makeSaveFile(String destDir, String uttrId) {
        return new File(destDir, uttrId + ".pcm");
    }
}
