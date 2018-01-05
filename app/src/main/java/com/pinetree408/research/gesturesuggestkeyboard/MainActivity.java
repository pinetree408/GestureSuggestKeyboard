package com.pinetree408.research.gesturesuggestkeyboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.pinetree408.research.gesturesuggestkeyboard.util.KeyBoardView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private float touchDownX, touchDownY;
    private long touchDownTime;
    private int posX, posY;
    private int savedPosX, savedPosY;

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

    View suggetListLayout;

    Animation leftAnim;
    Animation rightAnim;
    Animation upAnim;
    Animation downAnim;

    String direct;

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
        posX = 0;
        posY = 0;
        savedPosX = 0;
        savedPosY = 0;

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

        LayoutInflater vi1 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int suggestListId = R.layout.suggest_list;
        suggetListLayout = vi1.inflate(suggestListId, null);

        leftAnim = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left);
        rightAnim = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right);

        downAnim = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        upAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        leftAnim.setDuration(250);
        rightAnim.setDuration(250);
        downAnim.setDuration(250);
        upAnim.setDuration(250);

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

                        float thresholdX = (tempX - touchDownX) / (keyBoardView.getWidth() / 10);
                        float thresholdY = (tempY - touchDownY) / (keyBoardView.getHeight() / 3);

                        if (((thresholdX <= -0.5) || (thresholdX >= 0.5))
                                || ((thresholdY <= -0.5) || (thresholdY >= 0.5)) && (eventTime - touchDownTime > 100)) {

                            keyBoardView.setBackgroundColor(Color.parseColor("#d3d3d3"));
                            if(state.equals("tap")) {
                                state = "move";
                            } else if (state.equals("move")){
                            } else if (state.equals("move-up")) {
                                state = "flicking";
                            } else if (state.equals("flicking")) {
                            } else if (state.equals("flicking-up")) {
                                state = "flicking";
                            } else if (state.equals("move-tap")) {
                                state = "flicking";
                            }

                            suggestMap = getSuggest(inputString);

                            boolean isSuggested = false;
                            if ((posX != tempUnitX) || (posY != tempUnitY)) {
                                isSuggested = true;
                            }

                            Log.d(TAG, posX + ":" + posY);
                            Log.d(TAG, tempUnitX + ":" + tempUnitY);

                            int prevPosX = posX;
                            int prevPosY = posY;

                            posX = tempUnitX;
                            posY = tempUnitY;

                            if (state.equals("flicking")) {
                                posX = posX + savedPosX;
                                posY = posY + savedPosY;
                            }

                            if ((posX != prevPosX) || (posY != prevPosY)) {
                                if (suggetListLayout.getParent() != null) {
                                    removeSuggestionList();
                                }

                                if (posX > prevPosX) {
                                    direct = "right";
                                } else if (posX < prevPosX) {
                                    direct = "left";
                                }
                                if (posY > prevPosY) {
                                    direct = "up";
                                } else if (posY < prevPosY) {
                                    direct = "down";
                                }
                                setSuggestionList();
                            }

                        } else {
                            if (state.equals("tap")) {

                            } else if (state.equals("move")) {

                            } else if (state.equals("move-up")) {
                                state = "move-tap";
                            } else if (state.equals("flicking")) {
                            } else if (state.equals("flicking-up")) {
                                state = "move-tap";
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
                            if (suggestMap.keySet().toArray().length > 1) {
                                resultPrevView.setText("");
                                resultMainView.setText(suggestMap.get(suggestMap.keySet().toArray()[0]).get(0).word);
                                resultNextView.setText(suggestMap.get(suggestMap.keySet().toArray()[1]).get(0).word);
                            } else if (suggestMap.keySet().toArray().length > 0) {
                                resultPrevView.setText("");
                                resultMainView.setText(suggestMap.get(suggestMap.keySet().toArray()[0]).get(0).word);
                                resultNextView.setText("");
                            } else {
                                resultPrevView.setText("");
                                resultMainView.setText("");
                                resultNextView.setText("");
                            }
                        } else if (state.equals("move")) {
                            state = "move-up";
                            savedPosX = posX;
                            savedPosY = posY;
                        } else if (state.equals("move-up")){
                            state = "tap";
                            inputString = resultMainView.getText().toString();
                            inputView.setText(inputString);
                            removeSuggestionList();
                            resultPrevView.setText("");
                            resultMainView.setText("");
                            resultNextView.setText("");
                            keyBoardView.setBackgroundColor(Color.WHITE);
                            posX = 0;
                            posY = 0;
                            savedPosX = 0;
                            savedPosY = 0;
                        } else if (state.equals("flicking")) {
                            state = "flicking-up";
                            savedPosX = posX;
                            savedPosY = posY;
                        } else if (state.equals("flicking-up")) {
                            state = "tap";
                            inputString = resultMainView.getText().toString();
                            inputView.setText(inputString);
                            removeSuggestionList();
                            resultPrevView.setText("");
                            resultMainView.setText("");
                            resultNextView.setText("");
                            keyBoardView.setBackgroundColor(Color.WHITE);
                            posX = 0;
                            posY = 0;
                            savedPosX = 0;
                            savedPosY = 0;
                        } else if (state.equals("move-tap")) {
                            state = "tap";
                            inputString = resultMainView.getText().toString();
                            inputView.setText(inputString);
                            removeSuggestionList();
                            resultPrevView.setText("");
                            resultMainView.setText("");
                            resultNextView.setText("");
                            keyBoardView.setBackgroundColor(Color.WHITE);
                            posX = 0;
                            posY = 0;
                            savedPosX = 0;
                            savedPosY = 0;
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
                        if (suggetListLayout.getParent() != null) {
                            removeSuggestionList();
                        }
                        resultPrevView.setText("");
                        resultMainView.setText("");
                        resultNextView.setText("");
                        state = "tap";
                        keyBoardView.setBackgroundColor(Color.WHITE);
                        posX = 0;
                        posY = 0;
                        savedPosX = 0;
                        savedPosY = 0;
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

    public void setTextSwitcher(TextSwitcher switcher) {
        switcher.removeAllViews();
        switcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(MainActivity.this);
                t.setTextSize(20);
                t.setTypeface(Typeface.MONOSPACE);
                t.setHeight(resultPrevView.getHeight());
                t.setWidth(resultPrevView.getWidth());
                t.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                return t;
            }
        });
        if (direct.equals("left")) {
            switcher.setInAnimation(rightAnim);
            switcher.setOutAnimation(leftAnim);
        } else if (direct.equals("right")) {
            switcher.setInAnimation(leftAnim);
            switcher.setOutAnimation(rightAnim);
        } else if (direct.equals("up")) {
            switcher.setInAnimation(downAnim);
            switcher.setOutAnimation(upAnim);
        } else if (direct.equals("down")) {
            switcher.setInAnimation(upAnim);
            switcher.setOutAnimation(downAnim);
        }
    }

    public void setSuggestionList() {

        addContentView(suggetListLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        TextSwitcher prevFirstView = (TextSwitcher) findViewById(R.id.suggest_list_item01);
        TextSwitcher prevSecond = (TextSwitcher) findViewById(R.id.suggest_list_item02);
        TextSwitcher prevThird = (TextSwitcher) findViewById(R.id.suggest_list_item03);
        TextSwitcher prevForth = (TextSwitcher) findViewById(R.id.suggest_list_item04);

        TextSwitcher mainFirstView = (TextSwitcher) findViewById(R.id.suggest_list_item11);
        TextSwitcher mainSecond = (TextSwitcher) findViewById(R.id.suggest_list_item12);
        TextSwitcher mainThird = (TextSwitcher) findViewById(R.id.suggest_list_item13);
        TextSwitcher mainForth = (TextSwitcher) findViewById(R.id.suggest_list_item14);

        TextSwitcher nextFirstView = (TextSwitcher) findViewById(R.id.suggest_list_item21);
        TextSwitcher nextSecond = (TextSwitcher) findViewById(R.id.suggest_list_item22);
        TextSwitcher nextThird = (TextSwitcher) findViewById(R.id.suggest_list_item23);
        TextSwitcher nextForth = (TextSwitcher) findViewById(R.id.suggest_list_item24);

        setTextSwitcher(prevFirstView);
        setTextSwitcher(prevSecond);
        setTextSwitcher(prevThird);
        setTextSwitcher(prevForth);
        setTextSwitcher(mainFirstView);
        setTextSwitcher(mainSecond);
        setTextSwitcher(mainThird);
        setTextSwitcher(mainForth);
        setTextSwitcher(nextFirstView);
        setTextSwitcher(nextSecond);
        setTextSwitcher(nextThird);
        setTextSwitcher(nextForth);

        if (suggestMap.keySet().toArray().length == 0) {
            resultMainView.setText("");
            mainFirstView.setText("");
            mainSecond.setText("");
            mainThird.setText("");
            mainForth.setText("");
            resultPrevView.setText("");
            prevFirstView.setText("");
            prevSecond.setText("");
            prevThird.setText("");
            prevForth.setText("");
            resultNextView.setText("");
            nextFirstView.setText("");
            nextSecond.setText("");
            nextThird.setText("");
            nextForth.setText("");
            return;
        }

        if (posX > suggestMap.keySet().toArray().length - 1) {
            posX = suggestMap.keySet().toArray().length - 1;
        } else if (posX < 0) {
            posX = 0;
        }

        if (posY > suggestMap.get(suggestMap.keySet().toArray()[posX]).size() - 1) {
            posY = suggestMap.get(suggestMap.keySet().toArray()[posX]).size() - 1;
        } else if (posY < 0) {
            posY = 0;
        }
        String preMainFirstText = resultMainView.getText().toString();

        if (suggestMap.get(suggestMap.keySet().toArray()[posX]).size() > posY + 3) {
            resultMainView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX]).get(posY).word);
            mainFirstView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX]).get(posY).word);
            mainSecond.setText(suggestMap.get(suggestMap.keySet().toArray()[posX]).get(posY + 1).word);
            mainThird.setText(suggestMap.get(suggestMap.keySet().toArray()[posX]).get(posY + 2).word);
            mainForth.setText(suggestMap.get(suggestMap.keySet().toArray()[posX]).get(posY + 3).word);
        } else if (suggestMap.get(suggestMap.keySet().toArray()[posX]).size() > posY + 2) {
            resultMainView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX]).get(posY).word);
            mainFirstView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX]).get(posY).word);
            mainSecond.setText(suggestMap.get(suggestMap.keySet().toArray()[posX]).get(posY + 1).word);
            mainThird.setText(suggestMap.get(suggestMap.keySet().toArray()[posX]).get(posY + 2).word);
            mainForth.setText("");
        } else if (suggestMap.get(suggestMap.keySet().toArray()[posX]).size() > posY + 1) {
            resultMainView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX]).get(posY).word);
            mainFirstView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX]).get(posY).word);
            mainSecond.setText(suggestMap.get(suggestMap.keySet().toArray()[posX]).get(posY + 1).word);
            mainThird.setText("");
            mainForth.setText("");
        } else if (suggestMap.get(suggestMap.keySet().toArray()[posX]).size() > posY) {
            resultMainView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX]).get(posY).word);
            mainFirstView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX]).get(posY).word);
            mainSecond.setText("");
            mainThird.setText("");
            mainForth.setText("");
        } else {
            resultMainView.setText("");
            mainFirstView.setText("");
            mainSecond.setText("");
            mainThird.setText("");
            mainForth.setText("");
        }

        if (!resultMainView.getText().toString().equals(preMainFirstText)) {
            vib.vibrate(100);
        }

        if (posX - 1 < 0) {
            resultPrevView.setText("");
            prevFirstView.setText("");
            prevSecond.setText("");
            prevThird.setText("");
            prevForth.setText("");
        } else {
            if (suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).size() > posY + 3) {
                resultPrevView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).get(posY).word);
                prevFirstView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).get(posY).word);
                prevSecond.setText(suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).get(posY + 1).word);
                prevThird.setText(suggestMap.get(suggestMap.keySet().toArray()[posX- 1]).get(posY + 2).word);
                prevForth.setText(suggestMap.get(suggestMap.keySet().toArray()[posX- 1]).get(posY + 3).word);
            } else if (suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).size() > posY + 2) {
                resultPrevView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).get(posY).word);
                prevFirstView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).get(posY).word);
                prevSecond.setText(suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).get(posY + 1).word);
                prevThird.setText(suggestMap.get(suggestMap.keySet().toArray()[posX- 1]).get(posY + 2).word);
                prevForth.setText("");
            } else if (suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).size() > posY + 1) {
                resultPrevView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).get(posY).word);
                prevFirstView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).get(posY).word);
                prevSecond.setText(suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).get(posY + 1).word);
                prevThird.setText("");
                prevForth.setText("");
            } else if (suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).size() > posY) {
                resultPrevView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).get(posY).word);
                prevFirstView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX - 1]).get(posY).word);
                prevSecond.setText("");
                prevThird.setText("");
                prevForth.setText("");
            } else {
                resultPrevView.setText("");
                prevFirstView.setText("");
                prevSecond.setText("");
                prevThird.setText("");
                prevForth.setText("");
            }
        }

        if (posX + 1 > suggestMap.keySet().toArray().length - 1) {
            resultNextView.setText("");
            nextFirstView.setText("");
            nextSecond.setText("");
            nextThird.setText("");
            nextForth.setText("");
        } else {
            if (suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).size() > posY + 3) {
                resultNextView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).get(posY).word);
                nextFirstView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).get(posY).word);
                nextSecond.setText(suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).get(posY + 1).word);
                nextThird.setText(suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).get(posY + 2).word);
                nextForth.setText(suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).get(posY + 3).word);
            } else if (suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).size() > posY + 2) {
                resultNextView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).get(posY).word);
                nextFirstView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).get(posY).word);
                nextSecond.setText(suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).get(posY + 1).word);
                nextThird.setText(suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).get(posY + 2).word);
                nextForth.setText("");
            } else if (suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).size() > posY + 1) {
                resultNextView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).get(posY).word);
                nextFirstView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).get(posY).word);
                nextSecond.setText(suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).get(posY + 1).word);
                nextThird.setText("");
                nextForth.setText("");
            } else if (suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).size() > posY) {
                resultNextView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).get(posY).word);
                nextFirstView.setText(suggestMap.get(suggestMap.keySet().toArray()[posX + 1]).get(posY).word);
                nextSecond.setText("");
                nextThird.setText("");
                nextForth.setText("");
            } else {
                resultNextView.setText("");
                nextFirstView.setText("");
                nextSecond.setText("");
                nextThird.setText("");
                nextForth.setText("");
            }
        }
    }

    public void removeSuggestionList() {
        ((ViewGroup) suggetListLayout.getParent()).removeView(suggetListLayout);
    }
}
