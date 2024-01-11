package zerobase.weather.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.Memo;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcMemoRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcMemoRepository(DataSource dataSource) { // properties에 작성한 정보 = datasource
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // DB에 저장
    public Memo save(Memo memo) {
        String sql = "insert into memo values(?,?)";
        jdbcTemplate.update(sql, memo.getId(), memo.getText());
        return memo;
    }

    // 모두 찾기
    public List<Memo> findAll() {
        String sql = "select * from memo";
        return jdbcTemplate.query(sql, memoRowMapper());
    }

    // id로 찾기
    public Optional<Memo> findById(int id) {
        String sql = "select * from memo where id=?";
        return jdbcTemplate.query(sql, memoRowMapper(), id).stream().findFirst();
    }

    // DB 데이터를 Memo 객체로 매핑
    private RowMapper<Memo> memoRowMapper() {
        // ResultSet
        // {id = 1, text = 'this is memo~'}
        return(rs, rowNum) -> new Memo(
                rs.getInt("id"),
                rs.getString("text")
        );
    }
}
