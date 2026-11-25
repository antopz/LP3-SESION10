package bingo.controlador;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import bingo.modelo.GameModel;
import bingo.persistencia.BingoRepository;
import bingo.vista.BingoView;


public class BingoController {
    private final java.util.function.IntConsumer alVictoria;
    private final Runnable alFinalizar;
    private final GameModel model;
    private final BingoView view;
    private final BingoRepository db;
    private final Timer timer;
    public BingoController(GameModel model,BingoView view,BingoRepository db) throws SQLException {
        this(model, view, db, null, null);
    }
    public BingoController(GameModel model,BingoView view,BingoRepository db,
            java.util.function.IntConsumer alVictoria, Runnable alFinalizar) throws SQLException {
        this.alVictoria = alVictoria;
        this.alFinalizar = alFinalizar;
        this.model=model;this.view=view;this.db=db;
        if(alVictoria==null){model.players.clear();model.players.addAll(db.loadPlayers());}
        db.beginRound(model);
        view.connect(this);
        timer=new Timer(2700,e->drawBall());timer.setInitialDelay(900);timer.start();
    }
    private void error(SQLException ex){
        timer.stop();model.playing=false;
        JOptionPane.showMessageDialog(view,"Error al guardar en SQLite:\n"+ex.getMessage(),"Base de datos",JOptionPane.ERROR_MESSAGE);
    }
    private boolean in(double x,double y,double bx,double by,double w,double h){return x>=bx&&x<=bx+w&&y>=by&&y<=by+h;}
    public void drawBall(){
        if(!model.playing || model.finished)return;
        int n=model.drawBall();
        if(n<0){timer.stop();try{db.finishRound("SIN_BOLITAS",model);}catch(SQLException ex){error(ex);}view.repaint();return;}
        try{db.saveBall(n,model.called.size());}catch(SQLException ex){error(ex);return;}
        view.repaint();
    }
    public void toggle(){
        if(model.bag.isEmpty())return;
        model.playing=!model.playing;if(model.playing)timer.start();else timer.stop();view.repaint();
    }
    public void toggleCards(){model.showCards=!model.showCards;view.repaint();}
    public void newGame(){
        if(JOptionPane.showConfirmDialog(view,"¿Crear otra partida? Se archivará la ronda actual.","Nueva partida",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{db.finishRound("REINICIADA",model);model.newGame();db.beginRound(model);timer.restart();view.repaint();}
        catch(SQLException ex){error(ex);}
    }
    public void claim(){
        if(model.finished){
            JOptionPane.showMessageDialog(view,"El Bingo ya ha finalizado.","Partida finalizada",JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        boolean wasRunning=timer.isRunning();
        timer.stop();
        String[] choices=new String[model.cardsCount];
        for(int i=0;i<model.cardsCount;i++)choices[i]="Cartilla "+(i+1)+" · "+model.players.get(i);
        Object selection=JOptionPane.showInputDialog(view,"¿Quién está cantando BINGO?\nSelecciona su cartilla para comprobarla.",
                "Comprobar BINGO · "+model.modeTitle(),JOptionPane.QUESTION_MESSAGE,null,choices,choices[0]);
        if(selection==null){
            if(wasRunning && model.playing)timer.start();
            return;
        }
        int winner=java.util.Arrays.asList(choices).indexOf(selection.toString());
        if(!model.isWinner(winner)){
            JOptionPane.showMessageDialog(view,model.failureReason(winner),"Bingo todavía no válido",JOptionPane.INFORMATION_MESSAGE);
            if(wasRunning && model.playing)timer.start();
            view.repaint();
            return;
        }
        String mode=model.modeTitle(),name=model.players.get(winner);
        try{db.saveWinner(winner,model);}
        catch(SQLException ex){error(ex);return;}
        JOptionPane.showMessageDialog(view,"¡BINGO de "+name+"!\nModo: "+mode+"\nPremio: 200 puntos.","¡VICTORIA!",JOptionPane.INFORMATION_MESSAGE);
        model.nextMode();
        if(alVictoria!=null)alVictoria.accept(winner);
        try{
            if(model.finished){db.finishRound("FINALIZADA",model);}
            else {db.updateRound(model);if(wasRunning)timer.start();}
        }catch(SQLException ex){error(ex);}
        view.repaint();
        if(model.finished && alFinalizar!=null)alFinalizar.run();
    }
    public void history(){
        try{JTextArea text=new JTextArea(db.history(),20,54);text.setEditable(false);text.setFont(new Font("Monospaced",Font.PLAIN,13));
            JOptionPane.showMessageDialog(view,new JScrollPane(text),"Historial SQLite",JOptionPane.PLAIN_MESSAGE);
        }catch(SQLException ex){error(ex);}
    }
    public void editPlayers(){
        if(alVictoria!=null)return;
        JPanel panel=new JPanel(new GridLayout(model.cardsCount,2,8,8));JTextField[] fields=new JTextField[model.cardsCount];
        for(int i=0;i<model.cardsCount;i++){panel.add(new JLabel("Participante "+(i+1)));fields[i]=new JTextField(model.players.get(i),16);panel.add(fields[i]);}
        if(JOptionPane.showConfirmDialog(view,panel,"Editar participantes (guardados en SQLite)",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        for(int i=0;i<model.cardsCount;i++){
            String name=fields[i].getText().trim();if(name.isEmpty()){JOptionPane.showMessageDialog(view,"Los nombres no pueden estar vacíos.");return;}
            if(name.length()>35){JOptionPane.showMessageDialog(view,"Máximo 35 caracteres por jugador.");return;}
        }
        try{for(int i=0;i<model.cardsCount;i++)db.updatePlayer(i,fields[i].getText().trim());model.players.clear();model.players.addAll(db.loadPlayers());view.repaint();}
        catch(SQLException ex){error(ex);}
    }
    public void onClick(double x,double y){
        view.requestFocusInWindow();
        if(in(x,y,30,812,330,60)){toggleCards();return;}
        if(in(x,y,1230,810,325,70)){claim();return;}
        if(in(x,y,1250,62,50,42)){if(alVictoria==null)model.cardsCount=Math.max(1,model.cardsCount-1);view.repaint();return;}
        if(in(x,y,1455,62,50,42)){if(alVictoria==null)model.cardsCount=Math.min(model.maxCards,model.cardsCount+1);view.repaint();return;}
        if(in(x,y,25,725,380,70)){Object[] choices=alVictoria==null ? new Object[]{"Historial","Editar participantes","Cancelar"} : new Object[]{"Historial","Cancelar"};
            int choice=JOptionPane.showOptionDialog(view,"¿Qué deseas consultar?","Administración",JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,choices,choices[0]);
            if(choice==0)history();else if(choice==1)editPlayers();return;
        }
        if(model.showCards){
            for(int ci=0;ci<model.cardsCount;ci++){
                double cx=(ci%2==0?475:790),cy=(ci/2==0?215:535);
                if(!in(x,y,cx+15,cy+58,268,238))continue;
                int col=(int)((x-(cx+15))/53.6),row=(int)((y-(cy+58))/47.6);
                if(model.toggleMark(ci,row,col)) {
                    try{db.saveMark(ci,row,col,model.cards.get(ci).v[row][col]);}
                    catch(SQLException ex){error(ex);return;}
                }
                view.repaint();return;
            }
        }
    }
    public void close(){timer.stop();try{if(!model.finished)db.finishRound("INTERRUMPIDA",model);db.close();}catch(SQLException ex){System.err.println(ex.getMessage());}}
}
