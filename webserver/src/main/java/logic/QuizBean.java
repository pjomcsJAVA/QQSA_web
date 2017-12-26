package logic;

import net.thegreshams.firebase4j.error.FirebaseException;
import net.thegreshams.firebase4j.model.FirebaseResponse;
import net.thegreshams.firebase4j.service.Firebase;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.util.*;

@ManagedBean(name= "quiz")
@ApplicationScoped
public class QuizBean {

    private static String SPLIT_REGEX = "[-]";
    private static String FIRE_BASE_URL = "https://test-1ae31.firebaseio.com/"; //https://mds-q-b2d34.firebaseio.com/
    private static long STEP_TIME_SECS = 10;

    private List<Player> players;
    private Firebase firebase;
    private int[] correctAnswers = {1, 2, 3};
    private int step = 0;
    private long lastTime = 0;

    @PostConstruct
    public void init(){
        System.out.println("Entering init quiz");
        players = new ArrayList<>();
        try {
            firebase = new Firebase(FIRE_BASE_URL);
            //firebase.delete();

        } catch (FirebaseException  e) {
            e.printStackTrace();
        }
    }

    /**
     * Perform a step increment in FireBase if a STEP_TIME_SECS have elapsed.
     */
    public void nextStep(){
        System.out.println("Entering nextstep");
        String map = "procedure";
        String key = "step";
        long currentTime = System.currentTimeMillis();

        if(currentTime-lastTime > STEP_TIME_SECS *1000){
            step++;
            Map<String, Object> stepMap = new HashMap<>();
            stepMap.put(key, step);
            FirebaseResponse response = null;
            try {
                response = firebase.put(map, stepMap );
            } catch (Throwable e) {
            }
            System.out.println( "Result of POST:\n" + response );

            lastTime = currentTime;
        }else{
            System.out.println( "Not enough time yet");
        }
    }

    /**
     * Returns a table showing the name and score with all the players registered.
     *
     * @return a list with the players.
     */
    public List<Player> getScoreTable(){
        System.out.println("Entering getscoretable");

        players = new ArrayList<>();
        String usersKey = "users";
        FirebaseResponse response = null;
        try {
            response = firebase.get(usersKey);
        } catch (Throwable e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        System.out.println( "Result of GET:\n" + response);

        Map<String, Object> playersMap = response.getBody();

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
        return players;
    }

    /**
     * Returns a question
     *
     * @return a list with the players.
     */
    public List<Item> getQuestion(){
        System.out.println("Entering getQuestion");

        List<Item> questionList = new ArrayList<>();
        String questionsKey = "questions/question-"+step;
        FirebaseResponse response;
        try {
            response = firebase.get(questionsKey);
        } catch (Throwable e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        System.out.println( "Result of GET:\n" + response);
        Map<String, Object> questionsMap = response.getBody();

        for (Map.Entry entryQuestion : questionsMap.entrySet()) {
            int k = Integer.parseInt(entryQuestion.getKey().toString().split(SPLIT_REGEX)[1]);
            System.out.println("key: "+entryQuestion.getKey()+" "+entryQuestion.getValue());
            questionList.add(new Item(k, entryQuestion.getValue().toString()));

        }
        return questionList;
    }

    /**
     * Getter for the current step.
     *
     * @return the curtent step.
     */
    public int getStep() {
        return step;
    }

}
