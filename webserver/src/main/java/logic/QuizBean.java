package logic;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import net.thegreshams.firebase4j.model.FirebaseResponse;
import net.thegreshams.firebase4j.service.Firebase;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ApplicationScoped;

@ManagedBean(name= "quiz")
@ApplicationScoped
public class QuizBean {

    /**
     * Constants
     */
    private static final String SPLIT_REGEX = "[-]";
    private static final String FIRE_BASE_FILE = "firebase";
    private static final String KEY_USERS = "users";
    private static final String KEY_QUESTIONS= "questions";
    private static final String ANSWER_PREFIX = "answer-";
    private static final String PROPERTY_URL = "url";
    private static final int STEP_TIME_SECS = 26; // each step takes 10 secs to complete
    private static final int TOP_COUNT_LIMIT = 5;// the score table only shows the best 5 playeres 
    
    /**
     * Variables
     */
    private final Map<Integer, Question> questionsMap = new HashMap<>();
    private List<Player> players;
    private Firebase firebase;
    private int step = 0; // current step of the quiz
    private long lastTime = 0;
    private int timer;
   
    
    @PostConstruct
    public void init() {
        System.out.println("Entering init quiz");
        players = new ArrayList<>();
        try {
            String url = getFireBaseUrl(FIRE_BASE_FILE);
            firebase = new Firebase(url);
            prepareFireBase();
            loadQuestions();
            setStep(0, System.currentTimeMillis());//prepare quiz in mobile app

        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Perform a step increment in FireBase if a STEP_TIME_SECS have elapsed.
     */
    public void nextStep(){
        System.out.println("Entering nextStep with step "+step);
       
        
        final long currentTime = System.currentTimeMillis();
        
        if(currentTime - lastTime > STEP_TIME_SECS * 1000){
            if(step < getQuestionsCount()){
                step++;
                timer = STEP_TIME_SECS;
                Thread tTimer = new Thread(new Runnable() {
                    @Override
                    public void run(){
                        while(timer > 0){
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(QuizBean.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            timer--;
                        }
                        System.out.println("Timmer done in time of (ms): " + (System.currentTimeMillis()- currentTime));
                    }
                });
                tTimer.start();
                
                Thread tStep = new Thread(new Runnable() {
                    @Override
                    public void run(){
                        setStep(step, currentTime);
                    }
                });
                
                Thread tStatistics = new Thread(new Runnable() {
                    @Override
                    public void run(){
                        try {
                            Thread.sleep(STEP_TIME_SECS*1000);// waits for mobile app set the answers in firebase
                        } catch (InterruptedException ex) {
                            Logger.getLogger(QuizBean.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        loadStatistics();
                        System.out.println("loadStatistics done in time of (ms): " + (System.currentTimeMillis()- currentTime));
                        if(step == getQuestionsCount()){
                            loadScoreTable();
                        }
                    }
                });

                tStep.start();
                tStatistics.start();
            }
            lastTime = currentTime;
        }else{
            System.out.println( "Not enough time yet");
        }
    }
    
    private void setStep(int step, long currentTime) {
        final String map = "procedure";
        final String keyStep = "step";
        final String keyTimeStamp = "timestamp";
        
        Map<String, Object> stepMap = new HashMap<>();
        stepMap.put(keyStep, step);
        stepMap.put(keyTimeStamp, currentTime);
        FirebaseResponse response = null;
        try {
            response = firebase.put(map, stepMap );
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println( "Result of POST:\n" + response );
    }

    /**
     * Loads the score table form firebase.
     *
     */
    private void loadScoreTable(){
        System.out.println("Entering loadScoreTable");
        
        Map<String, Object> playersMap = getFirebaseMap(KEY_USERS);

        for (Map.Entry entryPlayer : playersMap.entrySet()) {

            Map<String, Object> user = (Map<String, Object>) entryPlayer.getValue();
            Player p = new Player(entryPlayer.getKey().toString());

            for (Map.Entry entryAnswer : user.entrySet()) {
                String ansKey = entryAnswer.getKey().toString().split(SPLIT_REGEX)[1];

                int key = Integer.parseInt(ansKey);
                int value = Integer.parseInt(entryAnswer.getValue().toString());
               
                if(key <= questionsMap.size() && questionsMap.get(key).getCorrect() == value){
                    p.incrementScore();
                }
            }
            players.add(p);
        }
        Collections.sort(players);
    }
    
    private void loadStatistics(){
        System.out.println("Entering loadStatistics");

        Question question = getQuestion();
        
        Map<String, Object> playersMap = getFirebaseMap(KEY_USERS);

        for (Map.Entry entryPlayer : playersMap.entrySet()) {
            Map<String, Object> user = (Map<String, Object>) entryPlayer.getValue();
            
            int option =0;
            try{
                option= (Integer) user.get(ANSWER_PREFIX+step);
            }catch (NullPointerException e){
                System.out.println("User retrived do not have answer-"+step+" defined! "+e.getMessage());
            }
            question.incrementItemByIndex(option);
        }
    }

    
    private void prepareFireBase() {
        System.out.println("Entering prepareFireBase");

        Map<String, Object> playersMap = new HashMap<>();
        FirebaseResponse response = null;
        try {
            response = firebase.delete(KEY_USERS);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println( "Result of POST:\n" + response );
    }

    
     /**
     * Loads all questions from Firebase.
     *
     */
     private void loadQuestions(){
        System.out.println("Entering loadQuestions");
        
        Map<String, Object> fbQuestionsMap =  getFirebaseMap(KEY_QUESTIONS);
        
        for (Map.Entry<String, Object> entry : fbQuestionsMap.entrySet()) {
            
            int key = Integer.parseInt(entry.getKey().split(SPLIT_REGEX)[1]);
            Map<String, Object> questionMap = (Map<String, Object>) entry.getValue();

            Question question = new Question(questionMap);
            questionsMap.put(key, question);
        }
    }


    private Map<String, Object> getFirebaseMap(String path){
        Map<String, Object> map = new HashMap<>();
        try {
            FirebaseResponse response = firebase.get(path);
            System.out.println( "Result of GET:\n" + response);
            map= response.getBody();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return map;
    }

    private String getFireBaseUrl(String fireBaseFile) throws Exception {
        final URL url = this.getClass().getResource(fireBaseFile);
        InputStream is = url.openStream();
        Properties p = new Properties();
        p.load(is);
        return p.getProperty(PROPERTY_URL);
    }

    /**
     * Returns a table showing the name and score with all the players registered.
     *
     * @return a list with the players.
     */
    public List<Player> getScoreTable(){
        System.out.println("Entering getScoreTable");
        List<Player> playersTop5 = new ArrayList<>();
        int limit = players.size() < 5 ? players.size() : TOP_COUNT_LIMIT;
        for (int i = 0; i < limit; i++) {
            playersTop5.add(players.get(i));
        }
        Collections.sort(playersTop5);
        return playersTop5;
    }

    /**
     * Returns a question
     *
     * @return a .
     */
    public Question getQuestion(){
        System.out.println("Entering getQuestion size: "+questionsMap.size()+" step: "+step);
        return questionsMap.get(step);
    }

    /**
     * Getter for the current step.
     *
     * @return the curtent step.
     */
    public int getStep() {
        return step;
    }
    
     /**
     * Getter for the total number of questions available.
     *
     * @return the total number of questions.
     */
    public int getQuestionsCount() {
        return questionsMap.size();
    }
    
    public int getTimer() {
        return timer;
    }
    
    public void showQuestionResults(int step){
        System.out.println("Entering showQuestionResults");
        questionsMap.get(step).setShowResults(true);
    }

    
}

