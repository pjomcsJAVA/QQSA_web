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
    private static final String KEY_USERS = "users";
    private static final String KEY_QUESTIONS= "questions";
    private static final long STEP_TIME_SECS = 10;
    private static final String PROPERTY_URL = "url";
   
    private List<Player> players;
    private Firebase firebase;
    private int step = 0;
    private long lastTime = 0;
    
    private Map<Integer, Question> questionsMap = new HashMap<>();
    

    @PostConstruct
    public void init() {
        System.out.println("Entering init quiz");
        players = new ArrayList<>();
        try {
            String url = getFireBaseUrl(FIRE_BASE_FILE);
            System.out.println("init url : "+url);
            firebase = new Firebase(url);
            //firebase.delete();
            loadQuestions();

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
        final String map = "procedure";
        final String key = "step";
        long currentTime = System.currentTimeMillis();

        if(currentTime - lastTime > STEP_TIME_SECS * 1000){
            if(step < getQuestionsCount()){
                step++;
                
                Thread t = new Thread(new Runnable() {
                @Override
                public void run(){
                    Map<String, Object> stepMap = new HashMap<>();
                    stepMap.put(key, step);
                    FirebaseResponse response = null;
                    try {
                        response = firebase.put(map, stepMap );
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    System.out.println( "Result of POST:\n" + response );
                }
            });
            
            t.start();
            }
            else{
                loadScoreTable();
            }

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
               
                if(key <= questionsMap.size() && questionsMap.get(key).getCorrect() == value){
                    p.incrementScore();
                }
            }
            players.add(p);
        }
        Collections.sort(players);
    }

    
     /**
     * Loads all questions from Firebase.
     *
     */
     private void loadQuestions(){
        System.out.println("Entering loadQuestions");
        
        Map<String, Object> fbQuestionsMap =  getFirebaseMap(KEY_QUESTIONS);
        
        for (Map.Entry<String, Object> entry : fbQuestionsMap.entrySet()) {
            int key = Integer.parseInt(entry.getKey().substring(entry.getKey().length()-1));
            Map<String, Object> questionMap = (Map<String, Object>) entry.getValue();

            Question question = new Question(questionMap);
            System.out.println(question);
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
        return players;
    }

    /**
     * Returns a question
     *
     * @return a .
     */
    public Question getQuestion(){
        System.out.println("Entering getQuestion");
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
    
    

}

