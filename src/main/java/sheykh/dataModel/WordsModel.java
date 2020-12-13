package sheykh.dataModel;

import cue.lang.WordIterator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;


public class WordsModel {

    private String content;
    private ArrayList<Word> words;
    private Path file;

    public WordsModel(String content, Path file){
        this.content = content;
        this.file = file;
        words = new ArrayList<>();

        wordIterator(this.content);

    }


//    private final void separator(String content){
//        String[] data = content.split(" |\\n|,|\"");
//        for(int i = 0; i < data.length; i++){
//            if(data[i].equals(" ")){
//                for(int j = i; j< data.length; j++){
//                    data[j] = data[j+1];
//                }
//            }
//        }
//        wordGenerator(data);
//    }

    private final void wordIterator(String content){
        ArrayList<String> result = new ArrayList<>();
        for(final String word: new WordIterator(content)){
            result.add(word);
        }
        String[] data = Arrays.copyOf(result.toArray(), result.size(), String[].class);
        wordGenerator(data);
    }

    private void wordGenerator(String[] data){

        int loop = data.length;
        for(int i=0; i < loop; i++){
            ArrayList<Word.Sentence> sentences = new ArrayList<>();
            ArrayList<Integer> indexes = new ArrayList<>();
            String word = data[i].toLowerCase();
            indexes.add(i);
            sentences.add(new Word.Sentence(sentences(data, i), file));
            int rep = 1;
            if(!isChecked(word)){
                for(int j = i+1; j < loop; j++) {
                    if (word.equals(data[j].toLowerCase())) {
                        indexes.add(j);
                        sentences.add(new Word.Sentence(sentences(data, j), file));
                        rep++;
                    }
                }

                Word item = new Word(word, rep, sentences);
                words.add(item);
            }
        }
    }
    private boolean isChecked (String word){
        word = word.toLowerCase();
        for(Word i: this.words){
            if(word.equals(i.getWord().toLowerCase())){
                return true;
            }
        }

        return false;
    }


    public ObservableList<Word> getWords(){
        return FXCollections.observableArrayList(words);
    }

    public String sentences(String[] words, int index){
        int size = words.length;
        int before, after;
        String result = "";
        if(index >= 15){
            before = index - 15;
        } else{
            before = 0;
        }

        if((size-index) >= 15){
            after = index + 15;
        }else{
            after = size;
        }

        for(int i = before; i< after; i++){
            if(i == index)
                result = result.concat('\u058D' + words[i] + '\u058D').concat(" ");
            else
                result = result.concat(words[i]).concat(" ");
        }


        return result;
    }

    public Path getFile(){
        return this.file;
    }

    public void getColelction(ArrayList<Word> list){
        ArrayList<Word> result = new ArrayList<>();
        for(Word w: this.getWords()){
            if(list.contains(w)){
                int index = list.indexOf(w);
                list.get(index).joinWords(w);
            } else{
                list.add(w);
            }
        }
    }


}