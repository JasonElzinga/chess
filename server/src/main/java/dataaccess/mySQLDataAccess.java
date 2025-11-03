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
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class mySQLDataAccess implements DataAccess{


    public mySQLDataAccess() throws DataAccessException, SQLException {
        configureDatabase();
    }

    @Override
    public void clear() throws DataAccessException, SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement("DROP TABLE IF EXISTS authdata, gamedata, userdata")) {
                ps.executeUpdate();
            }
        } catch (Exception e) {
            throw new SQLException("Failed to get database connection");
        }
        configureDatabase();
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM userdata WHERE username=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readUserData(rs);
                    }
                } catch (SQLException e) {
                    throw new DataAccessException("Error: database failure");
                }
            } catch (SQLException e) {
                throw new DataAccessException("Error: database failure");
            }
        }catch (SQLException e) {
            throw new DataAccessException("Error: database failure");
        }
        return null;
    }

    private UserData readUserData(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var password = rs.getString("password");
        var email = rs.getString("email");

        UserData user = new UserData(username, password, email);
        return user;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        var statement = "INSERT INTO userdata (username, password, email) VALUES (?, ?, ?)";
        //String json = new Gson().toJson(user);
        executeUpdate(statement, user.username(), user.password(), user.email());
    }

    @Override
    public void storeAuth(AuthData auth) throws DataAccessException {
        var statement = "INSERT INTO authdata (authtoken, username) VALUES (?, ?)";
        //String json = new Gson().toJson(user);
        executeUpdate(statement, auth.authToken(), auth.username());
    }

    @Override
    public AuthData getAuthData(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM authdata WHERE authtoken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readAuthData(rs);
                    }
                } catch (SQLException e) {
                    throw new DataAccessException("Error: database failure");
                }
            } catch (SQLException e) {
                throw new DataAccessException("Error: database failure");
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: database failure");
        }
        return null;
    }


    private AuthData readAuthData(ResultSet rs) throws SQLException {
        var authtoken = rs.getString("authtoken");
        var username = rs.getString("username");

        AuthData auth = new AuthData(username, authtoken);
        return auth;
    }

    @Override
    public void deleteAuthData(AuthData auth) throws DataAccessException {
        var statement = "DELETE FROM authdata WHERE authtoken=?";
        executeUpdate(statement, auth.authToken());
    }

    @Override
    public CreateGameResponse createGame(String gameName) throws DataAccessException {
        var newGame = new ChessGame();
        var serializer = new Gson();
        var jsonGame = serializer.toJson(newGame);
        var statement = "INSERT INTO gamedata (whiteusername, blackusername, gamename, game) VALUES (?, ?, ?, ?)";
        var generatedID = executeUpdate(statement, null, null, gameName, jsonGame);
        return new CreateGameResponse(generatedID);
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM gamedata";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        games.add(readGameData(rs));
                    }
                } catch (SQLException e) {
                    throw new DataAccessException("Error: database failure");
                }
            } catch (SQLException e) {
                throw new DataAccessException("Error: database failure");
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: database failure");
        }
        return games;
    }



    @Override
    public void joinGame(String username, String playerColor, Integer gameID) throws DataAccessException {
        String statement = "";
        if (playerColor.equals("WHITE")) {
            statement = "UPDATE gamedata SET whiteusername= ? WHERE gameID=?";
        } else {
            statement = "UPDATE gamedata SET blackusername= ? WHERE gameID=?";
        }
        executeUpdate(statement, username, gameID);
    }

    @Override
    public GameData getGame(Integer gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM gamedata WHERE gameID=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGameData(rs);
                    }
                } catch (SQLException e) {
                    throw new DataAccessException(e.getMessage());
                }
            } catch (SQLException e) {
                throw new DataAccessException(e.getMessage());
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error: database failure");
        }
        return null;
    }

    private GameData readGameData(ResultSet rs) throws SQLException {
        var serializer = new Gson();
        var gameID = rs.getInt("gameID");
        var whiteUsername = rs.getString("whiteusername");
        var blackUsername = rs.getString("blackusername");
        var gameName = rs.getString("gamename");
        var jsonGame = rs.getString("game");

        var game = serializer.fromJson(jsonGame, ChessGame.class);

        GameData gameData = new GameData(gameID, whiteUsername, blackUsername, gameName, game);
        return gameData;
    }


    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) {
                        ps.setString(i + 1, p);
                    }
                    else if (param instanceof Integer p) {
                        ps.setInt(i + 1, p);
                    }
                    //else if (param instanceof ChessGame p) ps.setString(i + 1, serializer.toJson(p));
                    else if (param == null) {
                        ps.setNull(i + 1, NULL);
                    }
                }
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: database failure");
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS authdata (
            authtoken VARCHAR(255) NOT NULL,
            username VARCHAR(255) NOT NULL,
            PRIMARY KEY (authtoken)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS gamedata (
            gameID INT NOT NULL AUTO_INCREMENT,
            whiteusername VARCHAR(255),
            blackusername VARCHAR(255),
            gamename VARCHAR(255),
            game JSON,
            PRIMARY KEY (gameID)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS userdata (
            username VARCHAR(255) NOT NULL,
            password VARCHAR(500) NOT NULL,
            email VARCHAR(255) NOT NULL,
            PRIMARY KEY (username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };


    private void configureDatabase() throws SQLException, DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: database failure");
        }
    }
}
