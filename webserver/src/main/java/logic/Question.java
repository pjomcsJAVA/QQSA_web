/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author vsazevedo
 */
public class Question {
    
    private String question;
    
    private String explanation;
    
    private int correct;

    private List<Item> answers = new ArrayList<>(); 

    public Question(String question, String explanation, List<Item> answers) {
        this.question = question;
        this.explanation = explanation;
        this.answers = answers;
    }

    public Question(Map<String, Object> questionData) {
        Integer corectStr = (Integer) questionData.get("correct");
        this.correct = corectStr;
        this.question = (String) questionData.get("text-0");
        this.explanation = (String) questionData.get("explanation"); 
        int index = 0;
        while(true){
            ++index;
            String option = (String) questionData.get("text-"+index);
            if(option != null){
                answers.add(new Item(index, option, index==correct));
            }else{
                break;
            }
        }
    }
    
    

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<Item> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Item> answers) {
        this.answers = answers;
    }
    
    
    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }
    
    public void incrementItemByIndex(int index){
        for (Item answer : answers) {
            if(answer.getIndex() == index){
                answer.incrementCount();
            }
        }
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder("Question: "+question);
        sb.append("\nexplanation "+explanation);
        sb.append("\nanswers:\n");
        for (Item answer : answers) {
            sb.append(answer.toString());
        }
        return sb.toString();
    } 
    
    
}
