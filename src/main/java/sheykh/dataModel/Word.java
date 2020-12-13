package sheykh.dataModel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Path;
import java.util.ArrayList;

public class Word {

    private String word;
    private int rep;
    private int[] indexes;
//    private String[] involved;
    private Path file;
    private ArrayList<Sentence> involve;


    public Word(String word, int rep, ArrayList<Sentence> involve) {
        this.word = word;
        this.involve = involve;
        this.rep = rep;
    }

    public void setSentence(ArrayList<Sentence> involve){
        this.involve = involve;
    }

    public ObservableList<Sentence> getInvolved(){
        return FXCollections.observableArrayList(involve);
    }

    public String getWord() {
        return word;
    }

    public int getRep() {
        return rep;
    }

    private void setRep(int rep){
        this.rep = rep;
    }

    private void addSentence(ObservableList<Sentence> sentences){
        this.involve.addAll(sentences);
    }

    public int[] getIndex() {
        return indexes;
    }

    public void joinWords(Word w){
        int newRep = this.getRep() + w.getRep();
        this.setRep(newRep);
        this.addSentence(w.getInvolved());
    }

    public static class Sentence {
        private String sentence;
        private Path file;

        public Sentence(String sentence, Path file){
            this.sentence = sentence;
            this.file = file;
        }
        public String getSentence(){
            return this.sentence;
        }
        public Path getFile(){
            return this.file;
        }
    }

    @Override
    public boolean equals(Object obj) {
        Word check = (Word) obj;
        if(this.getWord().equals(check.getWord())){
            return true;
        }
        return false;
    }


}
