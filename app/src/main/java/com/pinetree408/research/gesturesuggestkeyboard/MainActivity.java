package com.pinetree408.research.gesturesuggestkeyboard;

import android.content.Context;
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
import com.pinetree408.research.gesturesuggestkeyboard.util.Mackenzie;
import com.pinetree408.research.gesturesuggestkeyboard.util.Util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private float touchDownX, touchDownY;
    private long touchDownTime;
    private int posX, posY;
    private int savedPosX, savedPosY;

    TextView resultPrevView;
    TextView resultMainView;
    TextView resultNextView;
    TextView inputView;
    KeyBoardView keyBoardView;

    TextView spaceView;
    TextView backspaceView;

    String inputString;
    String inputWord;

    String state;

    View suggestedWordMapLayout;

    Animation leftAnim;
    Animation rightAnim;
    Animation upAnim;
    Animation downAnim;

    String direct;

    public List<String> wordList;

    Vibrator vib;

    String[] mackenzieSet;
    TextView targetView;
    Random random;

    Long startTime;
    TextView wpmView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wpmView = (TextView) findViewById(R.id.wpm);
        targetView = (TextView) findViewById(R.id.target);

        inputView = (TextView) findViewById(R.id.input);
        resultPrevView = (TextView) findViewById(R.id.result_pre);
        resultMainView = (TextView) findViewById(R.id.result_main);
        resultNextView = (TextView) findViewById(R.id.result_next);
        keyBoardView = (KeyBoardView) findViewById(R.id.tapboard);

        spaceView = (TextView) findViewById(R.id.space);
        backspaceView = (TextView) findViewById(R.id.backspace);

        mackenzieSet = Mackenzie.mackenzieSet;
        random = new Random();
        targetView.setText(mackenzieSet[random.nextInt(mackenzieSet.length)]);

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        inputString = "";
        inputWord = "";
        stateInitialize();

        wordList = getWordList(getResources().openRawResource(R.raw.word_set));

        suggestedWordMapLayout = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.suggest_list, null);

        leftAnim = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        rightAnim = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        downAnim = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        upAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        leftAnim.setDuration(250);
        rightAnim.setDuration(250);
        downAnim.setDuration(250);
        upAnim.setDuration(250);

        findViewById(R.id.container).setOnTouchListener(new View.OnTouchListener() {
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
                        int tempUnitX = (int) (tempX - touchDownX) / (keyBoardView.getWidth() / 15);
                        int tempUnitY = (int) (tempY - touchDownY) / (keyBoardView.getHeight() / 5);

                        float thresholdX = (tempX - touchDownX) / (keyBoardView.getWidth() / 15);
                        float thresholdY = (tempY - touchDownY) / (keyBoardView.getHeight() / 5);

                        if (((thresholdX <= -0.5) || (thresholdX >= 0.5))
                                || ((thresholdY <= -0.5) || (thresholdY >= 0.5))
                                && (eventTime - touchDownTime > 100)) {
                            // Move Threshold
                            switch (state) {
                                case "tap":
                                    state = "move";
                                    break;
                                case "move":
                                    break;
                                case "move-up":
                                    state = "flicking";
                                    break;
                                case "flicking":
                                    break;
                                case "flicking-up":
                                    state = "flicking";
                                    break;
                                case "move-tap":
                                    state = "flicking";
                                    break;
                            }

                            int prevPosX = posX;
                            int prevPosY = posY;

                            posX = tempUnitX;
                            posY = tempUnitY;

                            if (state.equals("flicking")) {
                                posX = posX + savedPosX;
                                posY = posY + savedPosY;
                            }

                            if ((posX != prevPosX) || (posY != prevPosY)) {
                                removeSuggestionList();
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
                                setSuggestedWordMapLayout(inputWord);
                            }
                        } else {
                            // Tap
                            switch (state) {
                                case "tap":
                                    break;
                                case "move":
                                    break;
                                case "move-up":
                                    state = "move-tap";
                                    break;
                                case "flicking":
                                    break;
                                case "flicking-up":
                                    state = "move-tap";
                                    break;
                                case "move-tap":
                                    break;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        switch (state) {
                            case "tap":
                                String[] params = Util.getInputInfo(keyBoardView, event);
                                if (!params[0].equals(".")) {
                                    if (startTime == null) {
                                        startTime = System.currentTimeMillis();
                                    }

                                    inputWord += params[0];
                                    inputView.setText(inputString + inputWord);
                                    if (inputView.getText().toString().equals(targetView.getText().toString().toLowerCase())) {
                                        double minute = ((System.currentTimeMillis() - startTime) / 1000.0) / 60.0;
                                        double numWord = targetView.getText().toString().replace(" ", "").length() / 5.0;
                                        startTime = null;
                                        wpmView.setText(String.valueOf((numWord / minute)));
                                        targetView.setText(mackenzieSet[random.nextInt(mackenzieSet.length)]);
                                        // initialize
                                        inputWord = "";
                                        inputString = "";
                                        inputView.setText(inputString + inputWord);
                                        removeSuggestionList();
                                        setResultView("", "", "");
                                        stateInitialize();
                                    } else {
                                        setSuggestedWordMapToList(inputWord);
                                    }
                                }
                                break;
                            case "move":
                                state = "move-up";
                                savedPosX = posX;
                                savedPosY = posY;
                                break;
                            case "move-up":
                                resetSuggest(resultMainView.getText().toString());
                                break;
                            case "flicking":
                                state = "flicking-up";
                                savedPosX = posX;
                                savedPosY = posY;
                                break;
                            case "flicking-up":
                                resetSuggest(resultMainView.getText().toString());
                                break;
                            case "move-tap":
                                resetSuggest(resultMainView.getText().toString());
                                break;
                        }
                        break;
                }
                return true;
            }
        });

        backspaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        if (startTime == null) {
                            startTime = System.currentTimeMillis();
                        }
                        removeSuggestionList();
                        stateInitialize();
                        if (inputWord.length() != 0) {
                            inputWord = inputWord.substring(0, inputWord.length() - 1);
                        } else {
                            if (inputString.length() != 0) {
                                char end = inputString.charAt(inputString.length() - 1);
                                if (end == ' '){
                                    inputString = inputString.substring(0, inputString.length() - 1);
                                    inputWord = "";
                                } else {
                                    String[] wordList = inputString.split(" ");
                                    inputWord = wordList[wordList.length - 1];
                                    inputWord = inputWord.substring(0, inputWord.length() - 1);
                                    if (wordList.length == 1) {
                                        inputString = "";
                                    } else {
                                        wordList = Arrays.copyOfRange(wordList, 0, wordList.length - 1);
                                        inputString = Util.strJoin(wordList, " ") + " ";
                                    }
                                }
                            }
                        }
                        inputView.setText(inputString + inputWord);
                        setSuggestedWordMapToList(inputWord);

                        if (inputView.getText().toString().equals(targetView.getText().toString().toLowerCase())) {
                            double minute = ((System.currentTimeMillis() - startTime) / 1000.0) / 60.0;
                            double numWord = targetView.getText().toString().replace(" ", "").length() / 5.0;
                            startTime = null;
                            wpmView.setText(String.valueOf((numWord / minute)));
                            targetView.setText(mackenzieSet[random.nextInt(mackenzieSet.length)]);
                            // initialize
                            inputWord = "";
                            inputString = "";
                            inputView.setText(inputString + inputWord);
                            removeSuggestionList();
                            setResultView("", "", "");
                            stateInitialize();
                        }
                        break;
                }
                return true;
            }
        });

        spaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        if (startTime == null) {
                            startTime = System.currentTimeMillis();
                        }
                        inputString = inputString + inputWord;
                        inputString = inputString + " ";
                        inputWord = "";
                        inputView.setText(inputString + inputWord);
                        removeSuggestionList();
                        setResultView("", "", "");
                        stateInitialize();
                        break;
                }
                return true;
            }
        });
    }

    public void setSuggestedWordMapToList(String inputWord) {
        TreeMap<Integer,ArrayList<String>> getSuggestedWordMap = getSuggestedWordMap(inputWord);
        Integer[] wordLengthSet = getSuggestedWordMap.keySet().toArray(new Integer[getSuggestedWordMap.keySet().size()]);
        if (wordLengthSet.length > 1) {
            setResultView("", getSuggestedWordMap.get(wordLengthSet[0]).get(0), getSuggestedWordMap.get(wordLengthSet[1]).get(0));
        } else if (wordLengthSet.length > 0) {
            setResultView("", getSuggestedWordMap.get(wordLengthSet[0]).get(0), "");
        } else {
            setResultView("", "", "");
        }
    }

    public void stateInitialize() {
        state = "tap";
        posX = 0;
        posY = 0;
        savedPosX = 0;
        savedPosY = 0;
    }

    public void resetSuggest(String input) {
        inputWord = input;
        inputString = inputString + inputWord;
        inputWord = "";
        inputView.setText(inputString + inputWord);
        removeSuggestionList();
        setResultView("", "", "");
        stateInitialize();
        if (inputView.getText().toString().equals(targetView.getText().toString().toLowerCase())) {
            double minute = ((System.currentTimeMillis() - startTime) / 1000.0) / 60.0;
            double numWord = targetView.getText().toString().replace(" ", "").length() / 5.0;
            startTime = null;
            wpmView.setText(String.valueOf((numWord / minute)));
            targetView.setText(mackenzieSet[random.nextInt(mackenzieSet.length)]);
            // initialize
            inputWord = "";
            inputString = "";
            inputView.setText(inputString + inputWord);
            removeSuggestionList();
            setResultView("", "", "");
            stateInitialize();
        } else {
            inputString = inputString + " ";
            inputView.setText(inputString + inputWord);
        }
    }

    public void setResultView(String prev, String main, String next) {
        resultPrevView.setText(prev);
        resultMainView.setText(main);
        resultNextView.setText(next);
    }

    public TreeMap<Integer,ArrayList<String>> getSuggestedWordMap(String inputWord) {
        TreeMap<Integer,ArrayList<String>> tempMap = new TreeMap<>();
        for (String word : wordList) {
            if (word.startsWith(inputWord)) {
                if (tempMap.get(word.length()) == null) {
                    tempMap.put(word.length(), new ArrayList<String>());
                }
                tempMap.get(word.length()).add(word);
            }
        }

        return tempMap;
    }

    public ArrayList<String> getWordList(InputStream inputStream) {
        ArrayList<String> wordList = new ArrayList<>();
        try {
            BufferedReader wordFile = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            String[] lines;
            while(( line = wordFile.readLine()) != null) {
                lines = line.split("   ");
                String word = lines[0];
                wordList.add(word);
            }
        } catch( Exception e) {
            System.err.println(e);
        }
        return wordList;
    }

    public void setTextSwitcher(TextSwitcher switcher) {
        switcher.removeAllViews();
        switcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(MainActivity.this);
                t.setTextSize(15);
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

    public void setSuggestedWordMapLayout(String inputWord) {
        addContentView(suggestedWordMapLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        TreeMap<Integer,ArrayList<String>> getSuggestedWordMap = getSuggestedWordMap(inputWord);
        Integer[] wordLengthSet = getSuggestedWordMap.keySet().toArray(new Integer[getSuggestedWordMap.keySet().size()]);

        LinearLayout preSuggestViewGroup = (LinearLayout) findViewById(R.id.first_suggest_list_item);
        LinearLayout mainSuggestViewGroup = (LinearLayout) findViewById(R.id.second_suggest_list_item);
        LinearLayout nextSuggestViewGroup = (LinearLayout) findViewById(R.id.third_suggest_list_item);

        for (int i = 0; i < preSuggestViewGroup.getChildCount(); i++) {
            TextSwitcher ts = (TextSwitcher) preSuggestViewGroup.getChildAt(i);
            setTextSwitcher(ts);
        }
        for (int i = 0; i < mainSuggestViewGroup.getChildCount(); i++) {
            TextSwitcher ts = (TextSwitcher) mainSuggestViewGroup.getChildAt(i);
            setTextSwitcher(ts);
        }
        for (int i = 0; i < nextSuggestViewGroup.getChildCount(); i++) {
            TextSwitcher ts = (TextSwitcher) nextSuggestViewGroup.getChildAt(i);
            setTextSwitcher(ts);
        }

        if (wordLengthSet.length == 0) {
            setResultView("", "", "");
            for (int i = 0; i < preSuggestViewGroup.getChildCount(); i++) {
                TextSwitcher ts = (TextSwitcher) preSuggestViewGroup.getChildAt(i);
                ts.setText("");
            }
            for (int i = 0; i < mainSuggestViewGroup.getChildCount(); i++) {
                TextSwitcher ts = (TextSwitcher) mainSuggestViewGroup.getChildAt(i);
                ts.setText("");
            }
            for (int i = 0; i < nextSuggestViewGroup.getChildCount(); i++) {
                TextSwitcher ts = (TextSwitcher) nextSuggestViewGroup.getChildAt(i);
                ts.setText("");
            }
            return;
        }

        if (posX > wordLengthSet.length - 1) {
            posX = wordLengthSet.length - 1;
        } else if (posX < 0) {
            posX = 0;
        }

        if (posY > getSuggestedWordMap.get(wordLengthSet[posX]).size() - 1) {
            posY = getSuggestedWordMap.get(wordLengthSet[posX]).size() - 1;
        } else if (posY < 0) {
            posY = 0;
        }
        String preMainFirstText = resultMainView.getText().toString();

        if (getSuggestedWordMap.get(wordLengthSet[posX]).size() > posY) {
            resultMainView.setText(getSuggestedWordMap.get(wordLengthSet[posX]).get(posY));
        } else {
            resultMainView.setText("");
        }
        for (int i = 0; i < mainSuggestViewGroup.getChildCount(); i++) {
            TextSwitcher ts = (TextSwitcher) mainSuggestViewGroup.getChildAt(i);
            if (getSuggestedWordMap.get(wordLengthSet[posX]).size() > posY + i) {
                ts.setText(getSuggestedWordMap.get(wordLengthSet[posX]).get(posY + i));
            } else {
                ts.setText("");
            }
        }

        if (!resultMainView.getText().toString().equals(preMainFirstText)) {
            vib.vibrate(100);
        }

        if (posX - 1 < 0) {
            resultPrevView.setText("");
            for (int i = 0; i < preSuggestViewGroup.getChildCount(); i++) {
                TextSwitcher ts = (TextSwitcher) preSuggestViewGroup.getChildAt(i);
                ts.setText("");
            }
        } else {
            if (getSuggestedWordMap.get(wordLengthSet[posX - 1]).size() > posY) {
                resultPrevView.setText(getSuggestedWordMap.get(wordLengthSet[posX - 1]).get(posY));
            } else {
                resultPrevView.setText("");
            }
            for (int i = 0; i < preSuggestViewGroup.getChildCount(); i++) {
                TextSwitcher ts = (TextSwitcher) preSuggestViewGroup.getChildAt(i);
                if (getSuggestedWordMap.get(wordLengthSet[posX - 1]).size() > posY + i) {
                    ts.setText(getSuggestedWordMap.get(wordLengthSet[posX - 1]).get(posY + i));
                } else {
                    ts.setText("");
                }
            }
        }

        if (posX + 1 > wordLengthSet.length - 1) {
            resultNextView.setText("");
            for (int i = 0; i < nextSuggestViewGroup.getChildCount(); i++) {
                TextSwitcher ts = (TextSwitcher) nextSuggestViewGroup.getChildAt(i);
                ts.setText("");
            }
        } else {
            if (getSuggestedWordMap.get(wordLengthSet[posX + 1]).size() > posY) {
                resultNextView.setText(getSuggestedWordMap.get(wordLengthSet[posX + 1]).get(posY));
            } else {
                resultNextView.setText("");
            }
            for (int i = 0; i < nextSuggestViewGroup.getChildCount(); i++) {
                TextSwitcher ts = (TextSwitcher) nextSuggestViewGroup.getChildAt(i);
                if (getSuggestedWordMap.get(wordLengthSet[posX + 1]).size() > posY + i) {
                    ts.setText(getSuggestedWordMap.get(wordLengthSet[posX + 1]).get(posY + i));
                } else {
                    ts.setText("");
                }
            }
        }
    }

    public void removeSuggestionList() {
        if (suggestedWordMapLayout.getParent() != null) {
            ((ViewGroup) suggestedWordMapLayout.getParent()).removeView(suggestedWordMapLayout);
        }
    }
}
