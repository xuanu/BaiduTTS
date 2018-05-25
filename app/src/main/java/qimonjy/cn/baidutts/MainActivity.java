package qimonjy.cn.baidutts;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.qimon.studentcircle.R;

import qimonjy.cn.baiduttslib.tts.SpeechUtils;
import qimonjy.cn.baiduttslib.tts.listener.FileSaveListener;
import qimonjy.cn.baiduttslib.tts.util.Md5Utils;


public class MainActivity extends Activity {

    private EditText mInput;

//    private SpeechUtils mSpeech;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SpeechUtils.getInstance().init(this);

        mInput = (EditText) findViewById(R.id.input);

        findViewById(R.id.startSpeak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String speakWord = mInput.getText().toString().trim();
                if (TextUtils.isEmpty(speakWord)) return;
                SpeechUtils.getInstance().speak(speakWord);
            }
        });
        findViewById(R.id.saveSpeak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String speakWord = mInput.getText().toString().trim();
                if (TextUtils.isEmpty(speakWord)) return;
                SpeechUtils.getInstance().synther(speakWord, new SpeechUtils.SyntherListerner() {
                    @Override
                    public void start(String uttid) {

                    }

                    @Override
                    public void finish(String uttid, String filePath) {
                        AudioTask.getInstance().play(filePath);
                    }

                    @Override
                    public void error(String uttid) {

                    }
                });
            }
        });
    }

    public void play(String recordingFile) {
        AudioTask.getInstance().play(recordingFile);
    }

}
