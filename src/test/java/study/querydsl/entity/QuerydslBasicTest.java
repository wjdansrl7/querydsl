package study.querydsl.entity;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;


    @BeforeEach
    public void beforeEach() {
        // given
        queryFactory = new JPAQueryFactory(em);
        Team TeamA = new Team("teamA");
        Team TeamB = new Team("teamB");
        em.persist(TeamA);
        em.persist(TeamB);

        Member member1 = new Member("member1", 10, TeamA);
        Member member2 = new Member("member2", 20, TeamA);

        Member member3 = new Member("member3", 30, TeamB);
        Member member4 = new Member("member4", 40, TeamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    void startJPQL() throws Exception {
        // given
        // member1을 찾아라.
        String qlString = "select m from Member m " +
                "where m.username =: username";
        
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();


        // when


        // then
        assertThat(findMember.getUsername()).isEqualTo("member1");
     }
     
     @Test
     void startQuerydsl() throws Exception {
         // given
//         QMember m = new QMember("m");
//         QMember m = QMember.member;
         // 같은 테이블을 조인해야 하는 경우에만 alias를 설정한다.
//         QMember m1 = new QMember("m1");

         // when
         Member findMember = queryFactory
                 .select(member)
                 .from(member)
                 .where(member.username.eq("member1")) // 파라미터 바인딩 처리
                 .fetchOne();


         // then
         assertThat(findMember.getUsername()).isEqualTo("member1");
      }

      @Test
      void search() throws Exception {
          // given
          Member findMember = queryFactory
                  .selectFrom(QMember.member)
                  .where(QMember.member.username.eq("member1")
                          .and(QMember.member.age.eq(10)))
                  .fetchOne();

          // when


          // then
          assertThat(findMember.getUsername()).isEqualTo("member1");


       }

    @Test
    void searchAndParam() throws Exception {
        // given
        Member findMember = queryFactory
                .selectFrom(QMember.member)
                .where(
                        QMember.member.username.eq("member1"),
                        QMember.member.age.eq(10)
                )
                .fetchOne();

        // when


        // then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void resultFetch() throws Exception {
        // given
//        List<Member> fetch = queryFactory
//                .selectFrom(member)
//                .fetch();
//
//        Member fetchOne = queryFactory
//                .selectFrom(QMember.member)
//                .fetchOne();
//
//        Member fetchFirst = queryFactory
//                .selectFrom(QMember.member)
////                .limit(1).fetchOne();
//                .fetchFirst();

//        QueryResults<Member> results = queryFactory
//                .selectFrom(member)
//                .fetchResults();
//
//        results.getTotal();
//        List<Member> content = results.getResults();

        long total = queryFactory
                .selectFrom(member)
                .fetchCount();

        // when


        // then


     }


}
