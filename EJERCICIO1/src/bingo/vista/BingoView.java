package bingo.vista;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import bingo.modelo.GameModel;
import bingo.controlador.BingoController;
import torneo.vista.Avatares;


public class BingoView extends JPanel {
    private static final int W=1600, H=900;
    private static final Color INK=new Color(16,27,53), MUTED=new Color(139,155,184);
    private static final Color GOLD=new Color(255,210,65), RED=new Color(245,77,96);
    private static final Color[] BALL={new Color(43,180,243),new Color(248,91,137),
            new Color(164,119,255),new Color(75,215,151),new Color(255,186,66)};
    private static final String[] LETTERS={"B","I","N","G","O"};
    private final GameModel model;
    private int[] avatares = new int[0];
    public void setAvatares(int[] nuevos) { avatares = nuevos.clone(); repaint(); }
    public BingoView(GameModel model) {
        this.model=model;setPreferredSize(new Dimension(W,H));
        setBackground(new Color(11,18,37));setFocusable(true);
    }
    public void connect(BingoController controller) {
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                controller.onClick(e.getX()*W/(double)getWidth(),e.getY()*H/(double)getHeight());
            }
        });
        registerShortcut("H","hide",controller::toggleCards);
        
        setToolTipText(" ");
    }
    @Override public String getToolTipText(java.awt.event.MouseEvent event) {
        if(!model.showCards)return null;
        double x=event.getX()*W/(double)Math.max(1,getWidth());
        double y=event.getY()*H/(double)Math.max(1,getHeight());
        for(int ci=0;ci<model.cardsCount;ci++){
            double cx=(ci%2==0?475:790),cy=(ci/2==0?215:535);
            double xx=x-(cx+15), yy=y-(cy+58);
            if(xx<0||yy<0||xx>=268||yy>=238)continue;
            int col=(int)(xx/53.6),row=(int)(yy/47.6);
            if(row>=5||col>=5)continue;
            int num=model.cards.get(ci).v[row][col];
            if(num==0)return "Espacio libre";
            String prefix=model.letter(num)+"-"+num;
            if(model.cards.get(ci).marked[row][col])return prefix+" - marcado";
            return model.calledSet.contains(num)?prefix+" - haz clic para marcar":prefix+" - aun no ha salido";
        }
        return null;
    }
    private void registerShortcut(String stroke,String key,Runnable r) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(stroke),key);
        getActionMap().put(key,new AbstractAction(){public void actionPerformed(java.awt.event.ActionEvent e){r.run();}});
    }
    @Override protected void paintComponent(Graphics graphics){
        super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();
        g.scale(getWidth()/(double)W,getHeight()/(double)H);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        background(g);topBar(g);left(g);right(g);
        if(model.showCards){for(int i=0;i<model.cardsCount;i++)drawCard(g,model.cards.get(i),i,(i%2==0?475:790),(i/2==0?215:535));}
        else bigBoard(g);
        g.dispose();
    }
    private void background(Graphics2D g){
        g.setPaint(new GradientPaint(0,0,new Color(22,29,69),1600,900,new Color(13,43,66)));g.fillRect(0,0,W,H);
        g.setColor(new Color(73,95,147,32));
        for(int x=-200;x<1800;x+=80)g.drawLine(x,0,x+350,H);
        for(int i=0;i<24;i++){int x=(i*271+100)%W,y=(i*183+97)%H;
            g.setColor(new Color(255,255,255,16+(i%4)*7));g.fillOval(x,y,3+i%4,3+i%4);}
        round(g,15,15,1570,870,36,new Color(7,13,31,92),null,0);
    }
    private void shadow(Graphics2D g,int x,int y,int w,int h,int rad){round(g,x+4,y+7,w,h,rad,new Color(2,6,21,115),null,0);}
    private void round(Graphics2D g,double x,double y,double w,double h,double rad,Color fill,Color border,float stroke){
        RoundRectangle2D r=new RoundRectangle2D.Double(x,y,w,h,rad,rad);if(fill!=null){g.setColor(fill);g.fill(r);}if(border!=null){g.setStroke(new BasicStroke(stroke));g.setColor(border);g.draw(r);}
    }
    private void txt(Graphics2D g,String s,double x,double y,int size,Color color,boolean bold){
        g.setFont(new Font("SansSerif",bold?Font.BOLD:Font.PLAIN,size));g.setColor(color);g.drawString(s,(float)x,(float)y);
    }
    private void center(Graphics2D g,String s,double cx,double y,int size,Color color,boolean bold){
        g.setFont(new Font("SansSerif",bold?Font.BOLD:Font.PLAIN,size));FontMetrics fm=g.getFontMetrics();
        g.setColor(color);g.drawString(s,(float)(cx-fm.stringWidth(s)/2.0),(float)y);
    }
    private void pill(Graphics2D g,int x,int y,int w,int h,String label,Color bg,Color fg,int font){
        round(g,x,y,w,h,16,bg,null,0);center(g,label,x+w/2.0,y+h/2.0+font*.35,font,fg,true);
    }
    private void topBar(Graphics2D g){
        shadow(g,390,26,790,162,32);round(g,390,26,790,162,32,new Color(35,55,92),new Color(91,135,183),3);
        txt(g,"BINGO",420,62,23,GOLD,true);
        center(g,"RONDA "+model.round+"  ·  BOLITAS LLAMADAS: "+model.called.size()+" / 75",835,59,17,new Color(216,230,246),true);
        for(int i=0;i<5;i++){
            int idx=model.called.size()-1-i;int x=476+i*137;
            round(g,x,81,104,92,48,new Color(16,31,58),new Color(72,101,148),2);
            if(idx>=0){int n=model.called.get(idx), cat=model.category(n);ball(g,x+52,127,i==0?43:33,n,cat,i==0);}
            else center(g,"·",x+52,146,46,new Color(59,88,128),true);
        }
    }
    private void ball(Graphics2D g,double cx,double cy,double radius,int n,int cat,boolean large){
        Color base=BALL[cat];g.setColor(new Color(0,0,0,100));g.fill(new Ellipse2D.Double(cx-radius+4,cy-radius+7,2*radius,2*radius));
        g.setPaint(new GradientPaint((float)(cx-radius),(float)(cy-radius),base.brighter(),(float)(cx+radius),(float)(cy+radius),base.darker()));
        g.fill(new Ellipse2D.Double(cx-radius,cy-radius,2*radius,2*radius));
        g.setStroke(new BasicStroke(2.3f));g.setColor(new Color(255,255,255,115));g.draw(new Ellipse2D.Double(cx-radius+4,cy-radius+4,2*radius-8,2*radius-8));
        double inner=radius*.70;g.setColor(new Color(255,255,255,244));g.fill(new Ellipse2D.Double(cx-inner,cy-inner,2*inner,2*inner));
        center(g,LETTERS[cat],cx,cy-(large?5:3),large?18:14,base.darker(),true);
        center(g,String.valueOf(n),cx,cy+(large?19:15),large?29:23,INK,true);
    }
    private void left(Graphics2D g){
        if(model.showCards){
            shadow(g,25,199,380,475,26);round(g,25,199,380,475,26,new Color(30,44,77),new Color(88,115,163),2);
            for(int col=0;col<5;col++){
                int x=40+col*71;pill(g,x,213,64,42,LETTERS[col],BALL[col],INK,26);
                for(int r=0;r<15;r++){
                    int n=col*15+r+1, xx=x, yy=264+r*26;
                    boolean hit=model.calledSet.contains(n);
                    round(g,xx,yy,64,23,7,hit?BALL[col]:new Color(47,62,94),null,0);
                    center(g,String.valueOf(n),xx+32,yy+17,14,hit?INK:new Color(173,190,211),true);
                }
            }
            txt(g,"●  Color = número que ya salió",37,701,15,new Color(204,226,242),true);
        }else{
            round(g,25,220,380,176,24,new Color(34,52,87),new Color(73,101,151),2);
            center(g,"CARTILLAS OCULTAS",215,270,24,Color.WHITE,true);
            center(g,"Consulta el tablero ampliado",215,313,17,MUTED,false);
            center(g,"o vuelve a mostrar tus cartillas.",215,339,17,MUTED,false);
        }
        
        shadow(g,25,725,380,70,17);
        round(g,25,725,380,70,17,new Color(31,49,82),new Color(91,126,176),1.5f);
        txt(g,"JUGADORES EN SALA",39,745,13,GOLD,true);
        for(int i=0;i<model.cardsCount;i++){
            int px=39+i*91;
            round(g,px,753,83,31,11,new Color(46,68,108),new Color(80,117,169),1);
            if (i < avatares.length && Avatares.imagen(avatares[i]) != null) g.drawImage(Avatares.imagen(avatares[i]),px+3,755,27,26,null);
            else { g.setColor(BALL[i]);g.fill(new Ellipse2D.Double(px+7,759,18,18)); center(g,""+(i+1),px+16,772,11,INK,true); }
            txt(g,model.players.get(i).length()>7?model.players.get(i).substring(0,7):model.players.get(i),px+30,773,11,Color.WHITE,true);
        }
        shadow(g,30,812,330,60,18);round(g,30,812,330,60,18,new Color(57,96,167),new Color(153,190,249),2);
        center(g,model.showCards?"OCULTAR TARJETAS":"MOSTRAR TARJETAS",195,849,20,Color.WHITE,true);
    }
    private void right(Graphics2D g){
        center(g,"TARJETAS DE BINGO",1390,41,24,GOLD,true);
        shadow(g,1205,52,358,58,17);round(g,1205,52,358,58,17,new Color(38,61,103),new Color(96,131,187),2);
        
        center(g,String.valueOf(model.cardsCount),1381,93,35,GOLD,true);
        
        shadow(g,1235,127,310,238,28);round(g,1235,127,310,238,28,new Color(37,61,99),new Color(102,143,200),3);
        center(g,model.mode==0?"LÍNEA · CUALQUIER DIRECCIÓN":model.modeTitle(),1390,162,model.mode==1||model.mode==0?16:22,GOLD,true);
        boolean[][] p=model.pattern();
        for(int r=0;r<5;r++)for(int c=0;c<5;c++){
            int x=1285+c*43,y=177+r*34;
            round(g,x,y,36,29,8,p[r][c]?new Color(96,240,80):new Color(224,231,243),p[r][c]?new Color(164,255,128):new Color(130,155,187),1);
        }
        center(g,model.mode==0?"HORIZONTAL, VERTICAL O DIAGONAL":"OBJETIVO DE LA RONDA",1390,355,12,new Color(193,212,236),true);
        shadow(g,1230,530,325,100,25);round(g,1230,530,325,100,25,new Color(34,61,99),new Color(91,130,178),2);
        center(g,"PUNTOS",1393,563,18,new Color(189,211,242),true);
        center(g,String.format("%,d",model.points),1393,603,37,Color.WHITE,true);
        shadow(g,1230,810,325,70,24);
        round(g,1230,810,325,70,24,new Color(255,191,32),new Color(255,236,134),3);
        center(g,"¡BINGO!",1392,859,39,INK,true);
    }
    private void drawCard(Graphics2D g,GameModel.Card card,int index,int x,int y){
        shadow(g,x,y,296,306,23);round(g,x,y,296,306,23,new Color(238,72,92),new Color(255,156,161),3);
        round(g,x+8,y+10,280,286,16,new Color(164,43,66),null,0);
        round(g,x+8,y+10,280,44,15,new Color(255,89,104),null,0);
        for(int c=0;c<5;c++)center(g,LETTERS[c],x+41+c*53.6,y+42,33,Color.WHITE,true);
        for(int r=0;r<5;r++)for(int c=0;c<5;c++){
            int n=card.v[r][c];double xx=x+15+c*53.6,yy=y+58+r*47.6;
            boolean marked=card.marked[r][c];
            Color fill=n==0?new Color(255,215,118):marked?BALL[c]:new Color(251,251,255);
            round(g,xx,yy,47,41,8,fill,marked?Color.WHITE:new Color(201,211,230),1.3f);
            if(n==0){center(g,"LIBRE",xx+23.5,yy+18,11,INK,true);center(g,"★",xx+23.5,yy+34,16,INK,true);}
            else {
                
                center(g,String.valueOf(n),xx+23.5,yy+28,22,INK,true);
                if(marked){
                    
                    g.setColor(new Color(255,255,255,235));
                    g.fill(new Ellipse2D.Double(xx+35,yy+3,9,9));
                    g.setColor(new Color(19,95,59));
                    g.setStroke(new BasicStroke(1.5f));
                    g.drawLine((int)xx+37,(int)yy+7,(int)xx+39,(int)yy+9);
                    g.drawLine((int)xx+39,(int)yy+9,(int)xx+42,(int)yy+5);
                }
            }
        }
        pill(g,x+232,y+12,41,29,""+(index+1),new Color(33,49,91),Color.WHITE,15);
        if (index < avatares.length && Avatares.imagen(avatares[index]) != null) {
            g.drawImage(Avatares.imagen(avatares[index]), x+204, y+7, 26, 39, null);
        }
    }
    private void bigBoard(Graphics2D g){
        int x=450,y=218,w=705,h=627;
        shadow(g,x,y,w,h,28);round(g,x,y,w,h,28,new Color(29,49,83),new Color(92,141,192),3);
        center(g,"TABLERO DE NÚMEROS · BINGO 75",x+w/2.0,y+46,27,GOLD,true);
        for(int col=0;col<5;col++){
            int cx=x+36+col*134;
            pill(g,cx,y+65,118,43,LETTERS[col],BALL[col],INK,29);
            for(int r=0;r<15;r++){
                int n=col*15+r+1;int yy=y+117+r*31;
                round(g,cx,yy,118,27,8,model.calledSet.contains(n)?BALL[col]:new Color(57,74,111),null,0);
                center(g,String.format("%02d",n),cx+59,yy+20,19,model.calledSet.contains(n)?INK:new Color(199,214,232),true);
            }
        }
    }
}
