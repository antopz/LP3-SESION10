package bingo.persistencia;

import java.sql.*;
import java.nio.file.*;
import java.util.*;
import bingo.modelo.GameModel;


public class BingoRepository implements AutoCloseable {
    private final Connection conn;
    private long currentGameId;
    public BingoRepository() throws SQLException {
        try {Files.createDirectories(Paths.get("datos"));}catch(Exception e){throw new SQLException("No se puede crear carpeta de datos",e);}
        conn=DriverManager.getConnection("jdbc:sqlite:datos/torneo_arcade.db");
        try(Statement st=conn.createStatement()){
            st.execute("PRAGMA foreign_keys=ON");
            st.execute("CREATE TABLE IF NOT EXISTS jugadores (id INTEGER PRIMARY KEY, nombre TEXT NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS partidas (id INTEGER PRIMARY KEY AUTOINCREMENT, fecha TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, estado TEXT NOT NULL, ronda INTEGER NOT NULL, modo TEXT NOT NULL, puntos INTEGER NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS cartillas (id INTEGER PRIMARY KEY AUTOINCREMENT, partida_id INTEGER NOT NULL REFERENCES partidas(id), jugador_id INTEGER NOT NULL REFERENCES jugadores(id), numeros TEXT NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS sorteos (id INTEGER PRIMARY KEY AUTOINCREMENT, partida_id INTEGER NOT NULL REFERENCES partidas(id), orden INTEGER NOT NULL, numero INTEGER NOT NULL CHECK(numero BETWEEN 1 AND 75), UNIQUE(partida_id,numero))");
            st.execute("CREATE TABLE IF NOT EXISTS ganadores (id INTEGER PRIMARY KEY AUTOINCREMENT, partida_id INTEGER NOT NULL REFERENCES partidas(id), jugador_id INTEGER NOT NULL REFERENCES jugadores(id), modo TEXT NOT NULL, puntos INTEGER NOT NULL, fecha TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            st.execute("CREATE TABLE IF NOT EXISTS marcas_bingo(id INTEGER PRIMARY KEY AUTOINCREMENT,partida_id INTEGER NOT NULL, jugador_id INTEGER NOT NULL,fila INTEGER NOT NULL,columna INTEGER NOT NULL,numero INTEGER NOT NULL,UNIQUE(partida_id,jugador_id,fila,columna))");
        }
        try(PreparedStatement ps=conn.prepareStatement("INSERT OR IGNORE INTO jugadores(id,nombre) VALUES (?,?)")){
            for(int i=1;i<=4;i++){ps.setInt(1,i);ps.setString(2,"Jugador "+i);ps.addBatch();}ps.executeBatch();
        }
    }
    public List<String> loadPlayers() throws SQLException {
        List<String> names=new ArrayList<>(Arrays.asList("Jugador 1","Jugador 2","Jugador 3","Jugador 4"));
        try(Statement st=conn.createStatement();ResultSet rs=st.executeQuery("SELECT id,nombre FROM jugadores ORDER BY id")){
            while(rs.next())if(rs.getInt("id")<=4)names.set(rs.getInt("id")-1,rs.getString("nombre"));
        }return names;
    }
    public void updatePlayer(int index,String name) throws SQLException {
        try(PreparedStatement ps=conn.prepareStatement("UPDATE jugadores SET nombre=? WHERE id=?")){
            ps.setString(1,name);ps.setInt(2,index+1);ps.executeUpdate();
        }
    }
    public void beginRound(GameModel model) throws SQLException {
        if(currentGameId!=0)finishRound("REINICIADA",model);
        conn.setAutoCommit(false);
        try {
            try(PreparedStatement ps=conn.prepareStatement("INSERT INTO partidas(estado,ronda,modo,puntos) VALUES ('EN_CURSO',?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                ps.setInt(1,model.round);ps.setString(2,model.modeTitle());ps.setInt(3,model.points);ps.executeUpdate();
                try(ResultSet rs=ps.getGeneratedKeys()){if(rs.next())currentGameId=rs.getLong(1);else throw new SQLException("No se generó ID de partida");}
            }
            try(PreparedStatement ps=conn.prepareStatement("INSERT INTO cartillas(partida_id,jugador_id,numeros) VALUES (?,?,?)")){
                for(int p=0;p<model.cardsCount;p++){
                    StringJoiner nums=new StringJoiner(",");
                    for(int r=0;r<5;r++)for(int c=0;c<5;c++)nums.add(String.valueOf(model.cards.get(p).v[r][c]));
                    ps.setLong(1,currentGameId);ps.setInt(2,p+1);ps.setString(3,nums.toString());ps.addBatch();
                }ps.executeBatch();
            }
            conn.commit();
        }catch(SQLException ex){conn.rollback();currentGameId=0;throw ex;}
        finally{conn.setAutoCommit(true);}
    }
    public void saveMark(int player,int row,int col,int number) throws SQLException {
        if (currentGameId == 0) return;
        try(PreparedStatement ps=conn.prepareStatement("INSERT OR IGNORE INTO marcas_bingo(partida_id,jugador_id,fila,columna,numero) VALUES (?,?,?,?,?)")) {
            ps.setLong(1,currentGameId);ps.setInt(2,player+1);ps.setInt(3,row);ps.setInt(4,col);ps.setInt(5,number);ps.executeUpdate();
        }
    }
    public void saveBall(int number,int order) throws SQLException {
        try(PreparedStatement ps=conn.prepareStatement("INSERT INTO sorteos(partida_id,orden,numero) VALUES (?,?,?)")){
            ps.setLong(1,currentGameId);ps.setInt(2,order);ps.setInt(3,number);ps.executeUpdate();
        }
    }
    public void finishRound(String status,GameModel model) throws SQLException {
        if(currentGameId==0)return;
        try(PreparedStatement ps=conn.prepareStatement("UPDATE partidas SET estado=?,puntos=? WHERE id=?")){
            ps.setString(1,status);ps.setInt(2,model.points);ps.setLong(3,currentGameId);ps.executeUpdate();
        }currentGameId=0;
    }
    
    public void updateRound(GameModel model) throws SQLException {
        if(currentGameId==0)throw new SQLException("No hay partida activa");
        try(PreparedStatement ps=conn.prepareStatement("UPDATE partidas SET ronda=?,modo=?,puntos=? WHERE id=?")){
            ps.setInt(1,model.round);ps.setString(2,model.modeTitle());
            ps.setInt(3,model.points);ps.setLong(4,currentGameId);ps.executeUpdate();
        }
    }
    public void saveWinner(int playerIndex,GameModel model) throws SQLException {
        if(currentGameId==0)throw new SQLException("No hay partida activa");
        conn.setAutoCommit(false);
        try{
            try(PreparedStatement ps=conn.prepareStatement("INSERT INTO ganadores(partida_id,jugador_id,modo,puntos) VALUES (?,?,?,?)")){
                ps.setLong(1,currentGameId);ps.setInt(2,playerIndex+1);
                ps.setString(3,model.modeTitle());ps.setInt(4,200);ps.executeUpdate();
            }
            try(PreparedStatement ps=conn.prepareStatement("UPDATE partidas SET puntos=? WHERE id=?")){
                ps.setInt(1,model.points+200);ps.setLong(2,currentGameId);ps.executeUpdate();
            }
            conn.commit(); 
        }catch(SQLException e){conn.rollback();throw e;}finally{conn.setAutoCommit(true);}
    }
    public String history() throws SQLException {
        StringBuilder b=new StringBuilder("ULTIMAS PARTIDAS\n\n");
        try(Statement st=conn.createStatement();ResultSet rs=st.executeQuery(
          "SELECT p.id,p.fecha,p.ronda,p.modo,p.estado,p.puntos, " +
          "(SELECT COUNT(*) FROM sorteos s WHERE s.partida_id=p.id) bolas, " +
          "(SELECT GROUP_CONCAT(j.nombre || ' [' || g.modo || ']', ', ') " +
          " FROM ganadores g JOIN jugadores j ON j.id=g.jugador_id WHERE g.partida_id=p.id) ganadores " +
          "FROM partidas p ORDER BY p.id DESC LIMIT 20")){
            while(rs.next()){b.append("#").append(rs.getInt("id")).append(" · ").append(rs.getString("fecha"))
                .append(" · Ronda ").append(rs.getInt("ronda")).append("\n")
                .append(rs.getString("modo")).append(" | ").append(rs.getString("estado"))
                .append(" | Puntos: ").append(rs.getInt("puntos"))
                .append(" | Bolas: ").append(rs.getInt("bolas"))
                .append(" | ").append(rs.getString("ganadores")==null?"Sin ganadores":rs.getString("ganadores")).append("\n\n");}
        }return b.toString();
    }
    @Override public void close() throws SQLException {conn.close();}
}
