package sheykh.dataModel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class SearchWord {

    private WordsModel content;
    private String search;
    private ArrayList<Word> result;
    private SearchMode searchMode;

    public enum SearchMode {
        PREFIX, MIDDLE, SUFFIX, CONCAT;
    }

    public SearchWord(WordsModel content, String search, SearchMode searchMode){
        this.content = content;
        this.search = search;
        this.searchMode = searchMode;
        result = new ArrayList<>();

        if(searchMode == SearchMode.PREFIX){
            prefixFind();
        } else if(searchMode == SearchMode.MIDDLE){
            middleFind();
        } else if(searchMode == SearchMode.SUFFIX){
            suffixFind();
        }
    }

    private void prefixFind(){
        for(Word word: content.getWords()){
            if(word.getWord().startsWith(search)){
                result.add(word);
            }
        }
    }

    private void middleFind(){
        for(Word word: content.getWords()){
            if(word.getWord().startsWith(search) || word.getWord().endsWith(search))
                return;
            if(word.getWord().contains(search)){
                result.add(word);
            }
        }
    }


    private void suffixFind(){
        for(Word word: content.getWords()){
            if(word.getWord().endsWith(search)){
                result.add(word);
            }
        }
    }

    private void concatSearch(){

    }

    public ObservableList<Word> getResults(){
        return FXCollections.observableArrayList(result);
    }
}
