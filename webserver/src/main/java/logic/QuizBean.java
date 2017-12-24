package logic;

import net.thegreshams.firebase4j.error.FirebaseException;
import net.thegreshams.firebase4j.model.FirebaseResponse;
import net.thegreshams.firebase4j.service.Firebase;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import java.util.*;

@ManagedBean(name= "quiz")
public class QuizBean {

    private List<Player> players;

    private Firebase firebase;

    private int[] answers = {1, 2, 3};


    @PostConstruct
    public void init(){
        players = new ArrayList<>();
        String fireBaseBaseUrl = "https://test-1ae31.firebaseio.com/";
        try {
            firebase = new Firebase(fireBaseBaseUrl);
            //firebase.delete();

        } catch (FirebaseException  e) {
            e.printStackTrace();
        }

    }


    /**
     * Returns a table showing the name and score with all the players registered.
     *
     * @return a list with the players.
     */
    public List<Player> getScoreTable(){
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
            System.out.println(entryPlayer.getKey());

            Player p = new Player(entryPlayer.getKey().toString());

            for (Map.Entry entry : user.entrySet()) {
                System.out.println(entry.getKey()+" : "+entry.getValue());
                String sq = entry.getKey().toString();
                int q = Integer.parseInt(sq.substring(sq.length()-1));// needs improvement
                int a = Integer.parseInt(entry.getValue().toString());

                if(answers[q-1] == a){
                    p.incrementScore();
                }
            }
            players.add(p);
        }
        Collections.sort(players);
        return players;
    }

}
