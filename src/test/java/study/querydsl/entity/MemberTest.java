package study.querydsl.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberTest {

    @Autowired
    EntityManager em;

    @Test
    void testEntity() throws Exception {
        // given
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

        // 초기화
        em.flush();
        em.clear();

        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("member.getTeam() = " + member.getTeam());
        }

        // when


        // then


     }


}