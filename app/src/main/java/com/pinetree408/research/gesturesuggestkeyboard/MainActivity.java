package com.pinetree408.research.gesturesuggestkeyboard;

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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private float touchDownX, touchDownY;
    private long touchDownTime;

    View container;
    TextView resultPrevView;
    TextView resultMainView;
    TextView resultNextView;
    TextView inputView;
    KeyBoardView keyBoardView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        long touchTime = eventTime - touchDownTime;
                        if (touchTime > 200) {
                            state = "move";
                            ArrayList<ArrayList<Anc>> tempList = getSuggest(inputString);
                            int unitX = ((int) (tempX - touchDownX)) / 50;
                            int unitY = ((int) (tempY - touchDownY)) / 50;
                            String resultPrev = "";
                            String resultMain = "";
                            String resultNext = "";
                            for (ArrayList<Anc> subList : tempList) {
                                if (subList.get(0).word.length() == (inputString.length() + unitX -1)) {
                                    if (unitY >= subList.size()) {
                                        unitY = subList.size() - 1;
                                    } else if (unitY < 0) {
                                        unitY = 0;
                                    }
                                    resultPrev = subList.get(unitY).word;
                                } else if (subList.get(0).word.length() == (inputString.length() + unitX)) {
                                    if (unitY >= subList.size()) {
                                        unitY = subList.size() - 1;
                                    } else if (unitY < 0) {
                                        unitY = 0;
                                    }
                                    resultMain = subList.get(unitY).word;
                                } else if (subList.get(0).word.length() == (inputString.length() + unitX + 1)) {
                                    if (unitY >= subList.size()) {
                                        unitY = subList.size() - 1;
                                    } else if (unitY < 0) {
                                        unitY = 0;
                                    }
                                    resultNext = subList.get(unitY).word;
                                }
                            }
                            resultPrevView.setText(resultPrev);
                            resultMainView.setText(resultMain);
                            resultNextView.setText(resultNext);
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
                            ArrayList<ArrayList<Anc>> tempList = getSuggest(inputString);
                            String resultPrev = "";
                            String resultMain = "";
                            String resultNext = "";
                            for (ArrayList<Anc> subList : tempList) {
                                if (subList.get(0).word.length() == (inputString.length() - 1)) {
                                    resultPrev = subList.get(0).word;
                                } else if (subList.get(0).word.length() == inputString.length()) {
                                    resultMain = subList.get(0).word;
                                } else if (subList.get(0).word.length() == (inputString.length() + 1)) {
                                    resultNext = subList.get(0).word;
                                }
                            }
                            resultPrevView.setText(resultPrev);
                            resultMainView.setText(resultMain);
                            resultNextView.setText(resultNext);
                        } else {
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
                        break;
                }
                return true;
            }
        });
    }

    public ArrayList<ArrayList<Anc>> getSuggest(String inputString) {
        ArrayList<ArrayList<Anc>> tempList = new ArrayList<>();
        for (Anc anc : ancList) {
            if (anc.word.startsWith(inputString)) {
                if (tempList.size() == 0) {
                    ArrayList<Anc> subTempList = new ArrayList<>();
                    subTempList.add(anc);
                    tempList.add(subTempList);
                } else {
                    boolean addFlag = false;
                    for (ArrayList<Anc> subList : tempList) {
                        if (subList.get(0).word.length() == anc.word.length()) {
                            subList.add(anc);
                            addFlag = true;
                            break;
                        }
                    }
                    if (!addFlag) {
                        ArrayList<Anc> subTempList = new ArrayList<>();
                        subTempList.add(anc);
                        tempList.add(subTempList);
                    }
                }
            }
        }
        return tempList;
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
