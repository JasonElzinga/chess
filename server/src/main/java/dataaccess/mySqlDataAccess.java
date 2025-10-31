package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.CreateGameResponse;
import model.GameData;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class mySqlDataAccess implements DataAccess{

    public mySqlDataAccess() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void clear() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public UserData getUser(String username) {

    }

    @Override
    public void createUser(UserData user) {
        var statement = "INSERT INTO pet (name, type, json) VALUES (?, ?, ?)";
        String json = new Gson().toJson(pet);
        int id = executeUpdate(statement, pet.name(), pet.type(), json);
        return new Pet(id, pet.name(), pet.type());
    }

    @Override
    public void storeAuth(AuthData auth) {

    }

    @Override
    public AuthData getAuthData(String authToken) {
        return null;
    }

    @Override
    public void deleteAuthData(AuthData auth) {

    }

    @Override
    public CreateGameResponse createGame(String gameName) {
        return null;
    }

    @Override
    public List<GameData> listGames() {
        return List.of();
    }

    @Override
    public void joinGame(String username, String playerColor, Integer gameID) {

    }

    @Override
    public GameData getGame(Integer gameID) {
        return null;
    }


    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    //else if (param instanceof ChessGame p) ps.setString(i + 1, serializer.toJson(p));
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS authData (
            authToken VARCHAR(255) NOT NULL,
            username VARCHAR(255) NOT NULL,
            PRIMARY KEY (authToken)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS gameData (
            gameID INT NOT NULL AUTO_INCREMENT,
            whiteUsername VARCHAR(255),
            blackUsername VARCHAR(255),
            gameName VARCHAR(255),
            game JSON,
            PRIMARY KEY (gameID)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS userData (
            username VARCHAR(255) NOT NULL,
            password VARCHAR(500) NOT NULL,
            email VARCHAR(255) NOT NULL,
            PRIMARY KEY (username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };


    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            //throw new DataAccessException(String.format("Unable to configure database: %s", e.getMessage()));
        }
    }

}
