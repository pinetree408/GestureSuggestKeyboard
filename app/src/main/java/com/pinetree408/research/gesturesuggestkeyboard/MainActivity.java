package com.pinetree408.research.gesturesuggestkeyboard;

import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.pinetree408.research.gesturesuggestkeyboard.util.KeyBoardView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int TAP_DURATION = 200;

    private float touchDownX, touchDownY;
    private long touchDownTime;
    private int posX, posY;
    private int posDeltaX, posDeltaY;

    View container;
    TextView resultPrevView;
    TextView resultMainView;
    TextView resultNextView;
    TextView inputView;
    KeyBoardView keyBoardView;

    TreeMap<Integer,ArrayList<Anc>> suggestMap;

    TextView clearView;

    String inputString;

    String state;

    class Anc {
        public String word;
        public int freq;
        public Anc( String word, int freq) {
            this.word = word;
            this.freq = freq;
        }
    }

    public List<Anc> ancList;

    Vibrator vib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        inputString = "";
        state = "tap";

        ancList = new ArrayList<Anc>();
        final InputStream inputStream = getResources().openRawResource(R.raw.word_set);
        getAncList(inputStream);

        container = findViewById(R.id.container);
        resultPrevView = (TextView) findViewById(R.id.result_pre);
        resultMainView = (TextView) findViewById(R.id.result_main);
        resultNextView = (TextView) findViewById(R.id.result_next);
        inputView = (TextView) findViewById(R.id.input);
        keyBoardView = (KeyBoardView) findViewById(R.id.tapboard);
        keyBoardView.setBackgroundColor(Color.WHITE);

        clearView = (TextView) findViewById(R.id.clear);

        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int tempX = (int) event.getAxisValue(MotionEvent.AXIS_X);
                int tempY = (int) event.getAxisValue(MotionEvent.AXIS_Y);
                long eventTime = System.currentTimeMillis();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchDownTime = eventTime;
                        touchDownX = tempX;
                        touchDownY = tempY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int tempUnitX = (int) (tempX - touchDownX) / (keyBoardView.getWidth() / 10);
                        int tempUnitY = (int) (tempY - touchDownY) / (keyBoardView.getHeight() / 3);

                        if ((tempUnitX != posX) || (tempUnitY != posY)) {
                            state = "move";
                            vib.vibrate(100);
                            posX = tempUnitX;
                            posY = tempUnitY;
                            suggestMap = getSuggest(inputString);

                            if (posX > suggestMap.keySet().toArray().length - 1) {
                                posX = suggestMap.keySet().toArray().length - 1;
                            } else if (posX < 0) {
                                posX = 0;
                            }
                            if (posY < 0) {
                                posY = 0;
                            }
                            resultMainView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX]).get(posY).word);

                            if (posX - 1 < 0) {
                                resultPrevView.setText("");
                            } else {
                                if (posY > suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).size() - 1) {
                                    posY = suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).size() - 1;
                                }
                                resultPrevView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).get(posY).word);
                            }

                            if (posX + 1 > suggestMap.keySet().toArray().length - 1) {
                                resultNextView.setText("");
                            } else {
                                if (posY > suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).size() - 1) {
                                    posY = suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).size() - 1;
                                }
                                resultNextView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).get(posY).word);
                            }

                        } else {
                            if (state.equals("tap")) {

                            } else if (state.equals("move")) {

                            } else if (state.equals("move-tap")) {

                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (state.equals("tap")) {
                            if (keyBoardView.getY() + keyBoardView.getHeight() < tempY) {
                                if (inputString.length() != 0) {
                                    inputString = inputString.substring(0, inputString.length() - 1);
                                }
                            } else {
                                String[] params = getInputInfo(event);
                                if (params[0].equals(".")) {
                                    break;
                                }
                                inputString += params[0];
                            }
                            inputView.setText(inputString);

                            suggestMap = getSuggest(inputString);
                            if (suggestMap.keySet().toArray().length > 2) {
                                resultPrevView.setText(suggestMap.get(suggestMap.keySet().toArray()[0]).get(0).word);
                                resultMainView.setText(suggestMap.get(suggestMap.keySet().toArray()[1]).get(0).word);
                                resultNextView.setText(suggestMap.get(suggestMap.keySet().toArray()[2]).get(0).word);
                            } else if (suggestMap.keySet().toArray().length > 1) {
                                resultPrevView.setText(suggestMap.get(suggestMap.keySet().toArray()[0]).get(0).word);
                                resultMainView.setText(suggestMap.get(suggestMap.keySet().toArray()[1]).get(0).word);
                                resultNextView.setText("");
                            } else if (suggestMap.keySet().toArray().length > 0) {
                                resultPrevView.setText(suggestMap.get(suggestMap.keySet().toArray()[0]).get(0).word);
                                resultMainView.setText("");
                                resultNextView.setText("");
                            } else {
                                resultPrevView.setText("");
                                resultMainView.setText("");
                                resultNextView.setText("");
                            }
                        } else if (state.equals("move")) {
                            state = "move-tap";
                        } else if (state.equals("move-tap")) {
                            state = "tap";
                            inputString = resultMainView.getText().toString();
                            inputView.setText(inputString);
                            resultPrevView.setText("");
                            resultMainView.setText("");
                            resultNextView.setText("");
                        }
                        break;
                }
                return true;
            }
        });

        clearView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        inputString = "";
                        inputView.setText(inputString);
                        resultPrevView.setText("");
                        resultMainView.setText("");
                        resultNextView.setText("");
                        state = "tap";
                        keyBoardView.setBackgroundColor(Color.WHITE);
                        break;
                }
                return true;
            }
        });
    }

    public TreeMap<Integer,ArrayList<Anc>> getSuggest(String inputString) {
        TreeMap<Integer,ArrayList<Anc>> tempMap = new TreeMap<>();
        for (Anc anc : ancList) {
            if (anc.word.startsWith(inputString)) {
                if (tempMap.get(anc.word.length()) == null) {
                    tempMap.put(anc.word.length(), new ArrayList<Anc>());
                }
                tempMap.get(anc.word.length()).add(anc);
            }
        }

        return tempMap;
    }

    public void getAncList(InputStream inputStream) {
        try {
            BufferedReader ancFile = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            String[] lines;
            while(( line = ancFile.readLine()) != null) {
                lines = line.split("   ");
                String word = lines[0];
                int freq = Integer.parseInt( lines[1]);
                Anc anc = new Anc(word, freq);
                ancList.add(anc);
            }
        } catch( Exception e) {
            System.err.println(e);
        }
    }

    public String[] getInputInfo(MotionEvent event) {
        double tempX = (double) event.getAxisValue(MotionEvent.AXIS_X);
        double tempY = (double) event.getAxisValue(MotionEvent.AXIS_Y);
        String input = keyBoardView.getKey(tempX - keyBoardView.getX(), tempY - keyBoardView.getY());

        return new String[]{
                String.valueOf(input),
                String.valueOf(tempX),
                String.valueOf(tempY)
        };
    }
}
