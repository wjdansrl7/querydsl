package study.querydsl.entity;

import com.querydsl.core.QueryFactory;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.internal.util.StringUtil.join;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

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

     @Test
     void sort() throws Exception {
         // given
         em.persist(new Member(null, 100));
         em.persist(new Member("member5", 100));
         em.persist(new Member("member6", 100));

         List<Member> result = queryFactory
                 .selectFrom(member)
                 .where(member.age.eq(100))
                 .orderBy(member.age.desc(), member.username.asc().nullsLast())
                 .fetch();

         // when
         Member member5 = result.get(0);
         Member member6 = result.get(1);
         Member memberNull = result.get(2);

         // then
         assertThat(member5.getUsername()).isEqualTo("member5");
         assertThat(member6.getUsername()).isEqualTo("member6");
         assertThat(memberNull.getUsername()).isNull();

        }

         @Test
         void paging1() throws Exception {
             // given
             List<Member> result = queryFactory
                     .selectFrom(member)
                     .orderBy(member.username.desc())
                     .offset(1)
                     .limit(2)
                     .fetch();
             // when


             // then
             assertThat(result.size()).isEqualTo(2);

      }

      @Test
      void paging2() throws Exception {
          // given
          QueryResults<Member> queryResults = queryFactory
                  .selectFrom(member)
                  .orderBy(member.username.desc())
                  .offset(1)
                  .limit(2)
                  .fetchResults();

          // when


          // then
          assertThat(queryResults.getTotal()).isEqualTo(4);
          assertThat(queryResults.getLimit()).isEqualTo(2);
          assertThat(queryResults.getOffset()).isEqualTo(1);
          assertThat(queryResults.getResults().size()).isEqualTo(2);
       }

       @Test
       void aggregation() throws Exception {
           // given
           List<Tuple> result = queryFactory
                   .select(
                           member.count(),
                           member.age.sum(),
                           member.age.avg(),
                           member.age.max(),
                           member.age.min()
                   )
                   .from(member)
                   .fetch();

           // when
           Tuple tuple = result.get(0);


           // then
           assertThat(tuple.get(member.count())).isEqualTo(4);
           assertThat(tuple.get(member.age.sum())).isEqualTo(100);
           assertThat(tuple.get(member.age.max())).isEqualTo(40);
           assertThat(tuple.get(member.age.avg())).isEqualTo(25);
           assertThat(tuple.get(member.age.min())).isEqualTo(10);
        }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
        void group() throws Exception {
            // given
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        // when
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);


        // then
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15); // (10 + 20) / 2


        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35); // (30 + 40) / 2
         }

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    void join() throws Exception {
             // given
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();


        // when


        // then
        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 연관관계가 없으면 조인을 못하나?
     * 세타 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     * 세타 조인 : member의 모든 table 정보와 team의 모든 테이블 정보를 다 조인한다.(물론 DB마다 최적화 해줌)
     * 외부 조인은 불가능 -> 다으멩 설명할 조인 on을 사용하면 외부 조인 가능
     */
    @Test
    void theta_join() throws Exception {
        // given
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        // when
        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        // then
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");


     }

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조인
     * JPQL: select m, t from Member m left join m, team t on t.name = "teamA
     * on절을 활용한 조인 대상 필터링을 사용할 대, 내부조인이면 익숙한 where 절로 해결하고, 정말 외부조인
     * 이 필요한 경우에만 이 기능을 사용하자.
     */
    @Test
     void join_on_filtering() throws Exception {
         // given
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
//                .join(member.team, team)
//                .on(team.name.eq("teamA"))
//                .where(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
        // when


         // then
      }

    /**
     * 연관관계 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     * 연관관계 상관없이 막 조인할 것이기 때문
     * on절: join하는 대상을 필터
     */
    @Test
    void join_on_no_relation() throws Exception {
        // given
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        // when
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
//                .leftJoin(member.team, team) // id가 매칭 된 것도 on절에 함께 들어간다.
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }


        // then


    }

    // entityManagerfactory를 만들어줌
    @PersistenceUnit
    EntityManagerFactory emf;
    @Test
    void fetchJoinNo() throws Exception {
        // given
        em.flush();
        em.clear();


        // when

        Member findMember = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        // then
        assertThat(loaded).as("페치 조인 미적용").isFalse();
     }

    @Test
    void fetchJoinUse() throws Exception {
        // given
        em.flush();
        em.clear();


        // when

        Member findMember = queryFactory
                .selectFrom(QMember.member)
                .join(member.team, team).fetchJoin()
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        // then
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    /**
     * 서브쿼리 : 쿼리 안에 쿼리
     * 나이가 가장 많은 회원 조회
     *
     */
    @Test
    void subQuery() throws Exception {

        QMember memberSub = new QMember("memberSub");
        // given
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();


        // when


        // then
        assertThat(result)
                .extracting("age")
                .containsExactly(40);


     }

    /**
     * 나이가 평균 이상인 회원
     */
    @Test
    void subQueryGoe() throws Exception {

        QMember memberSub = new QMember("memberSub");
        // given
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

//        for (Member member1 : result) {
//            System.out.println("member1 = " + member1);
//        }


        // when


        // then
        assertThat(result)
                .extracting("age")
                .containsExactly(30, 40);


    }

    @Test
    void subQueryIn() throws Exception {

        QMember memberSub = new QMember("memberSub");
        // given
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

//        for (Member member1 : result) {
//            System.out.println("member1 = " + member1);
//        }


        // when


        // then
        assertThat(result)
                .extracting("age")
                .containsExactly(20, 30, 40);
    }

    @Test
    void selectSubQuery() throws Exception {

        QMember memberSub = new QMember("memberSub");

        // given
        List<Tuple> result = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

        // when


        // then
     }

     @Test
     void basicCase() throws Exception {
         // given
         List<String> result = queryFactory
                 .select(member.age
                         .when(10).then("열살")
                         .when(20).then("스무살")
                         .otherwise("기타"))
                 .from(member)
                 .fetch();

         for (String s : result) {
             System.out.println("s = " + s);
         }

         // when


         // then
      }

      // 권장사항: DB의 raw data를 filtering, group 등 최소한만, 직접 튜플들을 가져와서 애플리케이션이나 프레젠테이션 로직에서
      // 사용하는 것을 권장
      @Test
      void complexCase() throws Exception {
          // given
          List<String> result = queryFactory
                  .select(new CaseBuilder()
                          .when(member.age.between(0, 20)).then("0~20살")
                          .when(member.age.between(21, 30)).then("21~30살")
                          .otherwise("기타"))
                  .from(member)
                  .fetch();

          for (String s : result) {
              System.out.println("s = " + s);
          }


          // when


          // then
       }

       @Test
       void constant() throws Exception {
           // given
           List<Tuple> result = queryFactory
                   .select(member.username, Expressions.constant("A"))
                   .from(member)
                   .fetch();

           for (Tuple tuple : result) {
               System.out.println("tuple = " + tuple);
           }

           // when


           // then
        }

        @Test
        void concat() throws Exception {
            // given
            List<String> result = queryFactory
                    .select(member.username.concat("_").concat(member.age.stringValue()))
                    .from(member)
                    .where(member.username.eq("member1"))
                    .fetch();

            for (String s : result) {
                System.out.println("s = " + s);
            }


            // when


            // then


         }



}
