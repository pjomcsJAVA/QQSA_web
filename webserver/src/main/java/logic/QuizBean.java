package logic;

import net.thegreshams.firebase4j.model.FirebaseResponse;
import net.thegreshams.firebase4j.service.Firebase;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

@ManagedBean(name= "quiz")
@ApplicationScoped
public class QuizBean {

    private static final String SPLIT_REGEX = "[-]";
    private static final String FIRE_BASE_FILE = "firebase";
    private static final String KEY_CORRECT = "correct";
    private static final String KEY_EXPLANATION = "explanation";
    private static final String KEY_USERS = "users";
    private static final long STEP_TIME_SECS = 10;
    private static final String PROPERTY_URL = "url";

    private List<Item> currentQuestion = new ArrayList<>();
    private List<Player> players;
    private Firebase firebase;
    private int[] correctAnswers = {1, 2, 3};// only for testing
    private int step = 0;
    private long lastTime = 0;
    private int questionCount = 2; // get this variable from fb

    private String currentExplanation;

    @PostConstruct
    public void init() {
        System.out.println("Entering init quiz");
        players = new ArrayList<>();
        try {
            String url = getFireBaseUrl(FIRE_BASE_FILE);
            System.out.println("init url : "+url);
            firebase = new Firebase(url);
            //firebase.delete();

        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Perform a step increment in FireBase if a STEP_TIME_SECS have elapsed.
     */
    public void nextStep(){
        System.out.println("Entering nextStep");
        String map = "procedure";
        String key = "step";
        long currentTime = System.currentTimeMillis();

        if(currentTime - lastTime > STEP_TIME_SECS * 1000){
            if(step < questionCount){
                step++;
                loadQuestion();
            }
            else{
                loadScoreTable();
            }

            Map<String, Object> stepMap = new HashMap<>();
            stepMap.put(key, step);
            FirebaseResponse response = null;
            try {
                response = firebase.put(map, stepMap );
            } catch (Throwable e) {
                e.printStackTrace();
            }
            System.out.println( "Result of POST:\n" + response );

            lastTime = currentTime;
        }else{
            System.out.println( "Not enough time yet");
        }
    }

    /**
     * Loads the score table form firebase.
     *
     */
    private void loadScoreTable(){
        System.out.println("Entering loadScoreTable");
        players = new ArrayList<>();

        Map<String, Object> playersMap = getFirebaseMap(KEY_USERS);

        for (Map.Entry entryPlayer : playersMap.entrySet()) {

            Map<String, Object> user = (Map<String, Object>) entryPlayer.getValue();
            Player p = new Player(entryPlayer.getKey().toString());

            for (Map.Entry entry : user.entrySet()) {
                System.out.println(entry.getKey()+" : "+entry.getValue());
                String sq = entry.getKey().toString().split(SPLIT_REGEX)[1];

                int key = Integer.parseInt(sq);
                int value = Integer.parseInt(entry.getValue().toString());

                if(correctAnswers[key-1] == value){
                    p.incrementScore();
                }
            }
            players.add(p);
        }
        Collections.sort(players);
    }

    /**
     * Loads a question based on a step from Firebase.
     *
     */
    private void loadQuestion(){
        System.out.println("Entering getQuestion with step "+step);

        Map<String, Object> questionsMap =  getFirebaseMap("questions/question-"+step);

        if(!questionsMap.isEmpty()){
            currentQuestion.clear();//clear old data
            System.out.println("clear data question "+questionsMap.get(KEY_CORRECT));
        }else{
            return;
        }
        int correct = Integer.parseInt(questionsMap.get(KEY_CORRECT).toString());
        currentExplanation = questionsMap.get(KEY_EXPLANATION).toString();

        for (Map.Entry entryQuestion : questionsMap.entrySet()) {
            String key = entryQuestion.getKey().toString();

            if(key.split(SPLIT_REGEX).length == 2){
                int k = Integer.parseInt(key.split(SPLIT_REGEX)[1]);
                currentQuestion.add(new Item(k, entryQuestion.getValue().toString(), k==correct));
            }
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
        return players;
    }

    /**
     * Returns a question
     *
     * @return a .
     */
    public List<Item> getQuestion(){
        return currentQuestion;
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
     * Getter for the explanation matching the @currentQuestion
     *
     * @return currentExplanation
     */
    public String getExplanation() {
        return currentExplanation;
    }

}
