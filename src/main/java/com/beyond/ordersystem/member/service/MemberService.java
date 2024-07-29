package com.beyond.ordersystem.member.service;

import com.beyond.ordersystem.member.dto.MemberLoginDto;
import com.beyond.ordersystem.member.dto.MemberSaveReqDto;
import com.beyond.ordersystem.member.dto.MemberResDto;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    public Member memberCreate(MemberSaveReqDto dto) {
        if (memberRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        System.out.println(encodedPassword);
        Member member = memberRepository.save
                (dto.toEntity(encodedPassword));
        return member;
    }

    public Page<MemberResDto> memberList(Pageable pageable) {
        Page<Member> members = memberRepository.findAll(pageable);
        return members.map(a-> a.fromEntity());
    }
    
    public Member login(MemberLoginDto dto) {
//        email 존재여부
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일 입니다."));
//        password 일치 여부
        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    public MemberResDto memberMyInfo() {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일"));
        return member.fromEntity();
    }
}
