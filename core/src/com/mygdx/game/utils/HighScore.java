package com.mygdx.game.utils;

import lombok.Getter;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

@Getter
public class HighScore implements Serializable, Comparable<HighScore> {
    private int score;
    private Date date;

    public HighScore(int score){
        this.score = score;
        date = Calendar.getInstance().getTime();
    }
    @Override
    public int compareTo(HighScore o) {
        return o.score - score;
    }

    public static void serialize(HighScore[] scores_in, int points){
        QuickSortGeneric<HighScore> sortGeneric = new QuickSortGeneric<>();
        HighScore[] scores = scores_in;
        if(scores.length >5 ){
                scores[scores.length-1] = new HighScore(points);
            sortGeneric.quickSort(scores);
        }
        else {
            if(scores.length == 1)
                scores[0] = new HighScore(points);
            else {
                HighScore[] newScore = new HighScore[scores.length];
                System.arraycopy(scores, 0, newScore, 0, scores.length);
                newScore[scores.length-1] = new HighScore(points);
                scores = newScore;
                System.out.println(Arrays.toString(scores));
                sortGeneric.quickSort(scores);
            }
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(Constants.SCORE_FILE_PATH))){
            oos.writeObject(scores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HighScore[] deSerialize(boolean extend){

        try {
            File scoreFile = new File(Constants.SCORE_FILE_PATH);
            if(scoreFile.exists()) {

                BufferedReader br = new BufferedReader(new FileReader(Constants.SCORE_FILE_PATH));
                if (br.readLine() == null) {
                    System.out.println("No errors, and file empty");
                    if(extend)
                        return new HighScore[1];

                    return new HighScore[0];
                }
                else {
                    ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(scoreFile.toPath()));
                    HighScore[] score = (HighScore[]) ois.readObject();

                    if (score.length < 5) {
                        if (extend) {
                            HighScore[] newScore = new HighScore[score.length + 1];
                            System.arraycopy(score, 0, newScore, 0, score.length);
                            score = newScore;
                        }
                    }
                    return score;
                }
            }
            else {
                scoreFile.createNewFile(); // if file already exists will do nothing
                if(extend)
                    return new HighScore[1];

                return new HighScore[0];
            }
        } catch (ClassNotFoundException fileNotFoundException){

//            Files.createFile("assets/scoreFile.ser"); // if file already exists will do nothing
            if(extend)
                return new HighScore[1];
            else
                return new HighScore[0];
        }
        catch (IOException IOexc) {
            IOexc.printStackTrace();
            return new HighScore[1];
        }
    }

}

