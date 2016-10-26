package cat.tecnocampus.databaseRepositories;

import cat.tecnocampus.domain.NoteLab;
import cat.tecnocampus.domain.UserLab;
import cat.tecnocampus.domain.UserLabBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by roure on 19/09/2016.
 */
@Repository
public class UserLabRepository {

    private JdbcOperations jdbcOperations;
    private NoteLabRepository noteLabRepository;
    private PasswordEncoder passwordEncoder;

    public UserLabRepository(JdbcOperations jdbcOperations, NoteLabRepository noteLabRepository, PasswordEncoder passwordEncoder) {
        this.jdbcOperations = jdbcOperations;
        this.noteLabRepository = noteLabRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserLab> findAll() {
        return jdbcOperations.query("Select * from user_lab", new UserLabMapper());
    }

    public List<UserLab> findAllLazy() {
        return jdbcOperations.query("Select * from user_lab", new UserLabMapperLazy());
    }

    public UserLab findOne(String userName) {
        return jdbcOperations.queryForObject("Select * from user_lab where username = ?", new Object[]{userName}, new UserLabMapper());
    }

    public int save(UserLab userLab) {
        int userUpdate = jdbcOperations.update("insert into user_lab values(?, ?, ?, ?)", userLab.getUsername(), userLab.getName(), userLab.getSecondName(), userLab.getEmail());
        noteLabRepository.saveUserNotes(userLab);

        return userUpdate;
    }

    public void saveAuthentication(String username, String password) {
        // inserting user with encoded password
        jdbcOperations.update("insert into users (username, password) values(?, ?)", username, passwordEncoder.encode(password));
        jdbcOperations.update("insert into user_roles (username, role) values(?, 'ROLE_USER')", username);
    }

    private final class UserLabMapper implements RowMapper<UserLab> {
        @Override
        public UserLab mapRow(ResultSet resultSet, int i) throws SQLException {
            UserLab userLab = new UserLabBuilder().setUsername(resultSet.getString("username")).setName(resultSet.getString("name"))
                    .setSecondname(resultSet.getString("second_name")).setEmail(resultSet.getString("email"))
                    .createUserLab();
            List<NoteLab> notes = noteLabRepository.findAllFromUser(userLab.getUsername());
            userLab.addNotes(notes);
            return userLab;
        }
    }

    private final class UserLabMapperLazy implements RowMapper<UserLab> {
        @Override
        public UserLab mapRow(ResultSet resultSet, int i) throws SQLException {
            UserLab userLab = new UserLabBuilder().setUsername(resultSet.getString("username")).setName(resultSet.getString("name"))
                    .setSecondname(resultSet.getString("second_name")).setEmail(resultSet.getString("email"))
                    .createUserLab();
            return userLab;
        }
    }

}
