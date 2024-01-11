package zerobase.weather.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Memo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class JdbcMemoRepositoryTest {

    @Autowired
    JdbcMemoRepository jdbcMemoRepository; // 클래스를 객체에 담아옴

    @Test
    void insertMemoTest() {

        //given   주어짐
         Memo newMemo = new Memo(2, "insertMemoTest");
        //when   ~ 할 때
        jdbcMemoRepository.save(newMemo);
        //then  이렇게 나올 거다~
        Optional<Memo> result = jdbcMemoRepository.findById(2);
        assertEquals(result.get().getText(), "insertMemoTest");
    }
    @Test
    void findAllMemoTest() {
        //given
        List<Memo> memoList = jdbcMemoRepository.findAll();
        //when
        //then
        System.out.println(memoList);
        assertNotNull(memoList);
    }
}