package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void basicTest() throws Exception {
        // given
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

//        List<Member> result1 = memberJpaRepository.findAll();
        // querydsl로 짜면 compile 시점에서 오류를 찾을 수 있고, 기본적으로 parameter binding이기 때문에
        // 신경쓰지 않아도 된다.
        List<Member> result1 = memberJpaRepository.findAll_querydsl();
//        List<Member> result2 = memberJpaRepository.findByUsername("member1");
        List<Member> result2 = memberJpaRepository.findByUsername_querydsl("member1");


        // when
        Member findMember = memberJpaRepository.findById(member.getId()).get();


        // then
         assertThat(findMember).isEqualTo(member);
        assertThat(result1).containsExactly(member);
        assertThat(result2).containsExactly(member);

     }

     @Test
     void searchTest() throws Exception {
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

         MemberSearchCondition condition = new MemberSearchCondition();
         condition.setAgeGoe(35);
         condition.setAgeLoe(40);
         condition.setTeamName("teamB");

//         List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);
         List<MemberTeamDto> result = memberJpaRepository.search(condition);
         assertThat(result).extracting("username").containsExactly("member4");

         // when


         // then
      }




}