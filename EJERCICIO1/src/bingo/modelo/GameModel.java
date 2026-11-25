package bingo.modelo;

import java.util.*;


public class GameModel {
    public static class Card {
        public final int[][] v=new int[5][5];
        public final boolean[][] marked=new boolean[5][5];
    }
    private final Random rng=new Random();
    public final List<Card> cards=new ArrayList<>();
    public final List<Integer> bag=new ArrayList<>(),called=new ArrayList<>();
    public final Set<Integer> calledSet=new HashSet<>();
    public final List<String> players=new ArrayList<>(Arrays.asList("Jugador 1","Jugador 2","Jugador 3","Jugador 4"));
    public int maxCards=4;
    public int cardsCount=4,mode=0,figureIndex=0,lineIndex=0,round=1,points=0;
    public boolean showCards=true,playing=true,finished=false;
    public String status="¡La ronda comienza! Marca los números llamados.";
    private static final String[] FIGURES={"CRUZ","CUATRO ESQUINAS","LETRA X","MARCO","LETRA T","LETRA L"};
    private static final String[] LINES={"COLUMNA","FILA","DIAGONAL","COLUMNA","FILA"};
    public GameModel(){generateRound();}
    private Card makeCard(){
        Card c=new Card();
        for(int col=0;col<5;col++){
            List<Integer> nums=new ArrayList<>();
            for(int n=col*15+1;n<=col*15+15;n++) nums.add(n);
            Collections.shuffle(nums,rng);
            for(int row=0;row<5;row++)c.v[row][col]=nums.get(row);
        }
        c.v[2][2]=0;c.marked[2][2]=true;return c;
    }
    public void generateRound(){
        cards.clear();for(int i=0;i<4;i++)cards.add(makeCard());
        bag.clear();called.clear();calledSet.clear();
        for(int n=1;n<=75;n++)bag.add(n);
        Collections.shuffle(bag,rng);
        playing=true;finished=false;status="";
    }
    public void newGame(){round=1;mode=0;figureIndex=0;lineIndex=0;points=0;generateRound();}
    public int drawBall(){
        if(bag.isEmpty()){playing=false;status="No quedan bolitas.";return -1;}
        int n=bag.remove(bag.size()-1);called.add(n);calledSet.add(n);
        
        
        status="";return n;
    }
    public String letter(int n){return "BINGO".substring((n-1)/15,(n-1)/15+1);}
    public int category(int n){return (n-1)/15;}
    public boolean toggleMark(int cardIndex,int row,int col){
        if(cardIndex<0||cardIndex>=cardsCount||row<0||row>4||col<0||col>4)return false;
        Card c=cards.get(cardIndex);int n=c.v[row][col];
        if(n==0)return false;
        if(!calledSet.contains(n)){status="La bolita "+letter(n)+"-"+n+" aún no ha salido.";return false;}
        
        if(c.marked[row][col])return false;
        c.marked[row][col]=true;return true;
    }
    public String modeTitle(){return mode==0?"LÍNEA ÚNICA":mode==1?"FIGURA · "+FIGURES[figureIndex]:"CARTILLA COMPLETA";}
    public boolean[][] pattern(){
        boolean[][] p=new boolean[5][5];
        if(mode==2){for(boolean[] row:p)Arrays.fill(row,true);return p;}
        if(mode==0){
            switch(lineIndex){
                case 0:for(int r=0;r<5;r++)p[r][0]=true;break;
                case 1:for(int c=0;c<5;c++)p[0][c]=true;break;
                case 2:for(int r=0;r<5;r++)p[r][r]=true;break;
                case 3:for(int r=0;r<5;r++)p[r][4]=true;break;
                default:for(int c=0;c<5;c++)p[4][c]=true;
            }
            return p;
        }
        switch(figureIndex){
            case 0:for(int i=0;i<5;i++){p[2][i]=true;p[i][2]=true;}break;
            case 1:p[0][0]=p[0][4]=p[4][0]=p[4][4]=true;break;
            case 2:for(int i=0;i<5;i++){p[i][i]=true;p[i][4-i]=true;}break;
            case 3:for(int r=0;r<5;r++)for(int c=0;c<5;c++)if(r==0||r==4||c==0||c==4)p[r][c]=true;break;
            case 4:for(int i=0;i<5;i++){p[0][i]=true;p[i][2]=true;}break;
            default:for(int i=0;i<5;i++){p[i][0]=true;p[4][i]=true;}
        }
        return p;
    }
    public boolean isWinner(int index){
        if(index<0 || index>=cardsCount)return false;
        if(mode==0)return completedLine(cards.get(index));
        return completedPattern(cards.get(index),pattern());
    }
    private boolean completedPattern(Card card,boolean[][] required){
        for(int row=0;row<5;row++)for(int col=0;col<5;col++){
            if(required[row][col] && !validMark(card,row,col))return false;
        }
        return true;
    }
    private boolean validMark(Card card,int row,int col){
        int number=card.v[row][col];
        return card.marked[row][col] && (number==0 || calledSet.contains(number));
    }
    private boolean completedLine(Card card){
        for(int row=0;row<5;row++){
            boolean complete=true;
            for(int col=0;col<5;col++)if(!validMark(card,row,col))complete=false;
            if(complete)return true;
        }
        for(int col=0;col<5;col++){
            boolean complete=true;
            for(int row=0;row<5;row++)if(!validMark(card,row,col))complete=false;
            if(complete)return true;
        }
        boolean diagonalA=true,diagonalB=true;
        for(int i=0;i<5;i++){
            if(!validMark(card,i,i))diagonalA=false;
            if(!validMark(card,i,4-i))diagonalB=false;
        }
        return diagonalA || diagonalB;
    }
    public String failureReason(int index){
        if(index<0 || index>=cardsCount)return "Participante no válido.";
        if(isWinner(index))return "¡Bingo válido!";
        if(mode==0)return "Necesitas marcar una línea completa: horizontal, vertical o diagonal. Solo cuentan los números que ya salieron.";
        int missing=0,uncalled=0;
        Card card=cards.get(index);
        boolean[][] required=pattern();
        for(int row=0;row<5;row++)for(int col=0;col<5;col++){
            if(required[row][col] && !validMark(card,row,col)){
                missing++;
                int n=card.v[row][col];
                if(n!=0 && !calledSet.contains(n))uncalled++;
            }
        }
        return "Aún faltan "+missing+" casillas para completar "+modeTitle()+".\n"
            +(uncalled>0?uncalled+" de esos números todavía no han salido.\n":"")
            +"Revisa la figura de la derecha y marca manualmente los números sorteados.";
    }
    public int winnerIndex(){
        for(int index=0;index<cardsCount;index++)if(isWinner(index))return index;
        return -1;
    }
    public void nextMode(){
        
        points+=200;
        if(mode==2){
            finished=true;
            playing=false;
            status="Partida finalizada: se completaron los tres modos.";
            return;
        }
        mode++;
        round++;
        status="";
    }
}
